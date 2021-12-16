package com.cometproject.server.network.messages.incoming.room.action;

import com.cometproject.server.boot.Comet;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.game.rooms.filter.FilterResult;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.PrivateChatBedFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.PrivateChatFloorItem;
import com.cometproject.server.logging.LogManager;
import com.cometproject.server.logging.entries.RoomChatLogEntry;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.moderation.ModToolMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.AdvancedAlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.NotificationMessageComposer;
import com.cometproject.server.network.messages.outgoing.nuxs.EmailVerificationWindowMessageComposer;
import com.cometproject.server.network.messages.outgoing.nuxs.SMSVerificationCompleteMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.MutedMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.ShoutMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.TalkMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.WhisperMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;



public class TalkMessageEvent implements Event {
    public void handle(Session client, MessageEvent msg) {
        String message = msg.readString();
        int bubble = msg.readInt();

        if (client.getPlayer().getSettings().getPersonalPin() != null){
            if(message.equalsIgnoreCase(client.getPlayer().getSettings().getPersonalPin().toLowerCase())) {
                client.getPlayer().sendBubble("pincode", Locale.getOrDefault("pin.code.success", "Acabas de introducir tu pin correctamente, ¡disfruta de tu sesión!"));
                client.getPlayer().getSettings().setPinSucces();
                client.sendQueue(new ModToolMessageComposer());
                client.sendQueue(new SMSVerificationCompleteMessageComposer(2,2));
                return;
            }
            else if(client.getPlayer().getPermissions().getRank().modTool() && !client.getPlayer().getSettings().isPinSuccess()) {
                client.getPlayer().sendBubble("pincode", Locale.getOrDefault("pin.code.required", "Debes verificar tu PIN antes de realizar cualquier acción."));
                client.send(new EmailVerificationWindowMessageComposer(1,1));
                return;
            }
        }

        final int timeMutedExpire = client.getPlayer().getData().getTimeMuted() - (int) Comet.getTime();

        PlayerEntity playerEntity = client.getPlayer().getEntity();

        if (playerEntity == null || playerEntity.getRoom() == null || playerEntity.getRoom().getEntities() == null)
            return;

        if (!playerEntity.isVisible() && !playerEntity.getPlayer().isInvisible()) {
            return;
        }

        if (client.getPlayer().getData().getTimeMuted() != 0) {
            if (client.getPlayer().getData().getTimeMuted() > (int) Comet.getTime()) {
                client.getPlayer().getSession().send(new MutedMessageComposer(timeMutedExpire));
                return;
            }
        }

        bubble = ShoutMessageEvent.getBubble(client, bubble);

        if (client.getPlayer().getChatMessageColour() != null) {
            message = "@" + client.getPlayer().getChatMessageColour() + "@" + message;

            if (message.toLowerCase().startsWith("@" + client.getPlayer().getChatMessageColour() + "@:")) {
                message = message.toLowerCase().replace("@" + client.getPlayer().getChatMessageColour() + "@:", ":");
            }
        }

        String filteredMessage = filterMessage(message);

        if (filteredMessage == null) {
            return;
        }

        if (!client.getPlayer().getPermissions().getRank().roomFilterBypass()) {
            FilterResult filterResult = RoomManager.getInstance().getFilter().filter(filteredMessage);

            if (filterResult.isBlocked()) {
                filterResult.sendLogToStaffs(client, "Room: " + playerEntity.getRoom().getData().getId() + "");
                client.send(new AdvancedAlertMessageComposer(Locale.get("game.message.blocked").replace("%s", filterResult.getMessage())));
                return;
            } else if (filterResult.wasModified()) {
                filteredMessage = filterResult.getMessage();
            }

            filteredMessage = playerEntity.getRoom().getFilter().filter(playerEntity, filteredMessage);
        }

        if (playerEntity.onChat(filteredMessage)) {
            if (message.startsWith("@")) {
                String finalName;
                String[] splittedName = message.replace("@", "").split(" ");
                finalName = splittedName[0];

                Session player = NetworkManager.getInstance().getSessions().getByPlayerUsername(finalName);

                if (player != null) {
                    player.send(new NotificationMessageComposer("Arroba", Locale.getOrDefault("mention.message", "The user %s has mentioned you in a room (%b), click here to go to the room.")
                            .replace("%s", client.getPlayer().getData().getUsername())
                            .replace("%b", message), "event:navigator/goto/" + client.getPlayer().getEntity().getRoom().getData().getId()));

                    client.send(new WhisperMessageComposer(client.getPlayer().getData().getId(), Locale.getOrDefault("mention.success", "You've mention %s successfully")
                            .replace("%s", finalName), 34));
                } else {
                    client.send(new WhisperMessageComposer(client.getPlayer().getData().getId(), Locale.getOrDefault("mention.notexist", "The user %s does not exist or it's disconnected")
                            .replace("%s", finalName), 34));
                }
            }

            try {
                if (LogManager.ENABLED && !message.replace(" ", "").isEmpty())
                    LogManager.getInstance().getStore().getLogEntryContainer().put(new RoomChatLogEntry(playerEntity.getRoom().getId(), client.getPlayer().getId(), message));
            } catch (Exception ignored) {

            }

            /*try {
                filteredMessage = TranslationComponent.translate("fr", "es", message);
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            if (client.getPlayer().getEntity().getPrivateChatItemId() != 0) {
                // broadcast message only to players in the tent.
                RoomItemFloor floorItem = client.getPlayer().getEntity().getRoom().getItems().getFloorItem(client.getPlayer().getEntity().getPrivateChatItemId());

                if (floorItem != null) {
                    if(floorItem instanceof PrivateChatBedFloorItem){
                        ((PrivateChatBedFloorItem) floorItem).broadcastMessage(new ShoutMessageComposer(playerEntity.getId(), filteredMessage, RoomManager.getInstance().getEmotions().getEmotion(filteredMessage), bubble));
                    } else
                    ((PrivateChatFloorItem) floorItem).broadcastMessage(new TalkMessageComposer(client.getPlayer().getEntity().getId(), filteredMessage, RoomManager.getInstance().getEmotions().getEmotion(filteredMessage), bubble));
                }
            } else {
                client.getPlayer().getEntity().getRoom().getEntities().broadcastChatMessage(new TalkMessageComposer(client.getPlayer().getEntity().getId(), filteredMessage, RoomManager.getInstance().getEmotions().getEmotion(filteredMessage), bubble), client.getPlayer().getEntity());
            }

            playerEntity.postChat(filteredMessage);
        }


    }

    public static String filterMessage(String message) {
        if (message.contains("You can type here to talk!")) {
            message = message.replace("You can type here to talk!", "");
        }

        return message.replace((char) 13 + "", "").replace("<", "&lt;").replace("&#10º;", "");
    }
}
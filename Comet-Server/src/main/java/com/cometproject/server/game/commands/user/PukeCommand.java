package com.cometproject.server.game.commands.user;

import com.cometproject.api.game.quests.QuestType;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.rooms.objects.entities.effects.PlayerEffect;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.room.avatar.WhisperMessageComposer;
import com.cometproject.server.network.sessions.Session;

public class PukeCommand extends ChatCommand {
    @Override
    public void execute(Session client, String[] params) {
        if (params.length != 1) {
            sendNotif(Locale.getOrDefault("command.puke.none", "¿A quién quieres vomitarle?"), client);
            return;
        }

        if (client.getPlayer().getEntity().isRoomMuted() || client.getPlayer().getEntity().getRoom().getRights().hasMute(client.getPlayer().getId())) {
            sendNotif(Locale.getOrDefault("command.user.muted", "Estás silenciado."), client);
            return;
        }

        String kissedPlayer = params[0];
        Session kissedSession = NetworkManager.getInstance().getSessions().getByPlayerUsername(kissedPlayer);

        if (kissedSession == null) {
            sendNotif(Locale.getOrDefault("command.user.offline", "¡El usuario no está en línea!"), client);
            return;
        }

        if (kissedSession.getPlayer().getEntity() == null) {
            sendNotif(Locale.getOrDefault("command.user.notinroom", "El usuario no está en ninguna sala."), client);
            return;
        }

        if (kissedSession.getPlayer().getData().getUsername().equals(client.getPlayer().getData().getUsername())) {
            sendNotif(Locale.getOrDefault("command.puke.himself", "Respetamos la coprofagia, pero lo de vomitarte encima como que ya es pasarse."), client);
            return;
        }

        if(kissedSession.getPlayer().getData().getRank() >= 6){
            client.getPlayer().getQuests().progressQuest(QuestType.EXPLORE_2, 1);
        }

        int posX = kissedSession.getPlayer().getEntity().getPosition().getX();
        int posY = kissedSession.getPlayer().getEntity().getPosition().getY();
        int playerX = client.getPlayer().getEntity().getPosition().getX();
        int playerY = client.getPlayer().getEntity().getPosition().getY();

        if (!((Math.abs((posX - playerX)) >= 2) || (Math.abs(posY - playerY) >= 2))) {
            client.getPlayer().getEntity().getRoom().getEntities().broadcastMessage(new WhisperMessageComposer(client.getPlayer().getEntity().getId(), "<b>" + client.getPlayer().getData().getUsername() + Locale.getOrDefault("command.puke.word", "</b> vomita sobre") + " " + kissedSession.getPlayer().getData().getUsername() + ". <i>*Qué puto asco men*</i>", 1));
            client.getPlayer().getEntity().applyEffect(new PlayerEffect(998, 2));
            kissedSession.getPlayer().getEntity().applyEffect(new PlayerEffect(169, 15));
            client.getPlayer().getQuests().progressQuest(QuestType.WEEN_PUKE_PLAYER, 1);
        } else {
            client.getPlayer().getSession().send(new WhisperMessageComposer(client.getPlayer().getEntity().getId(), Locale.getOrDefault("command.notaround", "No estás lo suficientemente cerca de %playername%. Acércate para poder interactuar.").replace("%playername%", kissedSession.getPlayer().getData().getUsername()), 34));
        }
    }

    @Override
    public String getPermission() {
        return "puke_command";
    }

    @Override
    public String getParameter() {
        return Locale.getOrDefault("command.parameter.username", "%username%");
    }

    @Override
    public String getDescription() {
        return Locale.get("command.puke.description");
    }
}
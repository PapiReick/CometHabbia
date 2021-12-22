package com.cometproject.server.game.commands.staff.alerts;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.networking.messages.IMessageComposer;
import com.cometproject.api.networking.sessions.ISession;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.moderation.ModerationManager;
import com.cometproject.server.game.rooms.types.misc.ChatEmotion;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.landing.TargettedOfferMessageComposer;
import com.cometproject.server.network.messages.outgoing.messenger.InstantChatMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.AdvancedAlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.TalkMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.network.websockets.WebSocketSessionManager;
import com.cometproject.server.network.websockets.packets.outgoing.alerts.EventAlertWebPacket;
import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import com.mrpowergamerbr.temmiewebhook.embed.FieldEmbed;
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed;
import com.mrpowergamerbr.temmiewebhook.embed.ThumbnailEmbed;

import java.util.Arrays;

public class EventAlertCommand extends ChatCommand {
    static TemmieWebhook discordLog;

    @Override
    public void execute(Session client, String[] params) {
        if (params.length == 0) {
            return;
        }

        int roomId = client.getPlayer().getEntity().getRoom().getId();

        String logDesc;

            CometSettings.setCurrentEventRoom(roomId);
            WebSocketSessionManager.getInstance().sendMessage(new EventAlertWebPacket("sendEventAlert", client.getPlayer().getData().getFigure(), client.getPlayer().getData().getUsername(), this.merge(params), roomId + ""));

            final IMessageComposer whisper = new TalkMessageComposer(
                    -1, Locale.getOrDefault("none.ishere", "Hay un nuevo evento creado por %username%, haz click <a href=\'event:navigator/goto/" + roomId + "'><b>aquí</b></a> para ir al evento.").replace("%message%", this.merge(params)) .replace("%username%", client.getPlayer().getData().getUsername()) + "<br><br><i> " + client.getPlayer().getData().getUsername() + "</i>", ChatEmotion.NONE, 33);

            for (ISession session : NetworkManager.getInstance().getSessions().getSessions().values()) {
                if (session.getPlayer() != null && !session.getPlayer().getSettings().ignoreEvents())
                            session.send(whisper);
                    }

            logDesc = "El staff %s ha creado un evento en la sala '%b'."
                    .replace("%s", client.getPlayer().getData().getUsername())
                    .replace("%b", client.getPlayer().getEntity().getRoom().getData().getName());

            for (Session player : ModerationManager.getInstance().getLogChatUsers()) {
                player.send(new InstantChatMessageComposer(logDesc, Integer.MAX_VALUE - 1));
            }

           discordLog = new TemmieWebhook("https://discord.com/api/webhooks/923216334031028295/-QFjGYny9tZfTXzDxyjQBekUAeSoPaIWcCJ9jR_E9M6izwMsTjaSSaQAnE_9deSBHWSt");

            DiscordEmbed de = DiscordEmbed.builder()
                    .color(3426654)
                    .title("¡Nuevo evento en Habbia!") // We are creating a embed with this title...
                    .description("¡Consigue monedas y pásalo en grande, no te lo pierdas!") // with this description...
                    .url("https://www.habbia.es/client" + roomId) // that, when clicked, goes to the TemmieWebhook repo...
                    .footer(FooterEmbed.builder() // with a fancy footer...
                            .text("The all seeing eye.") // this footer will have the text "TemmieWebhook!"...
                            .icon_url("https://image.jimcdn.com/app/cms/image/transf/none/path/s6638516af799e8b4/image/i729db7a0d3706cae/version/1523155307/image.png") // with this icon on the footer
                            .build()) // and now we build the footer...
                    .thumbnail(ThumbnailEmbed.builder() // with a fancy thumbnail...
                            .url("https://habbia.es/swf/camara/thumbnails/" + roomId + ".png") // with this thumbnail...
                            .height(320) // not too big because we don't want to flood the user chats with a huge image, right?
                            .build()) // and now we build the thumbnail...
                    .fields(Arrays.asList( // with fields...
                            FieldEmbed.builder().name("**Juego:**").value("Evento de Habbia").inline(true).build(),
                            FieldEmbed.builder().name("**Anfitrión:**").value(client.getPlayer().getData().getUsername() + "\r").inline(true).build(),
                            FieldEmbed.builder().name("**Descripción:**").value(this.merge(params)).build()
                    ))
                    .build(); // and finally, we build the embed

            DiscordMessage dm = DiscordMessage.builder()
                    .username("Habbia Alert") // We are creating a message with the username "Temmie"...
                    .content("") // with no content because we are going to use the embed...
                    .avatarUrl("https://image.jimcdn.com/app/cms/image/transf/none/path/s6638516af799e8b4/image/i729db7a0d3706cae/version/1523155307/image.png") // with this avatar...
                    .embeds(Arrays.asList(de)) // with the our embed...
                    .build(); // and now we build the message!

            discordLog.sendMessage(dm);

    }

    @Override
    public String getPermission() {
        return "eventalert_command";
    }

    @Override
    public String getParameter() {
        return "";
    }

    @Override
    public String getDescription() {
        return Locale.get("command.eventalert.description");
    }
}

package com.cometproject.server.game.commands.user;

import com.cometproject.api.config.CometSettings;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.network.messages.outgoing.notification.MassEventMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.NotificationMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.engine.RoomForwardMessageComposer;
import com.cometproject.server.network.sessions.Session;

public class HelpCommand extends ChatCommand {
    @Override
    public void execute(Session client, String[] params) {
        int time = (int) Comet.getTime();
        int timeSinceLastUpdate = time - client.getPlayer().getLastCFH();

        if(timeSinceLastUpdate >= CometSettings.callForHelpCooldown){
            client.send(new MassEventMessageComposer("help/tour"));
            client.getPlayer().setLastCFH(time);
            return;
        }

        client.send(new NotificationMessageComposer("ambassador", "Todavía debes esperar " + (300 - timeSinceLastUpdate) + " segundos para volver a pedir ayuda.", ""));
    }

    @Override
    public String getPermission() {
        return "help_command";
    }

    @Override
    public String getParameter() {
        return "";
    }

    @Override
    public String getDescription() {
        return Locale.get("command.help.description");
    }
}

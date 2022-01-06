package com.cometproject.server.game.commands.user;

import com.cometproject.api.commands.CommandInfo;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.commands.CommandManager;
import com.cometproject.server.game.permissions.PermissionsManager;
import com.cometproject.server.game.permissions.types.CommandPermission;
import com.cometproject.server.modules.ModuleManager;
import com.cometproject.server.network.messages.outgoing.notification.MotdNotificationMessageComposer;
import com.cometproject.server.network.sessions.Session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class CommandsCommand extends ChatCommand {
    @Override
    public void execute(Session client, String[] params) {
        StringBuilder message = new StringBuilder();
        List<ChatCommand> commands = CommandManager.getInstance().getCommandsForRank(client.getPlayer().getData().getRank());
        message.append("(").append(commands.size()).append("):\r\n");

        for (ChatCommand c : commands) {
            message.append(c.getDescription()).append("\n\n");
        }

        client.send(new MotdNotificationMessageComposer(Locale.get("command.commands.title") + ":\n\n" + Arrays.toString(new String[]{message.toString()})));

    }

    @Override
    public String getPermission() {
        return "commands_command";
    }

    @Override
    public String getParameter() {
        return "";
    }

    @Override
    public String getDescription() {
        return Locale.get("command.commands.description");
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}

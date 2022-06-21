package com.cometproject.server.game.commands.user;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.stats.CometStats;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.GameCycle;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.network.messages.outgoing.notification.WiredAlertMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.utilities.CometRuntime;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;


public class AboutCommand extends ChatCommand {

    @Override
    public void execute(Session client, String message[]) {
        StringBuilder about = new StringBuilder();
        NumberFormat format = NumberFormat.getInstance();

        CometStats cometStats = Comet.getStats();

        boolean aboutDetailed = client.getPlayer().getPermissions().getRank().aboutDetailed();
        boolean aboutStats = client.getPlayer().getPermissions().getRank().aboutStats();

        if (CometSettings.aboutShowRoomsActive || CometSettings.aboutShowUptime || aboutDetailed) {
            about.append("Server Stats:\n\n");

            if (CometSettings.aboutShowPlayersOnline || aboutDetailed)
                about.append("• ONLINES - " + format.format(cometStats.getPlayers()) + "\n");

            if (CometSettings.aboutShowRoomsActive || aboutDetailed)
                about.append("• ROOMS - " + format.format(cometStats.getRooms()) + "\n");

            if (CometSettings.aboutShowUptime || aboutDetailed)
                about.append("— UPTIME - " + cometStats.getUptime() + "\n\n");
        }

        // This will be visible to developers on the manager, no need to display it to the end-user.
        if (client.getPlayer().getPermissions().getRank().getId() > 10) {
            about.append("Server Performance\n");
            about.append("ª Allocated memory - " + format.format(cometStats.getAllocatedMemory()) + "MB\n");
            about.append("ª Used memory - " + format.format(cometStats.getUsedMemory()) + "MB\n");

            about.append("ª Process ID - " + CometRuntime.processId + "\n");
            about.append("ª OS - " + cometStats.getOperatingSystem() + "\n");
            about.append("ª CPU cores -  " + cometStats.getCpuCores() + "\n");
            about.append("ª Threads -  " + ManagementFactory.getThreadMXBean().getThreadCount() + "\n\n");
        }

        about.append("¶ Greetings:\n");
        about.append("µ Custom.\n");
        about.append("µ Leon.\n\n");
        about.append("Also thanks and special mention to Kev, Anis, Ken, Xdr, Finn, Carlos D, Beny, Laynester, NGH, LittleJ, for making this community a better place and helping in any way to make this possible.\n\n");

        about.append("Licensed to HABBIA.\n\n");

        about.append("Hotel Records:\n");
        about.append("Online record - " + GameCycle.getInstance().getOnlineRecord() + "\n");
        about.append("Record since last reboot - " + GameCycle.getInstance().getCurrentOnlineRecord() + "\n");

        client.send(new WiredAlertMessageComposer("Información del servidor:", about.toString()));
    }

    @Override
    public String getPermission() {
        return "about_command";
    }

    @Override
    public String getParameter() {
        return "";
    }

    @Override
    public String getDescription() {
        return Locale.get("command.about.description");
    }
}
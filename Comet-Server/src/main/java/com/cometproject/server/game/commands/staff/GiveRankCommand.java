package com.cometproject.server.game.commands.staff;

import com.cometproject.server.api.DiscordClient;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.players.PlayerManager;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.notification.AlertMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.storage.queries.player.PlayerDao;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;

public class GiveRankCommand extends ChatCommand {
    private String logDesc = "";

    @Override
    public void execute(Session client, String[] params) {
        if (params.length < 2 || !StringUtils.isNumeric(params[1]))
            return;
        String username = params[0];
        int rank = Integer.parseInt(params[1]);

        Session user = NetworkManager.getInstance().getSessions().getByPlayerUsername(username);

        if (user == null) {
            client.getPlayer().sendNotif("error", Locale.getOrDefault("user.not.found", "User is not found or currently offline"));
            return;
        }

        if(username.equals(client.getPlayer().getData().getUsername())) {
            client.getPlayer().sendNotif("error", Locale.getOrDefault("user.himself", "No te puedes dar rango a ti mismo"));
            return;
        }

        if(rank >= client.getPlayer().getData().getRank()) {
            client.getPlayer().sendNotif("error", Locale.getOrDefault("rank.too.high", "You cannot give a rank higher or equal to yours"));
            return;
        }

        user.getPlayer().getData().setRank(rank);
        PlayerDao.updateRank(user.getPlayer().getId(), rank);

        client.getPlayer().sendNotif("success", Locale.getOrDefault("command.giverank.success", "Rank set succesfully!"));
        user.getPlayer().sendNotif("Rank updated", Locale.getOrDefault("command.giverank.received", "Your rank was set to " + rank + ". Please reload client"));

        try {
            DiscordClient dcClient = new DiscordClient("https://discord.com/api/webhooks/932679731206291497/2oSXz_ScY0CHFnUVSgu1MiPXIdWIbIar6_YPAqb9J0u-cGmzNWJEH-qwXpEF5s8-JrKk");
            dcClient.setAvatarUrl("https://i.imgur.com/bA7O9aA.png");
            dcClient.setContent(" El staff " + client.getPlayer().getEntity().getUsername() + " le ha dado rango " + rank + " a " + user.getPlayer().getData().getUsername());
            dcClient.setUsername("Logs Habbia");
            dcClient.execute();
        } catch (IOException e) {
            e.printStackTrace();
    }

        this.logDesc = "%s has given rank %r to user '%u'"
                .replace("%r", Integer.toString(rank))
                .replace("%s", client.getPlayer().getData().getUsername())
                .replace("%u", username);
    }

    @Override
    public String getPermission() {
        return "giverank_command";
    }

    @Override
    public String getParameter() {
        return "%username% %rank%";
    }

    @Override
    public String getDescription() {
        return Locale.get("command.giverank.description");
    }

    @Override
    public String getLoggableDescription(){
        return this.logDesc;
    }

    @Override
    public boolean Loggable(){
        return true;
    }
}

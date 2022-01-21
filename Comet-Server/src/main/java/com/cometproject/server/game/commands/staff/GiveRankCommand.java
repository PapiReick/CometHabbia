package com.cometproject.server.game.commands.staff;

import com.cometproject.server.api.DiscordClient;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.players.PlayerManager;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.notification.AlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.MotdNotificationMessageComposer;
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
                client.send(new MotdNotificationMessageComposer(
                        "Aca tienes la lista de los rangos disponibles en el hotel, usa ;giverank <username> <ID> command.\n\n" +
                                "- [1] Usuario\n" +
                                "- [2] VIP\n" +
                                "- [3] Equipo Creativo\n" +
                                "- [4] Deejay\n" +
                                "- [5] Intermediario\n" +
                                "- [6] Master Trade\n" +
                                "- [7] Publicista\n" +
                                "- [8] Jefe De Publicidad\n" +
                                "- [9] Marketing\n" +
                                "- [10] Room Builder\n" +
                                "- [11] Helper\n" +
                                "- [12] Embajador\n" +
                                "- [13] Game Master\n" +
                                "- [14] Colaboración\n" +
                                "- [15] Coordinador\n" +
                                "- [16] Moderador\n" +
                                "- [17] Supervisor\n" +
                                "- [18] Inversionista (oculto)\n" +
                                "- [19] Economista (oculto)\n" +
                                "- [20] Manager\n" +
                                "- [21] Ceo\n" +
                                "- [22] Desarrollador\n" +
                                "- [23] Fundador"
                ));
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
        user.getPlayer().sendNotif("Rank updated", Locale.getOrDefault("command.giverank.received", "Your rank was set to %r. Please reload client").replace("%r", Integer.toString(rank)));

        try {
            DiscordClient dcClient = new DiscordClient("https://discord.com/api/webhooks/933726180195045396/YapXa9ru4V2g91E7jPdAD9Y0sQXTTN9diINcWt8O96Ug9DsvRzrwMhX3hgbGsa3h_0zD");
            dcClient.setAvatarUrl("https://i.imgur.com/bA7O9aA.png");
            dcClient.setContent(" El staff **" + client.getPlayer().getEntity().getUsername() + "** le ha dado rango `" + rank + "` a **" + user.getPlayer().getData().getUsername() + "** en el hotel");
            dcClient.setUsername("Logs Comandos Habbia");
            dcClient.execute();
        } catch (IOException e) {
            e.printStackTrace();
    }

        this.logDesc = "El staff %s le ha dado rango %r a '%u'"
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

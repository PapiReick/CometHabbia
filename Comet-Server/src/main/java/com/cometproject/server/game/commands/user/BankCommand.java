package com.cometproject.server.game.commands.user;

import com.cometproject.api.config.CometSettings;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.notification.AlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.NotificationMessageComposer;
import com.cometproject.server.network.messages.outgoing.nuxs.NuxGiftEmailViewMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.WhisperMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.engine.RoomForwardMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.storage.queries.player.PlayerDao;


public class BankCommand extends ChatCommand {
    @Override
    public void execute(Session client, String[] params) {
        int m = CometSettings.bankSystemMinimumRequired;

        client.getPlayer().getEntity().setBankType("");
        client.getPlayer().getEntity().setBankSent("");

        if (params.length != 2) {
            sendWhisper(Locale.getOrDefault("command.bank.none", "Introduce un nombre de usuario al que enviar dinero."), client);
            return;
        }

        String user = params[0];
        String type = params[1];

        if (user.equals(client.getPlayer().getData().getUsername())) {
            client.send(new NotificationMessageComposer(Locale.getOrDefault("bank.error.image", "bank_error"), Locale.getOrDefault("command.bank.playerhimself", "No puedes mandarte dinero a ti mismo.")));
            return;
        }

        if(!PlayerDao.usernameIsAvailable(user)) {
            // ALERT THAT USER DOESN'T EXIST
            client.getPlayer().getEntity().setBankType("");
            client.getPlayer().getEntity().setBankSent("");
            client.send(new NotificationMessageComposer(Locale.getOrDefault("bank.error.image", "bank_error"), Locale.getOrDefault("command.bank.existence", "El usuario al que intentas enviar dinero no existe.")));
            return;
        }

        switch (type){
            case "diamantes":
            case "diamonds":
            case "dia":
                if(client.getPlayer().getData().getVipPoints() < m){
                    sendWhisper(Locale.getOrDefault("command.bank.minimum", "No dispones de los diamantes suficientes para enviar dinero, el mínimo para transferir es de " + m + "."), client);
                    return;
                }
                client.getPlayer().getEntity().setBankType("diamonds");
                break;
            case "pdh":
            case "honor":
            case "cacas":
            case "caca":
            case "c":
            case "cacahuates":
                if(!CometSettings.bankSystemSeasonalEnabled){
                    sendWhisper(Locale.getOrDefault("command.bank.disabled", "Actualmente no se encuentra disponible este tipo de transferencia."), client);
                    return;

                }
                if(client.getPlayer().getData().getSeasonalPoints() < m){
                    sendWhisper(Locale.getOrDefault("command.bank.minimum", "No dispones de los Puntos de Honor suficientes para enviar dinero, el mínimo para transferir es de " + m + "."), client);
                    return;
                }
                client.getPlayer().getEntity().setBankType("honor");
                break;
            default:
                client.send(new WhisperMessageComposer(-1, "Por favor coloca el comando correctamente. <i>Ejemplo: :transfer Custom diamantes</i>.", 1));
                return;
        }


        client.getPlayer().getEntity().setBankSent(user);
        client.send(new NuxGiftEmailViewMessageComposer(m + "", 0, true, false, true));
    }

    @Override
    public String getPermission() {
        return "bank_command";
    }

    @Override
    public String getParameter() {
        return Locale.getOrDefault("command.parameter.username", "%username%");
    }

    @Override
    public String getDescription() {
        return Locale.get("command.bank.description");
    }
}

package com.cometproject.server.game.rooms.objects.items.types.floor;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.games.GameType;
import com.cometproject.server.game.rooms.types.components.games.RoomGame;
import com.cometproject.server.game.rooms.types.components.games.casino.CasinoGame;
import com.cometproject.server.network.messages.outgoing.notification.MassEventMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.NotificationMessageComposer;
import com.cometproject.server.network.messages.outgoing.nuxs.NuxGiftEmailViewMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.newyear.NewYearResolutionMessageComposer;

public class RoulletteMachineFloorItem extends RoomItemFloor {

    public RoulletteMachineFloorItem(RoomItemData itemData, Room room) {
        super(itemData, room);
    }

    @Override
    public boolean onInteract(RoomEntity entity, int requestData, boolean isWiredTrigger) {
        if (isWiredTrigger) {
            return false;
        }

        PlayerEntity p = ((PlayerEntity) entity);

        if(p == null)
        return true;

        if(p.getBetAmount() == 0){
            p.getPlayer().getSession().send(new MassEventMessageComposer("habbopages/users/roullette.txt?" + Comet.getTime()));
            p.getPlayer().getSession().send(new NuxGiftEmailViewMessageComposer(6 + "", 0, true, false, true));
            return true;
        }

        if(p.getBetAmount() > p.getPlayer().getData().getBlackMoney()){
            p.getPlayer().getSession().send(new NotificationMessageComposer("inters", Locale.getOrDefault("casino.missing.currency", "¡No tienes suficientes Tokens para realizar la apuesta!\n\nTienes %currency% Tokens y la apuesta en %bet%.").replace("%currency%", p.getPlayer().getData().getBlackMoney() + "").replace("%bet%", p.getPlayer().getEntity().getBetAmount() + "")));
            return true;
        }

        // Game starts if it's not in progress.
        if (p.getRoom().getGame().getInstance() == null) {
            p.getRoom().getGame().createNew(GameType.CASINO);
            p.getRoom().getGame().getInstance().startTimer(60);
        }

        final RoomGame game = p.getRoom().getGame().getInstance();

        if (!(game instanceof CasinoGame)) { return true; }
        p.getPlayer().getSession().send(new NewYearResolutionMessageComposer(p.getPlayer().getEntity().getBetRow()));
        return true;
    }

    @Override
    public void onPlaced() {
    }

    @Override
    public void onTickComplete() {
    }
}
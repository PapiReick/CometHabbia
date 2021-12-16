package com.cometproject.server.network.messages.outgoing.user.club;

import com.cometproject.api.networking.messages.IComposer;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.players.components.SubscriptionComponent;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.Calendar;


public class ClubDataMessageComposer extends MessageComposer {
    private final SubscriptionComponent subscriptionComponent;
    private final int windowId;

    public ClubDataMessageComposer(final SubscriptionComponent subscriptionComponent, final int windowId) {
        this.subscriptionComponent = subscriptionComponent;
        this.windowId = windowId;
    }

    @Override
    public short getId() {
        return Composers.ClubDataMessageComposer;
    }

    @Override
    public void compose(IComposer msg) {
        msg.writeInt(1); // size

            msg.writeInt(0);
            msg.writeString("DEAL_VIP_1");
            msg.writeBoolean(false);
            msg.writeInt(0);
            msg.writeInt(240);
            msg.writeInt(5);
            msg.writeBoolean(true);

        long seconds = 31 * 86400L;

        long secondsTotal = seconds;

        int totalYears = (int) Math.floor((int) seconds / 86400 * 31 * 12);
        seconds -= totalYears * 86400 * 31 * 12;

        int totalMonths = (int) Math.floor((int) seconds / 86400 * 31);
        seconds -= totalMonths * 86400 * 31;

        int totalDays = (int) Math.floor((int) seconds / 86400);
        seconds -= totalDays * 86400;

        msg.writeInt((int) secondsTotal / 86400 / 31);
        msg.writeInt((int) seconds);
        msg.writeBoolean(false); //giftable
        msg.writeInt((int) seconds);

        int endTimestamp = this.subscriptionComponent.getExpire();

        if (endTimestamp < (int)Comet.getTime()) {
            endTimestamp = (int)Comet.getTime();
        }

        endTimestamp += secondsTotal;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(endTimestamp * 1000L);
        msg.writeInt(cal.get(Calendar.YEAR));
        msg.writeInt(cal.get(Calendar.MONTH) + 1);
        msg.writeInt(cal.get(Calendar.DAY_OF_MONTH));

        msg.writeInt(this.windowId);
    }
}

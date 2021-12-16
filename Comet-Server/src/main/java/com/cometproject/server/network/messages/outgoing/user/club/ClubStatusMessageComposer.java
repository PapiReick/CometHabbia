package com.cometproject.server.network.messages.outgoing.user.club;

import com.cometproject.api.networking.messages.IComposer;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.players.components.SubscriptionComponent;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;


public class ClubStatusMessageComposer extends MessageComposer {
    private final SubscriptionComponent subscriptionComponent;

    public ClubStatusMessageComposer(final SubscriptionComponent subscriptionComponent) {
        this.subscriptionComponent = subscriptionComponent;
    }

    @Override
    public short getId() {
        return Composers.ScrSendUserInfoMessageComposer;
    }

    @Override
    public void compose(IComposer msg) {

        int timeLeft = this.subscriptionComponent.getTimeLeft();
        long timeLeftLong = timeLeft * 1000;

        int days = this.subscriptionComponent.getDaysLeft();
        int years = this.subscriptionComponent.getYearsLeft();
        int minutes = this.subscriptionComponent.getMinutesLeft();

        minutes = Math.max(minutes, 0);

        //System.out.println("SUBSCRIPTION DATA:\nDAYS: " + days + "\nMINUTES: " + minutes + "\nYEARS:" + years + "\nTIMELEFT: " + timeLeft + "\n");

        int months = 0;

        if (days > 31) {
            months = (int) Math.floor(days / 31);
            days = days - (months * 31);
        }

        months = days / 31;
         /*else {
            if (subscriptionComponent.exists()) {
                subscriptionComponent.delete();
            }
        }*/

        msg.writeString("habbo_club");

        msg.writeInt(this.subscriptionComponent.isValid() ? days : 0);
        msg.writeInt(0);
        msg.writeInt(0);
        msg.writeInt(0);
        msg.writeBoolean(this.subscriptionComponent.isValid());
        msg.writeBoolean(this.subscriptionComponent.isValid());
        msg.writeInt(this.subscriptionComponent.isValid() ? 1 : 0);
        msg.writeInt(minutes);
        msg.writeInt((int)timeLeftLong);
    }
}

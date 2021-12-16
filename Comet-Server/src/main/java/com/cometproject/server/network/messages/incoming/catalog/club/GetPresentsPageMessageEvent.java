package com.cometproject.server.network.messages.incoming.catalog.club;

import com.cometproject.server.game.catalog.CatalogManager;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.user.club.CatalogGiftsPageMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.club.ClubPresentsNotificationMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;


public class GetPresentsPageMessageEvent implements Event {
    public void handle(Session client, MessageEvent msg) {
        int pageId = 153;
        /*if(client.getPlayer().getSubscription().getPresents() > 0){
            client.send(new ClubPresentsNotificationMessageComposer(1));
        }*/

        if (CatalogManager.getInstance().pageExists(pageId) && CatalogManager.getInstance().getPage(pageId).isEnabled()) {
            client.send(new CatalogGiftsPageMessageComposer(CatalogManager.getInstance().getPage(pageId), client.getPlayer().getSubscription()));
        }
    }
}
package com.cometproject.server.network.messages.incoming.catalog;

import com.cometproject.server.composers.catalog.CatalogPageMessageComposer;
import com.cometproject.server.game.catalog.CatalogManager;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;


public class GetCataPageMessageEvent implements Event {
    public void handle(Session client, MessageEvent msg) {
        int pageId = msg.readInt();
        int junk = msg.readInt();
        String pageMode = msg.readString();

        if (client.getPlayer().cancelPageOpen) {
            client.getPlayer().cancelPageOpen = false;
            return;
        }

        if (CatalogManager.getInstance().pageExists(pageId) && CatalogManager.getInstance().getPage(pageId).isEnabled()) {
            if(pageId == 84){ // GROUP TYPE BYPASS NOT EXPLOITABLE BY TILE
                client.getPlayer().setGroupCreationType(0);
            }
            client.send(new CatalogPageMessageComposer(pageMode, CatalogManager.getInstance().getPage(pageId), client.getPlayer(), CatalogManager.getInstance()));
        }
    }
}

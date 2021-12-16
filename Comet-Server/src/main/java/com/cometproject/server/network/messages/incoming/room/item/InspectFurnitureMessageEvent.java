package com.cometproject.server.network.messages.incoming.room.item;

import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;


public class InspectFurnitureMessageEvent implements Event {
    @Override
    public void handle(Session client, MessageEvent msg) throws Exception {
        String data = msg.readString();

        //client.getPlayer().sendBubble("catalogue", "Item selected:\n" + data);
    }
}
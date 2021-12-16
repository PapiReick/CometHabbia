package com.cometproject.server.network.messages.incoming.handshake;

import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;


public class CheckReleaseMessageEvent implements Event {
    @Override
    public void handle(Session client, MessageEvent msg) {
        final int test = msg.readInt();
        final String release = msg.readString();


        /*if (!release.equals(CometServer.CLIENT_VERSION)) {
            client.getLogger().warn("Client connected with incorrect client version (" + release + ") and was disposed");
            client.disconnect();
        }*/
    }
}
package com.cometproject.server.network.messages.incoming.gamecenter;

import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.gamecenter.Game2WeeklyLeaderboardParser;
import com.cometproject.server.network.messages.outgoing.gamecenter.GameCenterAchievementsConfigurationComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;

public class GetGameLeaderboardsEvent implements Event {
    @Override
    public void handle(Session client, MessageEvent msg) throws Exception {
        final int gameId = msg.readInt();

        client.send(new GameCenterAchievementsConfigurationComposer(gameId, client.getPlayer().getAchievements()));
        //client.send(new Game2WeeklyLeaderboardParser(gameId, client.getPlayer().getData().getId()));
    }
}

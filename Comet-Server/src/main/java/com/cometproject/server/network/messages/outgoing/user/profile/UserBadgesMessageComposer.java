package com.cometproject.server.network.messages.outgoing.user.profile;

import com.cometproject.api.networking.messages.IComposer;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UserBadgesMessageComposer extends MessageComposer {
    private final int playerId;
    private final Map<String, Integer> badges;

    public UserBadgesMessageComposer(final int playerId, final Map<String, Integer> badges) {
        this.playerId = playerId;
        this.badges = badges;
    }

    @Override
    public short getId() {
        return Composers.HabboUserBadgesMessageComposer;
    }

    @Override
    public void compose(IComposer msg) {
        msg.writeInt(playerId);
        msg.writeInt(badges.size());

        final Map<String, Integer> sortedByCount =
                badges.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


        sortedByCount.forEach((badge, slot) -> {
            if (slot > 0) {
                msg.writeInt(slot);
                msg.writeString(badge);
            }
        });
    }
}

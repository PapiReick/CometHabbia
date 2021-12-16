package com.cometproject.storage.mysql.models.factories;

import com.cometproject.api.game.groups.types.GroupType;
import com.cometproject.api.game.groups.types.IGroupData;
import com.cometproject.api.game.players.data.PlayerAvatar;
import com.cometproject.storage.mysql.models.GroupData;

public class GroupDataFactory {

    public IGroupData create(int id, int category, String title, String description, String tag, String badge, int ownerId, String ownerName,
                             int roomId, int created, GroupType type, int colourA, int colourB,
                             boolean canMembersDecorate, boolean hasForum, int currency, PlayerAvatar playerAvatar) {
        return new GroupData(id, category, title, description, tag, badge, ownerId, ownerName, roomId, created, type, colourA, colourB,
                canMembersDecorate, hasForum, currency, playerAvatar);
    }

    public IGroupData create(String title, int category, String description, String tag, String badge, int ownerId,
                             String ownerName, int roomId, int colourA, int colourB, PlayerAvatar playerAvatar) {
        return new GroupData(-1, category, title, description, tag, badge, ownerId, ownerName, roomId, (int) (System.currentTimeMillis() / 1000), GroupType.REGULAR, colourA, colourB, false, false, 0, playerAvatar);
    }
}

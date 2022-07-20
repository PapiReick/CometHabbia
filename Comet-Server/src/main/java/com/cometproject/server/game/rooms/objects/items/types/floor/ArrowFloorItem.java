package com.cometproject.server.game.rooms.objects.items.types.floor;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.utilities.collections.ConcurrentHashSet;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class ArrowFloorItem extends RoomItemFloor {
    private final Set<Integer> skippedEntities = Sets.newConcurrentHashSet();
    private final Set<Integer> skippedItems = Sets.newConcurrentHashSet();
    private final Set<RoomEntity> movedEntities = new ConcurrentHashSet<>();

    public ArrowFloorItem (RoomItemData itemData, Room room) {
        super(itemData, room);
    }

    @Override
    public void onEntityStepOn(RoomEntity entity) {
        Position sqInfront = this.getPosition().squareInFront(this.getRotation());

        if (!this.getRoom().getMapping().isValidPosition(sqInfront)) {
            return;
        }

        List<RoomEntity> entities = this.getRoom().getEntities().getEntitiesAt(this.getPosition());

        for (RoomEntity entity2 : entities) {
            if (entity2.getPosition().getX() != this.getPosition().getX() && entity2.getPosition().getY() != this.getPosition().getY()) {
                continue;
            }

            if (this.skippedEntities.contains(entity2.getId())) {
                continue;
            }

            if (entity2.getPositionToSet() != null) {
                continue;
            }

            if (entity2.isWalking()) {
                continue;
            }

            if (sqInfront.getX() == this.getRoom().getModel().getDoorX() && sqInfront.getY() == this.getRoom().getModel().getDoorY()) {
                entity2.leaveRoom(false, false, true);
                continue;
            }


            entity2.moveTo(sqInfront.getX(), sqInfront.getY());

            entity2.unIdle();
            entity2.resetAfkTimer();
            entity2.markNeedsUpdate(true);

            this.onEntityStepOff(entity2);
            movedEntities.add(entity2);

        }
    }
}
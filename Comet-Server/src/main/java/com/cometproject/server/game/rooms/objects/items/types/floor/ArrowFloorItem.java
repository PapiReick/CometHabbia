package com.cometproject.server.game.rooms.objects.items.types.floor;

import com.cometproject.api.game.quests.QuestType;
import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
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
    public boolean onInteract(RoomEntity entity, int requestData, boolean isWiredTrigger) {
        if (!isWiredTrigger) {
            if (!(entity instanceof PlayerEntity)) {
                return false;
            }

            PlayerEntity pEntity = (PlayerEntity) entity;

            if (this.getDefinition().requiresRights()) {
                if (!pEntity.getRoom().getRights().hasRights(pEntity.getPlayerId()) && !pEntity.getPlayer().getPermissions().getRank().roomFullControl()) {
                    return false;
                }
            }

            if (pEntity.getPlayer().getId() == this.getRoom().getData().getOwnerId()) {
                pEntity.getPlayer().getQuests().progressQuest(QuestType.FURNI_SWITCH);
            }
        }

        this.toggleInteract(true);
        this.sendUpdate();

        this.saveData();
        return true;
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
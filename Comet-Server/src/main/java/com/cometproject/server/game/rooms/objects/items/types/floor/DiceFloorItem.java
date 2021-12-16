package com.cometproject.server.game.rooms.objects.items.types.floor;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFactory;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.misc.ChatEmotion;
import com.cometproject.server.network.messages.outgoing.room.avatar.TalkMessageComposer;

import java.util.Arrays;
import java.util.Random;


public class DiceFloorItem extends RoomItemFloor {
    private boolean isInUse = false;
    private RoomEntity r = null;
    private int rigNumber = -1;

    public DiceFloorItem(RoomItemData itemData, Room room) {
        super(itemData, room);
    }

    @Override
    public boolean onInteract(RoomEntity entity, int requestData, boolean isWiredTrigger) {
        if (!isWiredTrigger) {
            if (!this.getPosition().touching(entity.getPosition())) {
                entity.moveTo(this.getPosition().squareInFront(this.getRotation()).getX(), this.getPosition().squareBehind(this.getRotation()).getY());
                return false;
            }
        }

        if (this.isInUse) {
            return false;
        }

        this.r = entity;

        if (requestData >= 0) {
            if (!"-1".equals(this.getItemData().getData())) {
                this.getItemData().setData("-1");
                this.sendUpdate();

                this.isInUse = true;

                if (entity != null) {
                    if (entity.hasAttribute("diceRoll")) {
                        this.rigNumber = (int) entity.getAttribute("diceRoll");
                        entity.removeAttribute("diceRoll");
                    }
                }

                this.setTicks(RoomItemFactory.getProcessTime(1.5));
            }
        } else {
            this.getItemData().setData("0");
            this.sendUpdate();

            this.saveData();
        }

        return true;
    }

    @Override
    public void onPlaced() {
        if (!"0".equals(this.getItemData().getData())) {
            this.getItemData().setData("0");
        }
    }

    @Override
    public void onPickup() {
        this.cancelTicks();
    }

    @Override
    public void onTickComplete() {
        int num = new Random().nextInt(6) + 1;

        this.getItemData().setData(Integer.toString(this.rigNumber == -1 ? num : this.rigNumber));
        this.sendUpdate();

        this.saveData();

        if (this.rigNumber != -1) this.rigNumber = -1;

        if(Arrays.asList(this.getRoom().getData().getTags()).contains("21"))
        verifyCount(num);

        this.isInUse = false;
        this.r = null;
    }

    public void verifyCount(int c){
        this.r.addDiceCount(c);

        String messInfo = "Llevo <b><font size='14'>" + this.r.getDiceCount() + "</font></b> en mi tirada. <i>( " + (this.r.getDiceCount() - Integer.parseInt(this.getDataObject())) + " + " + Integer.parseInt(this.getDataObject()) + "</i> )";

        if(this.r.getDiceCount() > 21){
            messInfo = "<font color='#cc1445'>Saqué <b><font size='14'>" + this.r.getDiceCount() + "</font></b> en mi tirada, y yo volé de él, y el dado voló hacia la arbolada.</font>";
            this.r.resetDiceCount();
        }

        if(this.r.getDiceCount() == 21){
            messInfo = "<font color='#13cc60'>Saqué <b><font size='14'>" + this.r.getDiceCount() + "</font></b> en mi tirada.</font>";
            this.r.resetDiceCount();
        }

        this.getRoom().getEntities().broadcastMessage(new TalkMessageComposer(this.r.getId(), messInfo, ChatEmotion.NONE, 26));
    }
}

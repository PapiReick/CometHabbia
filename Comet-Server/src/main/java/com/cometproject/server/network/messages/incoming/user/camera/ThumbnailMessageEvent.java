package com.cometproject.server.network.messages.incoming.user.camera;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.server.api.ApiClient;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.room.engine.RoomDataMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.settings.ThumbnailTakenMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ThumbnailMessageEvent implements Event {
    @Override
    public void handle(Session client, MessageEvent msg) throws Exception {
        if (!client.getPlayer().getEntity().getRoom().getRights().hasRights(client.getPlayer().getId())
                && !client.getPlayer().getPermissions().getRank().roomFullControl() && !client.getPlayer().getRentable().hasRent()) {
            return;
        }

        final int length = msg.readInt(); // client sends [length:4][header:2][imageSize:4][payload:imageSize]
        final byte[] payload = msg.readBytes(length);

        final UUID imageId = UUID.randomUUID();
        final IRoomData roomData = client.getPlayer().getEntity().getRoom().getData();

        if (RenderRoomMessageEvent.isPngFile(payload)) {
            try {
                ByteBuf test = Unpooled.copiedBuffer(payload);
                BufferedImage image = ImageIO.read(new ByteBufInputStream(test));

                ImageIO.write(image, "png", new File(CometSettings.thumbnailUploadUrl + roomData.getId() + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
            }

            roomData.setThumbnail("camera/thumbnails/" + roomData.getId() + ".png");

            GameContext.getCurrent().getRoomService().saveRoomData(roomData);
//
            // Save image URL in database
//        client.getPlayer().sendMotd(imageId.toString());
            client.send(new RoomDataMessageComposer(client.getPlayer().getEntity().getRoom()));
            client.send(new ThumbnailTakenMessageComposer());
        }
    }
}

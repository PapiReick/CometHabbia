package com.cometproject.server.network.messages.incoming.user.camera;

import com.cometproject.api.config.CometSettings;
import com.cometproject.server.api.ApiClient;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.composers.camera.PhotoPreviewMessageComposer;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;
import com.cometproject.storage.api.StorageContext;
import com.google.common.collect.BiMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class RenderRoomMessageEvent implements Event {
    @Override
    public void handle(Session client, MessageEvent msg) throws Exception {
        final int length = msg.readInt();
        byte[] payload = msg.readBytes(length);

        int timestamp = (int) Comet.getTime();

        System.out.println("TAMAÑO : " + length);

        String URL = client.getPlayer().getData().getId() + "_" + timestamp + ".png";
        String URL_small = client.getPlayer().getData().getId() + "_" + timestamp + "_small.png";
        String base = CometSettings.cameraPhotoUrl.replace("%photoId%", "");
        client.getPlayer().setLastPhoto(URL);


        if (isPngFile(payload)) {
            try {
                ByteBuf test = Unpooled.copiedBuffer(payload);
                BufferedImage image = ImageIO.read(new ByteBufInputStream(test));

                ImageIO.write(image, "png", new File(CometSettings.cameraUploadUrl + URL));
                ImageIO.write(image, "png", new File(CometSettings.cameraUploadUrl + URL_small));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
            }

            client.send(new PhotoPreviewMessageComposer(URL));
        }


    }

    private static final byte[] signature = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};

    public static boolean isPngFile(byte[] file) {
        return Arrays.equals(Arrays.copyOfRange(file, 0, 8), signature);
    }
}

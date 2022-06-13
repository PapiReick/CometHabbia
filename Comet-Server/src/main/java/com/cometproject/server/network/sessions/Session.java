package com.cometproject.server.network.sessions;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.networking.messages.IMessageComposer;
import com.cometproject.api.networking.sessions.ISession;
import com.cometproject.games.snowwar.data.SnowWarPlayerData;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.moderation.ModerationManager;
import com.cometproject.server.game.players.PlayerManager;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.game.rooms.types.components.games.RoomGame;
import com.cometproject.server.game.rooms.types.components.games.survival.SurvivalGame;
import com.cometproject.server.game.rooms.types.components.games.survival.types.SurvivalPlayer;
import com.cometproject.server.game.rooms.types.components.games.survival.types.SurvivalQueue;
import com.cometproject.server.network.messages.outgoing.notification.LogoutMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.AvatarUpdateMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.items.UpdateFloorItemMessageComposer;
import com.cometproject.server.protocol.messages.MessageEvent;
import com.cometproject.server.protocol.security.exchange.DiffieHellman;
import com.cometproject.server.storage.cache.CachableObject;
import com.cometproject.server.storage.queries.player.PlayerDao;
import com.corundumstudio.socketio.SocketIOClient;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.UUID;


public class Session implements ISession {
    public static int CLIENT_VERSION = 0;
    private final ChannelHandlerContext channel;
    private final UUID uuid = UUID.randomUUID();
    private Logger logger = LogManager.getLogger("Session");
    private SessionEventHandler eventHandler;
    private boolean isClone = false;
    private int loginAt = 0;
    private String uniqueId = "";
    private Player player;
    private boolean disconnectCalled = false;
    public SnowWarPlayerData snowWarPlayerData;

    private ChannelHandlerContext wsChannel;

    private DiffieHellman diffieHellman;
    private long lastPing = Comet.getTime();

    public Session(ChannelHandlerContext channel) {
        this.channel = channel;
    }

    public void initialise() {
        this.eventHandler = new SessionEventHandler(this);
    }

    public void onDisconnect() {
        if (this.disconnectCalled) {
            return;
        }

        this.disconnectCalled = true;

        PlayerManager.getInstance().getPlayerLoadExecutionService().submit(() -> {
            try {
                if (player != null && player.getData() != null)
                    PlayerManager.getInstance().remove(player.getId(), player.getData().getUsername(), this.channel.attr(SessionManager.CHANNEL_ID_ATTR).get(), this.getIpAddress());

                this.eventHandler.dispose();

                if (this.player != null) {
                    if (this.getPlayer().getPermissions().getRank().modTool()) {
                        ModerationManager.getInstance().removeModerator(this);
                    }

                    if (this.getPlayer().getPermissions().getRank().messengerLogChat()) {
                        ModerationManager.getInstance().removeLogChatUser(this);
                    }

                    if (this.getPlayer().getPermissions().getRank().messengerAlfaChat()) {
                        ModerationManager.getInstance().removeAlfaChatUser(this);
                    }

                    if(this.getPlayer().getSurvivalRoomId() > 0) {
                        SurvivalQueue.getInstance().removePlayerFromQueue(this.getPlayer().getSurvivalRoomId(), this.getPlayer().getId(), this.getPlayer().getQueueData());
                    }

                    if(this.getPlayer().getEntity() != null && this.getPlayer().getEntity().isSurvivalMode()) {
                        final RoomGame game = this.getPlayer().getEntity().getRoom().getGame().getInstance();

                        if (!(game instanceof SurvivalGame))
                            return;

                        final SurvivalGame survivalGame = (SurvivalGame) game;
                        final SurvivalPlayer survivalPlayer = survivalGame.survivalPlayer(this.getPlayer().getData().getId());
                        if (survivalPlayer != null) {
                            ((SurvivalGame) game).playerLeaves(this.getPlayer().getData().getId(), true);
                        }
                    }

                    try {
                        this.getPlayer().dispose();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                this.setPlayer(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void disconnect() {
        this.onDisconnect();

        this.getChannel().disconnect();
    }

    public String getIpAddress() {
        String ipAddress = "0.0.0.0";

        if (this.player == null || !CometSettings.useDatabaseIp) {
            return ((InetSocketAddress) this.getChannel().channel().remoteAddress()).getAddress().getHostAddress();
        } else {
            if (this.getPlayer() != null) {
                ipAddress = PlayerDao.getIpAddress(this.getPlayer().getId());
            }
        }

        return ipAddress;
    }

    public void setLoginAt(int loginAt) {
        this.loginAt = loginAt;
    }

    public int getLoginAt() {
        return loginAt;
    }

    public void disconnect(String reason) {
        this.send(new LogoutMessageComposer(reason));
        this.disconnect();
    }

    public void handleMessageEvent(MessageEvent msg) {
        this.eventHandler.handle(msg);
    }

    public Session sendQueue(final IMessageComposer msg) {
        return this.send(msg, true);
    }

    public Session send(IMessageComposer msg) {
        return this.send(msg, false);
    }

    public Session send(IMessageComposer msg, boolean queue) {
        if (msg == null) {
            return this;
        }

        if (msg.getId() == 0) {
            logger.debug("Unknown header ID for message: " + msg.getClass().getSimpleName());
        }

        if (!(msg instanceof AvatarUpdateMessageComposer) && !(msg instanceof UpdateFloorItemMessageComposer))
            logger.debug("Sent message: " + msg.getClass().getSimpleName() + " / " + msg.getId());

        if (!queue) {
            this.channel.writeAndFlush(msg, channel.voidPromise());
        } else {
            this.channel.write(msg);
        }
        return this;
    }

    @Override
    public void flush() {
        this.channel.flush();
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        if (player == null || player.getData() == null) {
            return;
        }

        String username = player.getData().getUsername();

        this.logger = LogManager.getLogger("[" + username + "][" + player.getId() + "]");
        this.player = player;
        this.snowWarPlayerData = new SnowWarPlayerData(player);

        int channelId = this.channel.attr(SessionManager.CHANNEL_ID_ATTR).get();

        PlayerManager.getInstance().put(player.getId(), channelId, username, this.getIpAddress());

        if (player.getPermissions().getRank().modTool()) {
            ModerationManager.getInstance().addModerator(player.getSession());
        }

        if (player.getPermissions().getRank().messengerAlfaChat()) {
            ModerationManager.getInstance().addAlfa(player.getSession());
        }

        if (player.getPermissions().getRank().messengerLogChat()) {
            ModerationManager.getInstance().addLogChatUser(player.getSession());
        }
    }

    public ChannelHandlerContext getChannel() {
        return this.channel;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public UUID getSessionId() {
        return uuid;
    }

    public DiffieHellman getDiffieHellman() {
        if (this.diffieHellman == null) {
            this.diffieHellman = new DiffieHellman();
        }

        return diffieHellman;
    }

    public long getLastPing() {
        return lastPing;
    }

    public void setLastPing(long lastPing) {
        this.lastPing = lastPing;
    }

    public ChannelHandlerContext getWsChannel() {
        return wsChannel;
    }

    public void setWsChannel(ChannelHandlerContext wsChannel) {
        this.wsChannel = wsChannel;
    }
}
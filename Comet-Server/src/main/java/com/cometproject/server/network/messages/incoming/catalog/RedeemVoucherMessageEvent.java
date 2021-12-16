package com.cometproject.server.network.messages.incoming.catalog;

import com.cometproject.api.game.catalog.types.vouchers.VoucherStatus;
import com.cometproject.api.game.furniture.types.FurnitureDefinition;
import com.cometproject.api.game.players.IPlayer;
import com.cometproject.api.game.players.data.components.inventory.PlayerItem;
import com.cometproject.server.composers.catalog.UnseenItemsMessageComposer;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.catalog.CatalogManager;
import com.cometproject.server.game.catalog.types.Voucher;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.players.components.types.inventory.InventoryItem;
import com.cometproject.server.game.rooms.bundles.RoomBundleManager;
import com.cometproject.server.game.rooms.bundles.types.RoomBundle;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.notification.AdvancedAlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.UpdateInventoryMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;
import com.cometproject.server.storage.queries.catalog.VoucherDao;
import com.cometproject.server.storage.queries.items.ItemDao;
import com.cometproject.storage.api.StorageContext;
import com.cometproject.storage.api.data.Data;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RedeemVoucherMessageEvent implements Event {
    @Override
    public void handle(Session client, MessageEvent msg) throws Exception {
        final String voucherCode = msg.readString();

        if (client.getPlayer().getVoucherRedeemAttempts() >= 20 &&
                (client.getPlayer().getLastVoucherRedeemAttempt() + 120) > (System.currentTimeMillis() / 1000)) {
            return;
        }

        client.getPlayer().setVoucherRedeemAttempts(client.getPlayer().getVoucherRedeemAttempts() + 1);
        client.getPlayer().setLastVoucherRedeemAttempt((int) System.currentTimeMillis() / 1000);

        final Voucher voucher = VoucherDao.findVoucherByCode(voucherCode);

        if (voucher == null) {
            client.getPlayer().sendMotd(Locale.getOrDefault("voucher.error.doesnt_exist", "The voucher you entered doesn't exist!"));
            return;
        }

        if (voucher.getStatus() == VoucherStatus.CLAIMED) {
            client.getPlayer().sendMotd(Locale.getOrDefault("voucher.error.claimed", "The voucher you entered has already been claimed!"));
            return;
        }

        List<Integer> playersId = new ArrayList<>();
        if(!voucher.getClaimedBy().isEmpty()) {
            if(!voucher.getClaimedBy().contains(",")) {
                playersId.add(new Integer(voucher.getClaimedBy()));
            } else {
                for (String playerId : voucher.getClaimedBy().split(",")) {
                    playersId.add(new Integer(playerId));
                }
            }

            if(playersId.contains(client.getPlayer().getId())) {
                client.getPlayer().sendMotd(Locale.get("Ya has canjeado este código una vez."));
                return;
            }
        }

        playersId.add(client.getPlayer().getId());

        boolean failure = false;

        switch (voucher.getType()) {
            case COINS: {
                if (!StringUtils.isNumeric(voucher.getData())) {
                    failure = true;
                    break;
                }

                final int coinAmount = Integer.parseInt(voucher.getData());

                client.getPlayer().getData().increaseCredits(coinAmount);
                client.getPlayer().getData().save();
                client.send(client.getPlayer().composeCreditBalance());
                client.send(new AdvancedAlertMessageComposer(Locale.get("command.coins.title"), Locale.get("command.coins.received").replace("%amount%", String.valueOf(coinAmount))));
                break;
            }

            case DUCKETS: {
                if (!StringUtils.isNumeric(voucher.getData())) {
                    failure = true;
                    break;
                }

                final int ducketAmount = Integer.parseInt(voucher.getData());

                client.getPlayer().getData().increaseActivityPoints(ducketAmount);
                client.getPlayer().getData().save();
                client.send(client.getPlayer().composeCurrenciesBalance());
                client.send(new AdvancedAlertMessageComposer(Locale.get("command.duckets.successtitle"),
                        Locale.get("command.duckets.successmessage").replace("%amount%", String.valueOf(ducketAmount))
                ));
                break;
            }

            case VIP_POINTS: {
                if (!StringUtils.isNumeric(voucher.getData())) {
                    failure = true;
                    break;
                }

                final int vipPointAmount = Integer.parseInt(voucher.getData());

                client.getPlayer().getData().increaseVipPoints(vipPointAmount);
                client.getPlayer().getData().save();
                client.send(client.getPlayer().composeCurrenciesBalance());
                client.send(new AdvancedAlertMessageComposer(
                        Locale.get("command.points.successtitle"),
                        Locale.get("command.points.successmessage").replace("%amount%", String.valueOf(vipPointAmount))
                ));
                break;
            }

            case ITEM: {
                int id = ItemDao.getItemByName(voucher.getData());
                FurnitureDefinition itemDefinition = ItemManager.getInstance().getDefinition(id);
                IPlayer e = client.getPlayer();

                if (itemDefinition != null) {
                    final Data<Long> newItem = Data.createEmpty();
                    StorageContext.getCurrentContext().getRoomItemRepository().createItem(e.getData().getId(), id, "", newItem::set);
                    PlayerItem playerItem = new InventoryItem(newItem.get(), id, "");
                    client.getPlayer().getInventory().addItem(playerItem);
                    client.getPlayer().getSubscription().decrementPresents(client.getPlayer().getData().getId());
                    e.getSession().send(new UpdateInventoryMessageComposer());
                    e.getSession().send(new UnseenItemsMessageComposer(Sets.newHashSet(playerItem), ItemManager.getInstance()));
                }
                break;
            }

            case ROOM_BUNDLE: {
                RoomBundle roomBundle = RoomBundleManager.getInstance().getBundle(voucher.getData());

                CatalogManager.getInstance().getPurchaseHandler().purchaseBundle(roomBundle, client);
                break;
            }
        }

        if (failure) {
            client.getPlayer().sendMotd(Locale.getOrDefault("voucher.error", "The voucher was redeemed unsuccessfully"));

            client.getPlayer().getData().save();
            client.getPlayer().sendBalance();
        } else {
            client.getPlayer().sendMotd(Locale.getOrDefault("voucher.success", "The voucher was redeemed successfully"));
        }

        VoucherDao.claimVoucher(voucher.getId(), client.getPlayer().getId(), voucher.getLimitUse() - 1 == 0);
        client.getPlayer().setVoucherRedeemAttempts(0);
    }
}

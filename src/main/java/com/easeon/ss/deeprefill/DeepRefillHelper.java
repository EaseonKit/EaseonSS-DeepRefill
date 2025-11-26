package com.easeon.ss.deeprefill;

import com.easeon.ss.core.api.definitions.enums.InventoryType;
import com.easeon.ss.core.helper.ItemHelper;
import com.easeon.ss.core.util.system.EaseonLogger;
import com.easeon.ss.core.wrapper.EaseonItem;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;

public class DeepRefillHelper {
    private static final EaseonLogger logger = EaseonLogger.of();

    public static boolean isConsumptionChanged(EaseonItem before, EaseonItem after) {
        return before.not(after.getItem()) && (
               before.of(Items.GLASS_BOTTLE, Items.BUCKET, Items.WATER_BUCKET, Items.BOWL, Items.MAP) ||
               after.of(Items.GLASS_BOTTLE, Items.BUCKET, Items.WATER_BUCKET, Items.BOWL)
            );
    }

    /// 손에 들고 있는 아이템 재충전 (BlockUse, ItemUse용)
    public static void tryRefillItem(EaseonPlayer player, EaseonItem usedItem, EaseonItem item, Hand hand) {
        EaseonItem found = null;
        if (InventoryType.of(Easeon.instance.config.value, InventoryType.PLAYER))
            found = searchInInventory(player.getInventory(), usedItem);

        if (found == null && InventoryType.of(Easeon.instance.config.value, InventoryType.SHULKER_BOX))
            found = searchInShulkerBoxes(player.getInventory(), usedItem);

        if (found == null && InventoryType.of(Easeon.instance.config.value, InventoryType.ENDER_CHEST))
            found = searchInEnderChest(player, usedItem);

        if (found != null) {
            player.setStackInHand(hand, found.copy());
            if (!item.isEmpty()) {
                player.giveOrDropItem(item, 1);
//                logger.info("tryRefillItem: {}", item.getName().getString());
            }
            player.get().playerScreenHandler.syncState();
        }
    }

    /// 플레이어 인벤토리
    private static EaseonItem searchInInventory(PlayerInventory inventory, EaseonItem item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (i >= 36 && i <= 39) {
                continue;
            }

            var slot = new EaseonItem(inventory.getStack(i));
            if (ItemHelper.isSameItem(slot, item) && !slot.isEmpty()) {
                var found = slot.copy();
                inventory.setStack(i, ItemStack.EMPTY);
                return found;
            }
        }
        return null;
    }

    /// 인벤토리의 셜커박스
    private static EaseonItem searchInShulkerBoxes(PlayerInventory inventory, EaseonItem target) {
        for (int i = 0; i < inventory.size(); i++) {
            var shulker = new EaseonItem(inventory.getStack(i));
            if (shulker.isIn(ItemTags.SHULKER_BOXES)) {
                var shulkerBoxInventory = ItemHelper.getShulkerBoxInventory(shulker);
                for (int j = 0; j < inventory.size(); j++) {
                    var slot = new EaseonItem(shulkerBoxInventory.getStack(j));
                    if (ItemHelper.isSameItem(slot, target) && !slot.isEmpty()) {
                        var found = slot.copy();
                        shulkerBoxInventory.setStack(j, ItemStack.EMPTY);
                        ItemHelper.saveShulkerBoxInventory(shulker, shulkerBoxInventory);
                        return found;
                    }
                }
            }
        }
        return null;
    }

    ///  엔더상자
    private static EaseonItem searchInEnderChest(EaseonPlayer player, EaseonItem target) {
        var inventory = player.get().getEnderChestInventory();
        for (int i = 0; i < inventory.size(); i++) {
            var slot = new EaseonItem(inventory.getStack(i));
            if (ItemHelper.isSameItem(slot, target) && !slot.isEmpty()) {
                var found = slot.copy();
                inventory.setStack(i, ItemStack.EMPTY);
                return found;
            }
        }
        return null;
    }
}

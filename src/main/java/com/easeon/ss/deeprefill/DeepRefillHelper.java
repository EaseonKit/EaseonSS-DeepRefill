package com.easeon.ss.deeprefill;

import com.easeon.ss.core.helper.ItemHelper;
import com.easeon.ss.core.util.system.EaseonLogger;
import com.easeon.ss.core.wrapper.EaseonItem;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;

public class DeepRefillHelper {
    private static final EaseonLogger logger = EaseonLogger.of();

    /// 손에 들고 있는 아이템 재충전 (BlockUse, ItemUse용)
    public static void tryRefillItem(EaseonPlayer player, EaseonItem usedItem, Hand hand) {
        var found = searchInInventory(player.getInventory(), usedItem);
        if (found != null) {
            player.setStackInHand(hand, found.copy());
            player.get().playerScreenHandler.syncState();
            return;
        }

        found = searchInShulkerBoxes(player.getInventory(), usedItem);
        if (found != null) {
            player.setStackInHand(hand, found.copy());
            player.get().playerScreenHandler.syncState();
            return;
        }

        // 3. 엔더상자에서 검색
        found = searchInEnderChest(player, usedItem);
        if (found != null) {
            player.setStackInHand(hand, found.copy());
            player.get().playerScreenHandler.syncState();
        }
    }

    /// 플레이어 인벤토리에서 동일한 아이템 검색 및 제거
    private static EaseonItem searchInInventory(PlayerInventory inventory, EaseonItem item) {
        for (int i = 0; i < inventory.size(); i++) {
            var slot = new EaseonItem(inventory.getStack(i));
            if (ItemHelper.isSameItem(slot, item) && !slot.isEmpty()) {
                var found = slot.copy();
                slot.decrement(found.getCount());
                return found;
            }
        }
        return null;
    }

    /// 인벤토리의 셜커박스에서 동일한 아이템 검색
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

    ///  엔더상자 등의 일반 컨테이너 검색
    private static EaseonItem searchInEnderChest(EaseonPlayer player, EaseonItem target) {
        var inventory = player.get().getEnderChestInventory();
        for (int i = 0; i < inventory.size(); i++) {
            var slot = new EaseonItem(inventory.getStack(i));
            if (ItemHelper.isSameItem(slot, target) && !slot.isEmpty()) {
                var found = slot.copy();
                slot.decrement(found.getCount());
                return found;
            }
        }
        return null;
    }
}

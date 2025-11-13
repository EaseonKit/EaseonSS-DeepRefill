package com.easeon.ss.deeprefill;

import com.easeon.ss.core.util.system.EaseonLogger;
import com.easeon.ss.core.wrapper.EaseonBlockHit;
import com.easeon.ss.core.wrapper.EaseonItem;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
public class DeepRefillHandler {
    private static final EaseonLogger logger = EaseonLogger.of();

    private static final Map<UUID, ItemStackSnapshot> itemBeforeUse = new HashMap<>();
    private static final Map<UUID, ItemStackSnapshot> blockBeforeUse = new HashMap<>();
    private static final Map<UUID, ItemStackSnapshot> foodBeforeConsume = new HashMap<>();

    public static ActionResult onUseItemBefore(EaseonWorld world, EaseonPlayer player, EaseonItem item, Hand hand) {
        if (world.isServer() && !item.isFood())
            itemBeforeUse.put(player.getUuid(), new ItemStackSnapshot(item.copy(), hand));

        return ActionResult.PASS;
    }

    public static ActionResult onUseBlockBefore(EaseonWorld world, EaseonPlayer player, EaseonItem item, Hand hand, EaseonBlockHit hit) {
        var farmland = world.getBlockState(hit.getBlockPos());
        if (world.isServer() && (!item.isFood() || (item.easeonBlock().of(CropBlock.class) && farmland.of(Blocks.FARMLAND))))
            blockBeforeUse.put(player.getUuid(), new ItemStackSnapshot(item.copy(), hand));

        return ActionResult.PASS;
    }

    public static ActionResult onFoodConsumeBefore(EaseonWorld world, EaseonPlayer player, EaseonItem item) {
        if (world.isServer()) {
            var hand = player.get().getActiveHand();
            foodBeforeConsume.put(player.getUuid(), new ItemStackSnapshot(item.copy(), hand));
        }

        return ActionResult.PASS;
    }

    // AFTER 이벤트들 - 저장된 아이템 정보로 리필 처리
    public static ActionResult onUseItemAfter(EaseonWorld world, EaseonPlayer player, Hand hand) {
        var actualItem = new EaseonItem(player.get().getStackInHand(hand));
        if (world.isServer() && !actualItem.isFood()) {
            var snapshot = itemBeforeUse.remove(player.getUuid());
            if (snapshot != null && (actualItem.isEmpty() || DeepRefillHelper.isConsumptionChanged(snapshot.stack, actualItem))) {
                DeepRefillHelper.tryRefillItem(player, snapshot.stack, actualItem, hand);
            }
        }

        return ActionResult.PASS;
    }

    public static ActionResult onUseBlockAfter(EaseonWorld world, EaseonPlayer player, Hand hand, EaseonBlockHit hit) {
        var actualItem = player.getStackInHand(hand);
        var farmland = world.getBlockState(hit.getBlockPos());

        if (world.isServer() && (!actualItem.isFood() || (actualItem.easeonBlock().of(CropBlock.class) && farmland.of(Blocks.FARMLAND)))) {
            var snapshot = blockBeforeUse.remove(player.getUuid());
            if (snapshot != null && (actualItem.isEmpty() || DeepRefillHelper.isConsumptionChanged(snapshot.stack, actualItem))) {
                DeepRefillHelper.tryRefillItem(player, snapshot.stack, actualItem, hand);
            }
        }

        return ActionResult.PASS;
    }

    public static ActionResult onFoodConsumeAfter(EaseonWorld world, EaseonPlayer player, EaseonItem item) {
        if (world.isServer()) {
            var snapshot = foodBeforeConsume.remove(player.getUuid());
            var actualItem = player.getStackInHand(player.get().getActiveHand());
            if (snapshot != null && (actualItem.isEmpty() || DeepRefillHelper.isConsumptionChanged(snapshot.stack, actualItem))) {
                DeepRefillHelper.tryRefillItem(player, snapshot.stack, actualItem, snapshot.hand);
            }
        }

        return ActionResult.PASS;
    }

    private static class ItemStackSnapshot {
        final EaseonItem stack;
        final Hand hand;

        ItemStackSnapshot(EaseonItem stack, Hand hand) {
            this.stack = stack;
            this.hand = hand;
        }
    }
}
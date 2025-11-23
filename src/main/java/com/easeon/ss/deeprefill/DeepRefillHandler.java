package com.easeon.ss.deeprefill;

import com.easeon.ss.core.util.system.EaseonLogger;
import com.easeon.ss.core.wrapper.*;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Items;
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
    private static final Map<UUID, ItemStackSnapshot> entityInteract = new HashMap<>();
    private static final Map<UUID, ItemStackSnapshot> beehiveBeforeUse = new HashMap<>();

    public static ActionResult onBeehiveUseBefore(EaseonWorld world, EaseonPlayer player, EaseonItem item, Hand hand, EaseonBlockHit hit) {
//        logger.info("onBeehiveUseBefore");
        if (world.isServer()) {
            beehiveBeforeUse.put(player.getUuid(), new ItemStackSnapshot(item.copy(), hand));
        }

        return ActionResult.PASS;
    }

    public static ActionResult onBeehiveUseAfter(EaseonWorld world, EaseonPlayer player, EaseonItem item, Hand hand, EaseonBlockHit hit) {
        var actualItem = player.getStackInHand(hand);
//        logger.info("onBeehiveUseAfter");
        if (world.isServer()) {
            var snapshot = beehiveBeforeUse.remove(player.getUuid());
//            logger.info("onBeehiveUseAfter-Before: {} ({})", snapshot.stack.getName().getString(), snapshot.stack.getCount());
//            logger.info("onBeehiveUseAfter-After: {} ({})", actualItem.getName().getString(), actualItem.getCount());

            if (snapshot != null && (actualItem.isEmpty() || DeepRefillHelper.isConsumptionChanged(snapshot.stack, actualItem))) {
                DeepRefillHelper.tryRefillItem(player, snapshot.stack, actualItem, hand);
            }
        }

        return ActionResult.PASS;
    }

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

    public static ActionResult onEntityInteractBefore(EaseonWorld world, EaseonPlayer player, Hand hand, EaseonEntity entity) {
        var item = player.getStackInHand(hand);
//        logger.info("onEntityInteractBefore-Before");
//        logger.info("onEntityInteractBefore-Before: {} ({})", item.getName().getString(), item.getCount());
        if (world.isServer() && item.of(Items.BUCKET, Items.WATER_BUCKET)) {
            entityInteract.put(player.getUuid(), new ItemStackSnapshot(item.copy(), hand));
        }

        return ActionResult.PASS;
    }

    // AFTER 이벤트들 - 저장된 아이템 정보로 리필 처리
    public static ActionResult onUseItemAfter(EaseonWorld world, EaseonPlayer player, Hand hand) {
        var actualItem = player.getStackInHand(hand);
//        logger.info("onUseItemAfter-Before");
        if (world.isServer() && !actualItem.isFood()) {
            var snapshot = itemBeforeUse.remove(player.getUuid());

//            logger.info("onUseItemAfter-Before: {} ({})", snapshot.stack.getName().getString(), snapshot.stack.getCount());
//            logger.info("onUseItemAfter-After: {} ({})", actualItem.getName().getString(), actualItem.getCount());

            if (snapshot != null && (actualItem.isEmpty() || DeepRefillHelper.isConsumptionChanged(snapshot.stack, actualItem))) {
                DeepRefillHelper.tryRefillItem(player, snapshot.stack, actualItem, hand);
            }
        }

        return ActionResult.PASS;
    }

    public static ActionResult onUseBlockAfter(EaseonWorld world, EaseonPlayer player, Hand hand, EaseonBlockHit hit) {
        var actualItem = player.getStackInHand(hand);
        var farmland = world.getBlockState(hit.getBlockPos());
//        logger.info("onUseBlockAfter-Before");
        if (world.isServer() && (!actualItem.isFood() || (actualItem.easeonBlock().of(CropBlock.class) && farmland.of(Blocks.FARMLAND)))) {
            var snapshot = blockBeforeUse.remove(player.getUuid());

//            logger.info("onUseBlockAfter-Before: {} ({})", snapshot.stack.getName().getString(), snapshot.stack.getCount());
//            logger.info("onUseBlockAfter-After: {} ({})", actualItem.getName().getString(), actualItem.getCount());

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

    public static ActionResult onEntityInteractAfter(EaseonWorld world, EaseonPlayer player, Hand hand, EaseonEntity entity) {
//        logger.info("onEntityInteractAfter-After");
        var actualItem = player.getStackInHand(hand);
        if (world.isServer()) {
            var snapshot = entityInteract.remove(player.getUuid());

//            logger.info("onEntityInteractAfter-Before: {} ({})", snapshot.stack.getName().getString(), snapshot.stack.getCount());
//            logger.info("onEntityInteractAfter-After: {} ({})", actualItem.getName().getString(), actualItem.getCount());

            if (snapshot != null && DeepRefillHelper.isConsumptionChanged(snapshot.stack, actualItem)) {
                DeepRefillHelper.tryRefillItem(player, snapshot.stack, actualItem, hand);
            }
        }
        return ActionResult.PASS;
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class ItemStackSnapshot {
        final EaseonItem stack;
        final Hand hand;

        ItemStackSnapshot(EaseonItem stack, Hand hand) {
            this.stack = stack;
            this.hand = hand;
        }
    }
}
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

public class DeepRefillHandler {
    private static final EaseonLogger logger = EaseonLogger.of();

    // UUID를 키로 사용하여 각 플레이어의 사용 전 아이템 정보 저장
    private static final Map<UUID, ItemStackSnapshot> itemBeforeUse = new HashMap<>();
    private static final Map<UUID, ItemStackSnapshot> blockBeforeUse = new HashMap<>();
    private static final Map<UUID, ItemStackSnapshot> foodBeforeConsume = new HashMap<>();

    // BEFORE 이벤트들 - 아이템 정보 저장
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
    public static ActionResult onUseItemAfter(EaseonWorld world, EaseonPlayer player, EaseonItem item, Hand hand) {
        if (world.isServer() && !item.isFood()) {
            var playerUuid = player.getUuid();
            var snapshot = itemBeforeUse.remove(playerUuid);
//            logger.info("onUseItemAfter: {}, {}", item.getName().getString(), item.getCount());
            if (snapshot != null && item.isEmpty()) {
                DeepRefillHelper.tryRefillItem(player, snapshot.stack, hand);
            }
        }

        return ActionResult.PASS;
    }

    public static ActionResult onUseBlockAfter(EaseonWorld world, EaseonPlayer player, EaseonItem item, Hand hand, EaseonBlockHit hit) {
        var farmland = world.getBlockState(hit.getBlockPos());
        if (world.isServer() && (!item.isFood() || (item.easeonBlock().of(CropBlock.class) && farmland.of(Blocks.FARMLAND)))) {
            var playerUuid = player.getUuid();
            var snapshot = blockBeforeUse.remove(playerUuid);
//            logger.info("onUseBlockAfter: {}, {}", item.getName().getString(), item.getCount());
            if (snapshot != null && item.isEmpty()) {
                DeepRefillHelper.tryRefillItem(player, snapshot.stack, hand);
            }
        }

        return ActionResult.PASS;
    }

    public static ActionResult onFoodConsumeAfter(EaseonWorld world, EaseonPlayer player, EaseonItem item) {
        if (world.isServer()) {
            var playerUuid = player.getUuid();
            var snapshot = foodBeforeConsume.remove(playerUuid);
//            logger.info("onFoodConsumeAfter: {}, {}", item.getName().getString(), item.getCount());
            if (snapshot != null && item.isEmpty()) {
                DeepRefillHelper.tryRefillItem(player, snapshot.stack, snapshot.hand);
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
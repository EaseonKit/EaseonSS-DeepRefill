package com.easeon.ss.deeprefill;

import com.easeon.ss.core.api.common.base.BaseToggleModule;
import com.easeon.ss.core.api.definitions.enums.EventPhase;
import com.easeon.ss.core.api.events.EaseonBlockUse;
import com.easeon.ss.core.api.events.EaseonBlockUse.BlockUseTask;
import com.easeon.ss.core.api.events.EaseonFoodConsume;
import com.easeon.ss.core.api.events.EaseonFoodConsume.FoodConsumeTask;
import com.easeon.ss.core.api.events.EaseonItemUse;
import com.easeon.ss.core.api.events.EaseonItemUse.ItemUseTask;
import com.easeon.ss.core.helper.CopperHelper;
import net.fabricmc.api.ModInitializer;

public class Easeon extends BaseToggleModule implements ModInitializer {
    public static Easeon instance;

    // BEFORE 이벤트 태스크
    private BlockUseTask blockUseBeforeTask;
    private ItemUseTask itemUseBeforeTask;
    private FoodConsumeTask foodConsumeBeforeTask;

    // AFTER 이벤트 태스크
    private BlockUseTask blockUseAfterTask;
    private ItemUseTask itemUseAfterTask;
    private FoodConsumeTask foodConsumeAfterTask;

    public Easeon() {
        instance = this;
    }

    @Override
    public void onInitialize() {
        CopperHelper.init();
        logger.info("Initialized!");
    }

    public void updateTask() {
        if (config.enabled && blockUseBeforeTask == null && itemUseBeforeTask == null && foodConsumeBeforeTask == null) {
            // BEFORE 이벤트 등록 (아이템 정보 저장)
            blockUseBeforeTask = EaseonBlockUse.on(EventPhase.BEFORE, DeepRefillHandler::onUseBlockBefore);
            itemUseBeforeTask = EaseonItemUse.on(EventPhase.BEFORE, DeepRefillHandler::onUseItemBefore);
            foodConsumeBeforeTask = EaseonFoodConsume.on(EventPhase.BEFORE, DeepRefillHandler::onFoodConsumeBefore);

            // AFTER 이벤트 등록 (리필 처리)
            blockUseAfterTask = EaseonBlockUse.on(EventPhase.AFTER, DeepRefillHandler::onUseBlockAfter);
            itemUseAfterTask = EaseonItemUse.on(EventPhase.AFTER, DeepRefillHandler::onUseItemAfter);
            foodConsumeAfterTask = EaseonFoodConsume.on(EventPhase.AFTER, DeepRefillHandler::onFoodConsumeAfter);
        }

        if (!config.enabled && blockUseBeforeTask != null && itemUseBeforeTask != null && foodConsumeBeforeTask != null) {
            // BEFORE 이벤트 제거
            blockUseBeforeTask.remove();
            blockUseBeforeTask = null;
            itemUseBeforeTask.remove();
            itemUseBeforeTask = null;
            foodConsumeBeforeTask.remove();
            foodConsumeBeforeTask = null;

            // AFTER 이벤트 제거
            blockUseAfterTask.remove();
            blockUseAfterTask = null;
            itemUseAfterTask.remove();
            itemUseAfterTask = null;
            foodConsumeAfterTask.remove();
            foodConsumeAfterTask = null;
        }
    }
}
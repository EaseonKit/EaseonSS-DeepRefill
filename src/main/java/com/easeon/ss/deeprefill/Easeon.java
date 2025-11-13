package com.easeon.ss.deeprefill;

import com.easeon.ss.core.api.common.base.BaseValueModule;
import com.easeon.ss.core.api.definitions.enums.EventPhase;
import com.easeon.ss.core.api.definitions.interfaces.EventTask;
import com.easeon.ss.core.api.events.EaseonBlockUse;
import com.easeon.ss.core.api.events.EaseonFoodConsume;
import com.easeon.ss.core.api.events.EaseonItemUse;
import com.easeon.ss.core.api.registry.EaseonCommand;
import net.fabricmc.api.ModInitializer;

public class Easeon extends BaseValueModule implements ModInitializer {
    public static Easeon instance;
    private final EventTask[] tasks = new EventTask[6];

    public Easeon() {
        super(7, 0, 7, false);
        instance = this;
    }

    @Override
    public void onInitialize() {
        EaseonCommand.register(Command::register);
        updateTask();
        logger.info("Initialized!");
    }

    public void updateTask() {
        if (config.value > 0 && tasks[0] == null) {
            tasks[0] = EaseonBlockUse.on(EventPhase.BEFORE, DeepRefillHandler::onUseBlockBefore);
            tasks[1] = EaseonBlockUse.on(EventPhase.AFTER, DeepRefillHandler::onUseBlockAfter);
            tasks[2] = EaseonFoodConsume.on(EventPhase.BEFORE, DeepRefillHandler::onFoodConsumeBefore);
            tasks[3] = EaseonFoodConsume.on(EventPhase.AFTER, DeepRefillHandler::onFoodConsumeAfter);
            tasks[4] = EaseonItemUse.on(EventPhase.BEFORE, DeepRefillHandler::onUseItemBefore);
            tasks[5] = EaseonItemUse.on(EventPhase.AFTER, DeepRefillHandler::onUseItemAfter);
        }
        else if (config.value == 0 && tasks[0] != null) {
            for (int i = 0; i < tasks.length; i++) {
                tasks[i] = tasks[i].remove();
            }
        }
    }
}
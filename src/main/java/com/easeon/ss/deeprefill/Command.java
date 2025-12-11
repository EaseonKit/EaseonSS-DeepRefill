package com.easeon.ss.deeprefill;

import com.easeon.ss.core.api.definitions.enums.InventoryType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import static com.easeon.ss.core.api.definitions.constants.Colors.*;
import static com.easeon.ss.core.api.definitions.constants.Common.*;
import static com.easeon.ss.core.util.interaction.EaseonMessenger.*;
import static net.minecraft.commands.Commands.literal;

public class Command {
    private static int opLevel;
    private static String name;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        opLevel = Easeon.instance.config.requiredOpLevel;
        name = Easeon.instance.info.name
                .replaceAll(".*- ", "")
                .replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();

        dispatcher.register(literal("easeon")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(opLevel))))
            .then(literal(name.replaceAll(" ", ""))
                .executes(Command::sendStatus)
                .then(literal("on").executes(Command::enableAll))
                .then(literal("off").executes(Command::disableAll))
                .then(literal("inventory")
                    .then(literal("on").executes(Command::enableInventory))
                    .then(literal("off").executes(Command::disableInventory))
                )
                .then(literal("shulkerbox")
                    .then(literal("on").executes(Command::enableShulkerBox))
                    .then(literal("off").executes(Command::disableShulkerBox))
                )
                .then(literal("enderchest")
                    .then(literal("on").executes(Command::enableEnderChest))
                    .then(literal("off").executes(Command::disableEnderChest))
                )
            )
        );
    }

    private static int sendStatus(CommandContext<CommandSourceStack> ctx) {
        int value = Easeon.instance.config.value;
        to(ctx.getSource(), String.format("%s current %s: %s%d", Title, name, YELLOW, value));
        sendStatusDetail(ctx, value);

        return 1;
    }
    private static void sendStatusDetail(CommandContext<CommandSourceStack> ctx, int value) {
        var playerInventory = InventoryType.of(value, InventoryType.PLAYER);
        var ShulkerBox = InventoryType.of(value, InventoryType.SHULKER_BOX);
        var EnderChest = InventoryType.of(value, InventoryType.ENDER_CHEST);
        to(ctx.getSource(), String.format("- Inventory: %s%s%s, Shulker: %s%s%s, Ender: %s%s",
            YELLOW, playerInventory, WHITE, YELLOW, ShulkerBox, WHITE, YELLOW, EnderChest));
    }

    private static int enableAll(CommandContext<CommandSourceStack> ctx) {
        setValue(ctx, 7); // 1 | 2 | 4 = 7
        return 1;
    }

    private static int disableAll(CommandContext<CommandSourceStack> ctx) {
        setValue(ctx, 0);
        return 1;
    }

    private static int enableInventory(CommandContext<CommandSourceStack> ctx) {
        int current = Easeon.instance.config.value;
        setValue(ctx, current | InventoryType.PLAYER.flag);
        return 1;
    }

    private static int disableInventory(CommandContext<CommandSourceStack> ctx) {
        int current = Easeon.instance.config.value;
        setValue(ctx, current & ~InventoryType.PLAYER.flag);
        return 1;
    }

    private static int enableShulkerBox(CommandContext<CommandSourceStack> ctx) {
        int current = Easeon.instance.config.value;
        setValue(ctx, current | InventoryType.SHULKER_BOX.flag);
        return 1;
    }

    private static int disableShulkerBox(CommandContext<CommandSourceStack> ctx) {
        int current = Easeon.instance.config.value;
        setValue(ctx, current & ~InventoryType.SHULKER_BOX.flag);
        return 1;
    }

    private static int enableEnderChest(CommandContext<CommandSourceStack> ctx) {
        int current = Easeon.instance.config.value;
        setValue(ctx, current | InventoryType.ENDER_CHEST.flag);
        return 1;
    }

    private static int disableEnderChest(CommandContext<CommandSourceStack> ctx) {
        int current = Easeon.instance.config.value;
        setValue(ctx, current & ~InventoryType.ENDER_CHEST.flag);
        return 1;
    }

    private static void setValue(CommandContext<CommandSourceStack> ctx, int newValue) {
        int oldValue = Easeon.instance.config.value;

        if (oldValue == newValue) {
            to(ctx.getSource(), String.format("%s%s%s is already %d", Title, YELLOW, name, newValue));
        } else {
            Easeon.instance.config.setValue(newValue);
            Easeon.instance.updateTask();
            toAll(ctx.getSource(), String.format("%s%s changed: %s%d%s → %s%d", Title, name, YELLOW, oldValue, WHITE, GREEN, newValue));
            sendStatusDetail(ctx, newValue);
        }
    }
}
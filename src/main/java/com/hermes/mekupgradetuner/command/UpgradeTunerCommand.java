package com.hermes.mekupgradetuner.command;

import com.hermes.mekupgradetuner.Config;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class UpgradeTunerCommand {

    private static final List<String> KEYS = List.of(
            "speedMax",
            "energyMax",
            "gasMax",
            "mufflingMax",
            "filterMax",
            "anchorMax",
            "stoneGeneratorMax",
            "removeSpeedEnergyCost",
            "limitSpeedEnergyIncrease",
            "speedEnergyMultiplierCap",
            "limitEnergyReduction",
            "energyConsumptionFloor",
            "limitMinimumConsumption",
            "minimumConsumptionPercent"
    );

    private static final List<String> BOOL_KEYS = List.of(
            "removeSpeedEnergyCost",
            "limitSpeedEnergyIncrease",
            "limitEnergyReduction",
            "limitMinimumConsumption"
    );

    private UpgradeTunerCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("mekupgradetuner")
                .requires(source -> !source.getServer().isDedicatedServer() || source.hasPermission(2))
                .then(Commands.literal("get")
                        .executes(UpgradeTunerCommand::get))
                .then(Commands.literal("set")
                        .then(Commands.argument("key", StringArgumentType.word())
                                .suggests((context, builder) -> suggestKeys(builder))
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests(UpgradeTunerCommand::suggestValues)
                                        .executes(UpgradeTunerCommand::set))))
                .then(Commands.literal("toggle")
                        .then(Commands.argument("key", StringArgumentType.word())
                                .suggests((context, builder) -> suggestBoolKeys(builder))
                                .executes(UpgradeTunerCommand::toggle)));
        event.getDispatcher().register(root);
    }

    private static int get(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal(String.join("\n", Config.describe())), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "key");
        String value = StringArgumentType.getString(context, "value");
        String error = Config.setValue(key, value);
        if (error != null) {
            context.getSource().sendFailure(Component.literal(error));
            return 0;
        }
        context.getSource().sendSuccess(
                () -> Component.literal("已设置 " + key + " = " + value + "（已写入配置文件，机器会在下一 tick 生效）"),
                true
        );
        return 1;
    }

    private static int toggle(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "key");
        String error = Config.toggle(key);
        if (error != null) {
            context.getSource().sendFailure(Component.literal(error));
            return 0;
        }
        context.getSource().sendSuccess(
                () -> Component.literal("已切换 " + key + "（已写入配置文件，机器会在下一 tick 生效）"),
                true
        );
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestKeys(SuggestionsBuilder builder) {
        String prefix = builder.getRemainingLowerCase();
        for (String key : KEYS) {
            if (key.toLowerCase().startsWith(prefix)) {
                builder.suggest(key);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestBoolKeys(SuggestionsBuilder builder) {
        String prefix = builder.getRemainingLowerCase();
        for (String key : BOOL_KEYS) {
            if (key.toLowerCase().startsWith(prefix)) {
                builder.suggest(key);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestValues(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String key = StringArgumentType.getString(context, "key");
        if (BOOL_KEYS.contains(key)) {
            builder.suggest("true");
            builder.suggest("false");
        } else if ("speedEnergyMultiplierCap".equals(key)) {
            builder.suggest("10.0");
            builder.suggest("5.0");
            builder.suggest("20.0");
        } else if ("energyConsumptionFloor".equals(key)) {
            builder.suggest("0.1");
            builder.suggest("0.5");
            builder.suggest("0.01");
        } else if ("minimumConsumptionPercent".equals(key)) {
            builder.suggest("50.0");
            builder.suggest("75.0");
            builder.suggest("100.0");
            builder.suggest("200.0");
        } else {
            builder.suggest("8");
            builder.suggest("12");
            builder.suggest("16");
        }
        return builder.buildFuture();
    }
}

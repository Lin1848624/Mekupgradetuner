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

    private record HelpEntry(String key, String summary, String detail) {
    }

    private static final List<HelpEntry> LIMIT_HELP = List.of(
            new HelpEntry("speedMax", "加速升级最多装几个",
                    "范围 1-64。12 个 = 31.6 倍速；16 个 = 100 倍速。装太多会让化学灌注机等 6 种机器卡服。"),
            new HelpEntry("energyMax", "能量升级最多装几个",
                    "范围 1-64。也决定电池容量：16 个 = 100 倍容量。耗电降低还受 energyConsumptionFloor 和 minimumConsumptionPercent 限制。"),
            new HelpEntry("gasMax", "气体升级最多装几个",
                    "范围 1-64。影响气体消耗，不直接影响电。"),
            new HelpEntry("mufflingMax", "消音升级最多装几个",
                    "范围 1-64。原版 4 个就完全静音。"),
            new HelpEntry("filterMax", "过滤升级上限",
                    "开关型，1 个就够；调高没有额外效果。"),
            new HelpEntry("anchorMax", "区块加载升级上限",
                    "开关型，1 个就够。"),
            new HelpEntry("stoneGeneratorMax", "造石升级上限",
                    "开关型，1 个就够。")
    );

    private static final List<HelpEntry> ENERGY_HELP = List.of(
            new HelpEntry("removeSpeedEnergyCost", "加速完全不加耗电（1.0.0 行为，比较超模）",
                    "true = 加速不产生任何耗电；false = 按下面规则计算。"),
            new HelpEntry("limitSpeedEnergyIncrease", "给加速的耗电上涨封顶",
                    "true = 加速最多把耗电提高到 speedEnergyMultiplierCap 倍；false = 不封顶（原版平方公式）。"),
            new HelpEntry("speedEnergyMultiplierCap", "加速最多把耗电提高到几倍",
                    "范围 1.0-1000.0。10.0 = 最多 10 倍；1.0 = 加速完全不涨耗电。"),
            new HelpEntry("limitEnergyReduction", "给能量升级的省电效果封顶",
                    "true = 能量最多把耗电降到 energyConsumptionFloor 倍；false = 不封顶（最多 0.01 倍）。"),
            new HelpEntry("energyConsumptionFloor", "能量升级最多把耗电降到基础值的几倍",
                    "范围 0.0001-1.0。0.1 = 最多省 90%；1.0 = 不省电。"),
            new HelpEntry("limitMinimumConsumption", "最终耗电再加一个最低值",
                    "true = 所有计算完后再取 max(结果, 基础耗电 × minimumConsumptionPercent%)。"),
            new HelpEntry("minimumConsumptionPercent", "最终耗电最低为基础耗电的百分之几",
                    "范围 0-1000。50 = 最低 50%；100 = 基础耗电；200 = 最低 2 倍基础耗电。")
    );

    private UpgradeTunerCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("mekupgradetuner")
                .requires(source -> !source.getServer().isDedicatedServer() || source.hasPermission(2))
                .then(Commands.literal("help")
                        .executes(UpgradeTunerCommand::help)
                        .then(Commands.argument("key", StringArgumentType.word())
                                .suggests((context, builder) -> suggestKeys(builder))
                                .executes(UpgradeTunerCommand::helpKey)))
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

    private static int help(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("通用机械：升级调校 帮助"), false);
        source.sendSuccess(() -> Component.literal("升级安装上限（装几个）：\n" + overview(LIMIT_HELP)), false);
        source.sendSuccess(() -> Component.literal("耗电规则：\n" + overview(ENERGY_HELP)), false);
        source.sendSuccess(() -> Component.literal(
                "用法：\n"
                        + "  /mekupgradetuner help <参数>   看单项详细说明\n"
                        + "  /mekupgradetuner set <参数> <值>\n"
                        + "  /mekupgradetuner toggle <开关参数>\n"
                        + "  /mekupgradetuner get"
        ), false);
        return 1;
    }

    private static int helpKey(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "key");
        HelpEntry entry = findHelp(key);
        if (entry == null) {
            context.getSource().sendFailure(Component.literal("没有这个参数：" + key + "。用 /mekupgradetuner help 看全部。"));
            return 0;
        }
        String value = Config.currentValue(entry.key());
        context.getSource().sendSuccess(() -> Component.literal(
                entry.key() + " = " + value + "\n" + entry.summary() + "\n" + entry.detail()
        ), false);
        return 1;
    }

    private static String overview(List<HelpEntry> entries) {
        StringBuilder builder = new StringBuilder();
        for (HelpEntry entry : entries) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(entry.key()).append(" = ").append(Config.currentValue(entry.key()))
                    .append(" ｜ ").append(entry.summary());
        }
        return builder.toString();
    }

    private static HelpEntry findHelp(String key) {
        for (HelpEntry entry : LIMIT_HELP) {
            if (entry.key().equalsIgnoreCase(key)) {
                return entry;
            }
        }
        for (HelpEntry entry : ENERGY_HELP) {
            if (entry.key().equalsIgnoreCase(key)) {
                return entry;
            }
        }
        return null;
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

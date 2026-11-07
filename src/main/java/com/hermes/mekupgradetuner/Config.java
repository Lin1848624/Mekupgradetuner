package com.hermes.mekupgradetuner;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * 配置文件。默认值直接写成用户选定的 12 / 16 / 16 / 4 / 1 / 1 / 1，
 * 想回到原版就在 config/mekupgradetuner-common.toml 里改成 8 / 8 / 8 / 4 / 1 / 1 / 1。
 */
public final class Config {

    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.IntValue SPEED_MAX;
    private static final ForgeConfigSpec.IntValue ENERGY_MAX;
    private static final ForgeConfigSpec.IntValue GAS_MAX;
    private static final ForgeConfigSpec.IntValue MUFFLING_MAX;
    private static final ForgeConfigSpec.IntValue FILTER_MAX;
    private static final ForgeConfigSpec.IntValue ANCHOR_MAX;
    private static final ForgeConfigSpec.IntValue STONE_GENERATOR_MAX;
    private static final ForgeConfigSpec.BooleanValue REMOVE_SPEED_ENERGY;
    private static final ForgeConfigSpec.BooleanValue LIMIT_SPEED_ENERGY;
    private static final ForgeConfigSpec.DoubleValue SPEED_ENERGY_CAP;
    private static final ForgeConfigSpec.BooleanValue LIMIT_ENERGY_REDUCTION;
    private static final ForgeConfigSpec.DoubleValue ENERGY_FLOOR;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment(
                "每种升级最多能装几个。范围 1-64。",
                "原版值：加速 8、能量 8、气体 8、消音 4、过滤 1、区块加载 1、造石 1。",
                "过滤 / 区块加载 / 造石是“装上就生效”的开关型升级，超过 1 个没有额外效果。"
        ).push("upgradeLimits");
        SPEED_MAX = builder.comment("加速升级上限。12 = 31.6 倍速；16 = 100 倍速（那 6 种多次操作机器会卡）。")
                .defineInRange("speedMax", 12, 1, 64);
        ENERGY_MAX = builder.comment("能量升级上限。16 = 能量上限升到 100 倍；耗电降低另受 energyConsumptionFloor 限制。")
                .defineInRange("energyMax", 16, 1, 64);
        GAS_MAX = builder.comment("气体升级上限。")
                .defineInRange("gasMax", 16, 1, 64);
        MUFFLING_MAX = builder.comment("消音升级上限。")
                .defineInRange("mufflingMax", 4, 1, 64);
        FILTER_MAX = builder.comment("过滤升级上限（开关型，1 个就够）。")
                .defineInRange("filterMax", 1, 1, 64);
        ANCHOR_MAX = builder.comment("区块加载升级上限（开关型，1 个就够）。")
                .defineInRange("anchorMax", 1, 1, 64);
        STONE_GENERATOR_MAX = builder.comment("造石升级上限（开关型，1 个就够）。")
                .defineInRange("stoneGeneratorMax", 1, 1, 64);
        builder.pop();

        builder.comment("耗电相关。").push("energy");
        REMOVE_SPEED_ENERGY = builder.comment("true = 加速升级完全不加耗电（1.0.0 的行为，比较超模）。")
                .define("removeSpeedEnergyCost", false);
        LIMIT_SPEED_ENERGY = builder.comment("true = 给“加速带来的耗电上涨”封顶。")
                .define("limitSpeedEnergyIncrease", true);
        SPEED_ENERGY_CAP = builder.comment("加速最多把耗电提高到几倍。10.0 = 最多 10 倍（+900%）。范围 1.0-1000.0。")
                .defineInRange("speedEnergyMultiplierCap", 10.0, 1.0, 1000.0);
        LIMIT_ENERGY_REDUCTION = builder.comment("true = 给“能量升级的省电效果”封顶。")
                .define("limitEnergyReduction", true);
        ENERGY_FLOOR = builder.comment("耗电最低降到基础值的几倍。0.1 = 最多省 90%，不会更低。范围 0.0001-1.0。")
                .defineInRange("energyConsumptionFloor", 0.1, 0.0001, 1.0);
        builder.pop();

        SPEC = builder.build();
    }

    private Config() {
    }

    public static void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            ConfigCache.update(
                    SPEED_MAX.get(),
                    ENERGY_MAX.get(),
                    GAS_MAX.get(),
                    MUFFLING_MAX.get(),
                    FILTER_MAX.get(),
                    ANCHOR_MAX.get(),
                    STONE_GENERATOR_MAX.get(),
                    REMOVE_SPEED_ENERGY.get(),
                    LIMIT_SPEED_ENERGY.get(),
                    SPEED_ENERGY_CAP.get(),
                    LIMIT_ENERGY_REDUCTION.get(),
                    ENERGY_FLOOR.get()
            );
        }
    }
}

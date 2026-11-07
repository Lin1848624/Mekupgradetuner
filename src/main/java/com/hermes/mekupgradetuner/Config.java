package com.hermes.mekupgradetuner;

import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
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

    private static ModConfig modConfig;

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
            modConfig = event.getConfig();
            apply();
        }
    }

    private static void apply() {
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

    /**
     * 给指令的 get 子命令用。
     */
    public static List<String> describe() {
        return List.of(
                "升级安装上限：",
                "  speedMax = " + SPEED_MAX.get(),
                "  energyMax = " + ENERGY_MAX.get(),
                "  gasMax = " + GAS_MAX.get(),
                "  mufflingMax = " + MUFFLING_MAX.get(),
                "  filterMax = " + FILTER_MAX.get(),
                "  anchorMax = " + ANCHOR_MAX.get(),
                "  stoneGeneratorMax = " + STONE_GENERATOR_MAX.get(),
                "耗电设置：",
                "  removeSpeedEnergyCost = " + REMOVE_SPEED_ENERGY.get(),
                "  limitSpeedEnergyIncrease = " + LIMIT_SPEED_ENERGY.get(),
                "  speedEnergyMultiplierCap = " + SPEED_ENERGY_CAP.get(),
                "  limitEnergyReduction = " + LIMIT_ENERGY_REDUCTION.get(),
                "  energyConsumptionFloor = " + ENERGY_FLOOR.get()
        );
    }

    /**
     * 指令改配置。成功返回 null，失败返回给玩家看的中文错误。
     */
    public static String setValue(String key, String raw) {
        if (modConfig == null) {
            return "配置还没加载，进世界后再试";
        }
        try {
            switch (key) {
                case "speedMax" -> {
                    Integer value = parseInt(raw, 1, 64);
                    if (value == null) {
                        return "speedMax 必须是 1-64 的整数";
                    }
                    SPEED_MAX.set(value);
                }
                case "energyMax" -> {
                    Integer value = parseInt(raw, 1, 64);
                    if (value == null) {
                        return "energyMax 必须是 1-64 的整数";
                    }
                    ENERGY_MAX.set(value);
                }
                case "gasMax" -> {
                    Integer value = parseInt(raw, 1, 64);
                    if (value == null) {
                        return "gasMax 必须是 1-64 的整数";
                    }
                    GAS_MAX.set(value);
                }
                case "mufflingMax" -> {
                    Integer value = parseInt(raw, 1, 64);
                    if (value == null) {
                        return "mufflingMax 必须是 1-64 的整数";
                    }
                    MUFFLING_MAX.set(value);
                }
                case "filterMax" -> {
                    Integer value = parseInt(raw, 1, 64);
                    if (value == null) {
                        return "filterMax 必须是 1-64 的整数";
                    }
                    FILTER_MAX.set(value);
                }
                case "anchorMax" -> {
                    Integer value = parseInt(raw, 1, 64);
                    if (value == null) {
                        return "anchorMax 必须是 1-64 的整数";
                    }
                    ANCHOR_MAX.set(value);
                }
                case "stoneGeneratorMax" -> {
                    Integer value = parseInt(raw, 1, 64);
                    if (value == null) {
                        return "stoneGeneratorMax 必须是 1-64 的整数";
                    }
                    STONE_GENERATOR_MAX.set(value);
                }
                case "removeSpeedEnergyCost" -> {
                    Boolean value = parseBool(raw);
                    if (value == null) {
                        return "removeSpeedEnergyCost 只接受 true/false";
                    }
                    REMOVE_SPEED_ENERGY.set(value);
                }
                case "limitSpeedEnergyIncrease" -> {
                    Boolean value = parseBool(raw);
                    if (value == null) {
                        return "limitSpeedEnergyIncrease 只接受 true/false";
                    }
                    LIMIT_SPEED_ENERGY.set(value);
                }
                case "speedEnergyMultiplierCap" -> {
                    Double value = parseDouble(raw, 1.0D, 1000.0D);
                    if (value == null) {
                        return "speedEnergyMultiplierCap 必须是 1.0-1000.0 的数字";
                    }
                    SPEED_ENERGY_CAP.set(value);
                }
                case "limitEnergyReduction" -> {
                    Boolean value = parseBool(raw);
                    if (value == null) {
                        return "limitEnergyReduction 只接受 true/false";
                    }
                    LIMIT_ENERGY_REDUCTION.set(value);
                }
                case "energyConsumptionFloor" -> {
                    Double value = parseDouble(raw, 0.0001D, 1.0D);
                    if (value == null) {
                        return "energyConsumptionFloor 必须是 0.0001-1.0 的数字";
                    }
                    ENERGY_FLOOR.set(value);
                }
                default -> {
                    return "未知配置项：" + key;
                }
            }
            apply();
            modConfig.save();
            return null;
        } catch (RuntimeException e) {
            return "设置失败：" + e.getMessage();
        }
    }

    public static String toggle(String key) {
        return switch (key) {
            case "removeSpeedEnergyCost" -> setValue(key, Boolean.toString(!REMOVE_SPEED_ENERGY.get()));
            case "limitSpeedEnergyIncrease" -> setValue(key, Boolean.toString(!LIMIT_SPEED_ENERGY.get()));
            case "limitEnergyReduction" -> setValue(key, Boolean.toString(!LIMIT_ENERGY_REDUCTION.get()));
            default -> "不是开关项：" + key
                    + "（可用：removeSpeedEnergyCost / limitSpeedEnergyIncrease / limitEnergyReduction）";
        };
    }

    private static Integer parseInt(String raw, int min, int max) {
        try {
            int value = Integer.parseInt(raw);
            return value >= min && value <= max ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDouble(String raw, double min, double max) {
        try {
            double value = Double.parseDouble(raw);
            return Double.isFinite(value) && value >= min && value <= max ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean parseBool(String raw) {
        if ("true".equalsIgnoreCase(raw)) {
            return true;
        }
        if ("false".equalsIgnoreCase(raw)) {
            return false;
        }
        return null;
    }
}

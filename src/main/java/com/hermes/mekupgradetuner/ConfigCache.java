package com.hermes.mekupgradetuner;

import mekanism.api.Upgrade;

/**
 * Mixin 里读取的缓存值。默认值和 Config 里的默认值保持一致；
 * 配置文件还没加载时也用这些值，不会抛异常。
 */
public final class ConfigCache {

    private static volatile int speedMax = 12;
    private static volatile int energyMax = 16;
    private static volatile int gasMax = 16;
    private static volatile int mufflingMax = 4;
    private static volatile int filterMax = 1;
    private static volatile int anchorMax = 1;
    private static volatile int stoneGeneratorMax = 1;

    private static volatile boolean removeSpeedEnergyCost = false;
    private static volatile boolean limitSpeedEnergyIncrease = true;
    private static volatile double speedEnergyMultiplierCap = 10.0D;
    private static volatile boolean limitEnergyReduction = true;
    private static volatile double energyConsumptionFloor = 0.1D;

    private ConfigCache() {
    }

    public static int max(Upgrade upgrade) {
        return switch (upgrade) {
            case SPEED -> speedMax;
            case ENERGY -> energyMax;
            case GAS -> gasMax;
            case MUFFLING -> mufflingMax;
            case FILTER -> filterMax;
            case ANCHOR -> anchorMax;
            case STONE_GENERATOR -> stoneGeneratorMax;
        };
    }

    public static boolean removeSpeedEnergyCost() {
        return removeSpeedEnergyCost;
    }

    public static boolean limitSpeedEnergyIncrease() {
        return limitSpeedEnergyIncrease;
    }

    public static double speedEnergyMultiplierCap() {
        return speedEnergyMultiplierCap;
    }

    public static boolean limitEnergyReduction() {
        return limitEnergyReduction;
    }

    public static double energyConsumptionFloor() {
        return energyConsumptionFloor;
    }

    static void update(int speed, int energy, int gas, int muffling, int filter, int anchor, int stone,
                       boolean freeSpeedEnergy, boolean limitSpeedEnergy, double speedEnergyCap,
                       boolean limitEnergy, double energyFloor) {
        speedMax = speed;
        energyMax = energy;
        gasMax = gas;
        mufflingMax = muffling;
        filterMax = filter;
        anchorMax = anchor;
        stoneGeneratorMax = stone;
        removeSpeedEnergyCost = freeSpeedEnergy;
        limitSpeedEnergyIncrease = limitSpeedEnergy;
        speedEnergyMultiplierCap = speedEnergyCap;
        limitEnergyReduction = limitEnergy;
        energyConsumptionFloor = energyFloor;
    }
}

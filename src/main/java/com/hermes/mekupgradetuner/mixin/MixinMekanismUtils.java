package com.hermes.mekupgradetuner.mixin;

import com.hermes.mekupgradetuner.ConfigCache;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.MekanismUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MekanismUtils.class)
public class MixinMekanismUtils {

    /**
     * 原版：def * 10^(2 * 加速/8 - 能量/8)。
     * 现在按三个开关组合：
     * - removeSpeedEnergyCost：加速完全不耗电（1.0.0 行为）；
     * - limitSpeedEnergyIncrease：加速贡献最多 speedEnergyMultiplierCap 倍；
     * - limitEnergyReduction：能量贡献最多把耗电降到 energyConsumptionFloor 倍。
     */
    @Inject(method = "getEnergyPerTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mekupgradetuner$freeSpeedEnergy(IUpgradeTile tile, FloatingLong def, CallbackInfoReturnable<FloatingLong> cir) {
        if (tile.supportsUpgrades()) {
            double speedMultiplier = 1.0D;
            if (!ConfigCache.removeSpeedEnergyCost()) {
                speedMultiplier = Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(),
                        2 * MekanismUtils.fractionUpgrades(tile, Upgrade.SPEED));
                if (ConfigCache.limitSpeedEnergyIncrease()) {
                    speedMultiplier = Math.min(speedMultiplier, ConfigCache.speedEnergyMultiplierCap());
                }
            }
            double energyDivisor = Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(),
                    MekanismUtils.fractionUpgrades(tile, Upgrade.ENERGY));
            if (ConfigCache.limitEnergyReduction()) {
                energyDivisor = Math.min(energyDivisor, 1.0D / ConfigCache.energyConsumptionFloor());
            }
            FloatingLong result = def.multiply(speedMultiplier / energyDivisor);
            if (ConfigCache.limitMinimumConsumption()) {
                result = result.max(def.multiply(ConfigCache.minimumConsumptionPercent() / 100.0D));
            }
            cir.setReturnValue(result);
        }
    }
}

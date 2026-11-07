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
     * 去掉“2 * 加速”这一项，加速升级不再影响耗电，只剩能量升级的折扣。
     */
    @Inject(method = "getEnergyPerTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mekupgradetuner$freeSpeedEnergy(IUpgradeTile tile, FloatingLong def, CallbackInfoReturnable<FloatingLong> cir) {
        if (ConfigCache.removeSpeedEnergyCost() && tile.supportsUpgrades()) {
            double exponent = -MekanismUtils.fractionUpgrades(tile, Upgrade.ENERGY);
            cir.setReturnValue(def.multiply(Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), exponent)));
        }
    }
}

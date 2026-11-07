package com.hermes.mekupgradetuner.mixin;

import com.hermes.mekupgradetuner.ConfigCache;
import mekanism.api.Upgrade;
import mekanism.common.tile.component.TileComponentUpgrade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileComponentUpgrade.class)
public class MixinTileComponentUpgrade {

    @Redirect(
            method = "tickServer",
            at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;getMax()I"),
            remap = false
    )
    private int mekupgradetuner$tickServerMax(Upgrade upgrade) {
        return ConfigCache.max(upgrade);
    }

    @Redirect(
            method = "addUpgrades",
            at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;getMax()I", ordinal = 0),
            remap = false
    )
    private int mekupgradetuner$addUpgradesMaxFirst(Upgrade upgrade) {
        return ConfigCache.max(upgrade);
    }

    @Redirect(
            method = "addUpgrades",
            at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;getMax()I", ordinal = 1),
            remap = false
    )
    private int mekupgradetuner$addUpgradesMaxSecond(Upgrade upgrade) {
        return ConfigCache.max(upgrade);
    }
}

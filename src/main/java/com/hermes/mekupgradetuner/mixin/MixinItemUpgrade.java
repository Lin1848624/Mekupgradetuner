package com.hermes.mekupgradetuner.mixin;

import com.hermes.mekupgradetuner.ConfigCache;
import mekanism.api.Upgrade;
import mekanism.common.item.ItemUpgrade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemUpgrade.class)
public class MixinItemUpgrade {

    @Redirect(
            method = "appendHoverText",
            at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;getMax()I")
    )
    private int mekupgradetuner$tooltipMax(Upgrade upgrade) {
        return ConfigCache.max(upgrade);
    }
}

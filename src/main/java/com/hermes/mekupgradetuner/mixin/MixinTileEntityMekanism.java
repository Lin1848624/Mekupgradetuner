package com.hermes.mekupgradetuner.mixin;

import com.hermes.mekupgradetuner.ConfigCache;
import mekanism.api.Upgrade;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityMekanism.class)
public class MixinTileEntityMekanism {

    @Redirect(
            method = "isFullyMuffled",
            at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;getMax()I"),
            remap = false
    )
    private int mekupgradetuner$muffledMax(Upgrade upgrade) {
        return ConfigCache.max(upgrade);
    }
}

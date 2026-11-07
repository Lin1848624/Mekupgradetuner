package com.hermes.mekupgradetuner.mixin;

import com.hermes.mekupgradetuner.ConfigCache;
import mekanism.api.Upgrade;
import mekanism.common.recipe.upgrade.UpgradesRecipeData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(UpgradesRecipeData.class)
public class MixinUpgradesRecipeData {

    @Redirect(
            method = "merge",
            at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;getMax()I"),
            remap = false
    )
    private int mekupgradetuner$mergeMax(Upgrade upgrade) {
        return ConfigCache.max(upgrade);
    }
}

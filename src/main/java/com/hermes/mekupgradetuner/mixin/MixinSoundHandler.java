package com.hermes.mekupgradetuner.mixin;

import com.hermes.mekupgradetuner.ConfigCache;
import mekanism.api.Upgrade;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "mekanism.client.sound.SoundHandler$TileTickableSound")
public class MixinSoundHandler {

    @Redirect(
            method = "getTileVolumeFactor",
            at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;getMax()I"),
            remap = false
    )
    private int mekupgradetuner$volumeMax(Upgrade upgrade) {
        return ConfigCache.max(upgrade);
    }

    @Inject(method = "getTileVolumeFactor", at = @At("RETURN"), cancellable = true, remap = false)
    private void mekupgradetuner$clampVolume(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(Mth.clamp(cir.getReturnValueF(), 0.0F, 1.0F));
    }
}

package com.hermes.mekupgradetuner.mixin;

import com.hermes.mekupgradetuner.ConfigCache;
import mekanism.api.Upgrade;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    /**
     * 指令改完配置后，让已加载的机器在下一 tick 重算一次耗电/容量。
     */
    @Inject(method = "tickServer", at = @At("HEAD"), remap = false)
    private static void mekupgradetuner$syncConfig(Level level, BlockPos pos, BlockState state, TileEntityMekanism tile, CallbackInfo ci) {
        if (ConfigCache.shouldRecalculate(tile)) {
            tile.recalculateUpgrades(Upgrade.SPEED);
            tile.recalculateUpgrades(Upgrade.ENERGY);
        }
    }
}

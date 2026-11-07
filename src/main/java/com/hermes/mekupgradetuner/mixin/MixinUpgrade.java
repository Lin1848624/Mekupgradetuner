package com.hermes.mekupgradetuner.mixin;

import com.hermes.mekupgradetuner.ConfigCache;
import mekanism.api.Upgrade;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Upgrade.class)
public class MixinUpgrade {

    /**
     * 读档时原版会用 private final maxStack 字段裁剪数量（不是调用 getMax()），
     * 这里把字段读取改成配置值，否则重进世界会被裁回 8。
     */
    @Redirect(
            method = "buildMap",
            at = @At(
                    value = "FIELD",
                    target = "Lmekanism/api/Upgrade;maxStack:I",
                    opcode = Opcodes.GETFIELD
            ),
            remap = false
    )
    private static int mekupgradetuner$buildMapMax(Upgrade upgrade) {
        return ConfigCache.max(upgrade);
    }
}

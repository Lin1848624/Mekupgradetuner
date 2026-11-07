package com.hermes.mekupgradetuner;

import com.hermes.mekupgradetuner.command.UpgradeTunerCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MekUpgradeTuner.MODID)
public final class MekUpgradeTuner {

    public static final String MODID = "mekupgradetuner";

    public MekUpgradeTuner() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::onConfigLoad);
        MinecraftForge.EVENT_BUS.addListener(UpgradeTunerCommand::register);
    }
}

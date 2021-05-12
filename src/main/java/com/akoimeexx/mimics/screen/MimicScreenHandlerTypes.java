package com.akoimeexx.mimics.screen;

import com.akoimeexx.mimics.MimicsMod;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry.SimpleClientHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class MimicScreenHandlerTypes {
    public static final ScreenHandlerType<MimicScreenHandler> MIMIC_SCREEN_HANDLER_9x2;
    public static final ScreenHandlerType<MimicScreenHandler> MIMIC_SCREEN_HANDLER_9x3;
    public static final ScreenHandlerType<MimicScreenHandler> MIMIC_SCREEN_HANDLER_9x4;
    public static final ScreenHandlerType<MimicScreenHandler> MIMIC_SCREEN_HANDLER_9x5;
    public static final ScreenHandlerType<MimicScreenHandler> MIMIC_SCREEN_HANDLER_9x6;

    public static void load() {

    }

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(
        String name, 
        SimpleClientHandlerFactory<T> factory
    ) {
        return ScreenHandlerRegistry.registerSimple(
            MimicsMod.id(name), 
            factory
        );
    }

    static {
        MIMIC_SCREEN_HANDLER_9x2 = register(
            "mimic_tier1_9x2", 
            MimicScreenHandler::createTier1ScreenHandler
        );
        MIMIC_SCREEN_HANDLER_9x3 = register(
            "mimic_tier2_9x3", 
            MimicScreenHandler::createTier2ScreenHandler
        );
        MIMIC_SCREEN_HANDLER_9x4 = register(
            "mimic_tier3_9x4", 
            MimicScreenHandler::createTier3ScreenHandler
        );
        MIMIC_SCREEN_HANDLER_9x5 = register(
            "mimic_tier4_9x5", 
            MimicScreenHandler::createTier4ScreenHandler
        );
        MIMIC_SCREEN_HANDLER_9x6 = register(
            "mimic_tier5_9x6", 
            MimicScreenHandler::createTier5ScreenHandler
        );
    }
  
}

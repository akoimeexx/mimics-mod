package com.akoimeexx.mimics;

import com.akoimeexx.mimics.client.gui.screen.ingame.MimicScreen;
import com.akoimeexx.mimics.client.render.entity.MimicEntityRenderer;
import com.akoimeexx.mimics.entity.EntityTypes;
import com.akoimeexx.mimics.entity.MimicEntity;
import com.akoimeexx.mimics.screen.MimicScreenHandlerTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

@Environment(EnvType.CLIENT)
public class MimicsClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(
            EntityTypes.MIMIC, 
            (dispatcher, context) -> {
                return new MimicEntityRenderer<MimicEntity>(dispatcher);
            }
        );
        ScreenRegistry.register(
            MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x2, 
            MimicScreen::new
        );
        ScreenRegistry.register(
            MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x3, 
            MimicScreen::new
        );
        ScreenRegistry.register(
            MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x4, 
            MimicScreen::new
        );
        ScreenRegistry.register(
            MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x5, 
            MimicScreen::new
        );
        ScreenRegistry.register(
            MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x6, 
            MimicScreen::new
        );
    }
}

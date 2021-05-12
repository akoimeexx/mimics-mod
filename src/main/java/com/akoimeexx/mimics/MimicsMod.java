package com.akoimeexx.mimics;

import com.akoimeexx.mimics.entity.EntityTypes;
import com.akoimeexx.mimics.item.Items;
import com.akoimeexx.mimics.screen.MimicScreenHandlerTypes;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class MimicsMod implements ModInitializer {
    public static final String MOD_ID = "mimics";
    public static Identifier id(String name) {
        return new Identifier(MOD_ID, name);
    }
	@Override
	public void onInitialize() {
        EntityTypes.load();
        MimicScreenHandlerTypes.load();
        Items.load();
	}
}

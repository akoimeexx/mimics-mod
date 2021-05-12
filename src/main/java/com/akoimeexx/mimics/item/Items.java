package com.akoimeexx.mimics.item;

import com.akoimeexx.mimics.MimicsMod;
import com.akoimeexx.mimics.entity.EntityTypes;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Items {
    public static final MimicItem MIMIC;
    public static final Item MIMIC_CORE;
    public static final SpawnEggItem MIMIC_SPAWN_EGG;

    public static void load() {
        System.out.println("Items loaded");
    }

    private static <T extends Item> T register(String name, T item) {
        return register(MimicsMod.id(name), item);
    }

    private static <T extends Item> T register(
        Identifier id, 
        T item
    ) {
        return (T)Registry.register(Registry.ITEM, id, item);
    }

    static {
        MIMIC = register(
            "mimic", 
            new MimicItem(
                new Item.Settings()
                    .group(ItemGroup.TOOLS)
                    .maxCount(1)
                    .fireproof()
            )
        );
        MIMIC_CORE = register(
            "mimic_core", 
            new Item(
                new Item.Settings()
                    .group(ItemGroup.MISC)
                    .fireproof()
                    .maxCount(16)
            )
        );
        MIMIC_SPAWN_EGG = register(
            "mimic_spawn_egg", 
            new SpawnEggItem(
                EntityTypes.MIMIC, 
                0xD7A306, 
                0xC0C0C0, 
                new Item.Settings().group(ItemGroup.MISC)
            )
        );
    }
}

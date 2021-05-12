package com.akoimeexx.mimics.entity;

import com.akoimeexx.mimics.MimicsMod;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;

public class EntityTypes<T extends Entity> {
    public static final EntityType<MimicEntity> MIMIC;

    public static void load() {
        FabricDefaultAttributeRegistry.register(
            MIMIC, 
            MimicEntity.createMimicAttributes()
        );
    }
    private static <T extends Entity> EntityType<T> register(
        String name, 
        FabricEntityTypeBuilder<T> type
    ) {
        return Registry.register(
            Registry.ENTITY_TYPE, 
            MimicsMod.id(name), 
            type.build()
        );
    }
    static {
        MIMIC = register(
            "mimic", 
            FabricEntityTypeBuilder
                .create(SpawnGroup.CREATURE, MimicEntity::new)
                .dimensions(EntityDimensions.changing(0.75F, 0.75F))
        );
    }
}

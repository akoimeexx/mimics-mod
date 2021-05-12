package com.akoimeexx.mimics.client.render.entity;

import com.akoimeexx.mimics.MimicsMod;
import com.akoimeexx.mimics.client.render.entity.model.MimicEntityModel;
import com.akoimeexx.mimics.entity.MimicEntity;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class MimicEntityRenderer<T extends MimicEntity> extends MobEntityRenderer<T, MimicEntityModel<T>> {
    //private final EntityRenderer disguise; 
    
    public MimicEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher, new MimicEntityModel<T>(), 0.35F);
        // disguise = BlockEntityRenderDispatcher.INSTANCE.get(
        //     BlockEntityType.CHEST.instantiate()
        // );
        
    }

    @Override
    public Identifier getTexture(T entity) {
        if (!entity.isTamed())
            return MimicsMod.id("textures/entity/mimic/tier_2.png");
        
        switch (entity.getTier()) {
            case 2:
                return MimicsMod.id("textures/entity/mimic/tier_2.png");
            case 3:
                return MimicsMod.id("textures/entity/mimic/tier_3.png");
            case 4:
                return MimicsMod.id("textures/entity/mimic/tier_4.png");
            case 5:
                return MimicsMod.id("textures/entity/mimic/tier_5.png");
            case 1:
            default:
                return MimicsMod.id("textures/entity/mimic/tier_1.png");
        }
    }
    
}

package com.akoimeexx.mimics.client.render.entity.model;

import com.akoimeexx.mimics.entity.MimicEntity;
import com.google.common.collect.ImmutableList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class MimicEntityModel<T extends MimicEntity> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart base;
    private final ModelPart lid;

    public MimicEntityModel() {
        this.textureHeight = 32;
        this.textureWidth = 32;
        
        body = new ModelPart(this);
		body.setPivot(0.0F, 24.0F, 0.0F);

        base = new ModelPart(this, 0, 0);
		base.setPivot(0.0F, -5.0F, 4.0F);
            base
                .setTextureOffset(0, 11)
                .addCuboid(-4.0F, 0.0F, -8.0F, 8.0F, 5.0F, 8.0F, 0.0F, false);
    		base
                .setTextureOffset(0, 0)
                .addCuboid(1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
    		base
                .setTextureOffset(0, 0)
                .addCuboid(-3.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
            base
                .addChild(setPivotAngle(
                    createTooth(
                        -3.5F, 0.0F, -6.0F, 
                        0.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F
                    ), 
                    -0.9599F, -0.3927F, 0.3927F
                ));
            base
                .addChild(setPivotAngle(
                    createTooth(
                        -1.0F, 0.0F, -7.5F, 
                        -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F
                    ), 
                    -0.3927F, 0.0F, -0.7854F
                ));
            base
                .addChild(setPivotAngle(
                    createTooth(
                        1.0F, 0.0F, -7.5F, 
                        -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F
                    ), 
                    -0.3927F, 0.0F, 0.7854F
                ));
            base
                .addChild(setPivotAngle(
                    createTooth(
                        3.5F, 0.0F, -6.0F, 
                        -1.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F
                    ), 
                    -0.9599F, 0.3927F, -0.3927F
                ));

        lid = new ModelPart(this);
		lid.setPivot(0.0F, -5.0F, 4.0F);
        setPivotAngle(lid, -0.3927F, 0.0F, 0.0F);
    		lid
                .setTextureOffset(0, 0)
                .addCuboid(-4.0F, -3.0F, -8.0F, 8.0F, 3.0F, 8.0F, 0.0F, false);
		    lid
                .setTextureOffset(0, 0)
                .addCuboid(1.0F, -1.0F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		    lid
                .setTextureOffset(0, 0)
                .addCuboid(-3.0F, -1.0F, -0.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
    		lid
                .setTextureOffset(0, 0)
                .addCuboid(-1.0F, -1.0F, -8.5F, 2.0F, 2.0F, 1.0F, 0.0F, false);
            lid
                .addChild(setPivotAngle(
                    createTooth(
                        -3.5F, 0.0F, -5.5F, 
                        0.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F
                    ), 
                    0.9599F, -0.3927F, -0.3927F
                ));
            lid
                .addChild(setPivotAngle(
                    createTooth(
                        -1.5F, 0.0F, -7.5F, 
                        -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F
                    ),
                    0.3927F, 0.0F, 0.7854F
                ));
            lid
                .addChild(setPivotAngle(
                    createTooth(
                        1.5F, 0.0F, -7.5F,
                        -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F
                    ),
                    0.3927F, 0.0F, -0.7854F
                ));
            lid
                .addChild(setPivotAngle(
                    createTooth(
                        3.5F, 0.0F, -5.5F, 
                        -1.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F
                    ), 
                    0.9599F, 0.3927F, 0.3927F
                ));

        body.addChild(base);
        body.addChild(lid);
    }

    @Override
    public void setAngles(
        T entity, 
        float limbAngle, 
        float limbDistance, 
        float animationProgress,
        float headYaw, 
        float headPitch
    ) {      
    }

    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of(this.lid);
    }
  
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(this.base);
    }    

    public void animateModel(T mimic, float x, float y, float z) {
        if (!mimic.isInSittingPose()) {

            if (mimic.hasAngerTime()) {
                this.lid.pitch = Math.min(MathHelper.cos(
                    (mimic.world.getTime() % 200) * -0.7854F
                ), 0);
            } else {
                if (!mimic.hasPassengers())
                    this.lid.pitch = Math.min(MathHelper.cos(
                        (mimic.world.getTime() % 200) * -0.098175F
                    ), 0);
                else 
                    this.lid.pitch = 0.0F;
            }
        }
    }
    
    private ModelPart createTooth(
        float pivotX,
        float pivotY,
        float pivotZ,
        float cubeX,
        float cubeY,
        float cubeZ,
        float sizeX,
        float sizeY,
        float sizeZ
    ) {
        ModelPart tooth = new ModelPart(this);
        tooth.setPivot(pivotX, pivotY, pivotZ);
        tooth
            .setTextureOffset(1, 0)
            .addCuboid(cubeX, cubeY, cubeZ, sizeX, sizeY, sizeZ);
        
        return tooth;
    }

    public Iterable<ModelPart> getParts() {
        return ImmutableList.of(this.lid, this.base);
    }
    private ModelPart setPivotAngle(ModelPart part, float x, float y, float z) {
        part.pitch = x;
        part.yaw = y;
        part.roll = z;
        return part;
    }

    @Override
    public void render(
        MatrixStack matrices, 
        VertexConsumer vertices, 
        int light, 
        int overlay, 
        float red, 
        float green,
        float blue, 
        float alpha
    ) {
        body.render(matrices, vertices, light, overlay);        
    }

    protected void scale(MimicEntity entity, MatrixStack matrixStack, float f) {
        // f = 0.9375F for illager
        matrixStack.scale(f, f, f);
    }    
}

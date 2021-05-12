package com.akoimeexx.mimics.mixin;

import java.util.List;
import java.util.UUID;

import com.akoimeexx.mimics.entity.MimicEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CatEntity;

@Mixin(CatEntity.class)
public abstract class CatGoalsMixin extends MobEntity {
    private CatGoalsMixin() { super(null,null); }

    @Inject(method="initGoals()V", at=@At("TAIL"))
    private void injectSitOnMimicGoal(CallbackInfo info) {
        if ((Object)this instanceof CatEntity)
            this.goalSelector.add(4, new CatSitOnMimicGoal(((CatEntity)(Object)this)));
    }

    class CatSitOnMimicGoal extends Goal {
        private final CatEntity cat;
        private MimicEntity target;

        public CatSitOnMimicGoal(CatEntity cat) { this.cat = cat; }

        @Override
        public boolean canStart() {
            if (
                !this.cat.isTamed() || 
                this.cat.isSitting() ||
                this.cat.isLeashed()
            ) return false;
            return !this.cat.world.getEntitiesByClass(
                MimicEntity.class, 
                this.cat.getBoundingBox().expand(8.0D, 8.0D, 8.0D),
                (mimic) -> {
                    UUID owner = mimic.getOwnerUuid();
                    if (owner == null)
                        return false;
                    return 
                        owner.equals(this.cat.getOwnerUuid()) && 
                        this.cat.canSee(mimic) && 
                        !mimic.hasPassengers();
                }
            ).isEmpty();
        }

        @Override
        public boolean canStop() {
            return 
                this.cat.hasVehicle() || 
                this.cat.isLeashed() || 
                target == null;
        } 

        public void start() {
            super.start();
            this.cat.setInSittingPose(false);

            List<MimicEntity> list = this.cat.world.getEntitiesByClass(
                MimicEntity.class, 
                this.cat.getBoundingBox().expand(8.0D, 8.0D, 8.0D),
                (mimic) -> {
                    UUID owner = this.cat.getOwnerUuid();
                    if (owner == null)
                        return false;
                    return 
                        owner.equals(mimic.getOwnerUuid()) && 
                        this.cat.canSee(mimic) && 
                        !mimic.hasPassengers();
                }
            );
            if (!list.isEmpty()) {
                target = list.get(0);
            }
        }
      
        public void tick() {
            super.tick();
            if (
                this.target != null && 
                !this.target.hasPassengers()
            ) {
                if (
                    this.cat
                        .getBoundingBox()
                        .expand(1.0D, 1.0D, 1.0D)
                        .intersects(this.target.getBoundingBox())
                ) {
                    this.cat.setInSittingPose(
                        this.cat.startRiding(this.target, true)
                    );
                } else {
                    this.cat.getNavigation().startMovingTo(
                        this.target, 
                        1.2000000476837158D
                    );
                }
            }
        }
    }
}
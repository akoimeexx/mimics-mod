package com.akoimeexx.mimics.entity;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.akoimeexx.mimics.inventory.TieredInventory;
import com.akoimeexx.mimics.screen.MimicScreenHandler;
import com.akoimeexx.mimics.screen.MimicScreenHandlerTypes;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.Durations;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.IntRange;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;


public class MimicEntity 
    extends TameableEntity 
    implements Angerable, NamedScreenHandlerFactory 
{
    private static final TrackedData<Integer> ANGER_TIME;
    private static final IntRange ANGER_TIME_RANGE;
    @Nullable
    protected static final TrackedData<String> FOOD_ITEM;
    protected static final TrackedData<Integer> TIER;
    protected static final TrackedData<Integer> EXPERIENCE;

    private TieredInventory inventory;
    private UUID targetUuid;

    protected MimicEntity(
        EntityType<? extends TameableEntity> entityType, 
        World world
    ) {
        super(entityType, world);
        this.experiencePoints = 15;
        this.setTamed(false);
        this.setCanPickUpLoot(true);
        this.setTier(4);
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ANGER_TIME, 0);
        this.dataTracker.startTracking(FOOD_ITEM, null);
        this.dataTracker.startTracking(TIER, 1);
        this.dataTracker.startTracking(EXPERIENCE, 0);
    }

    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new PounceAtTargetGoal(this, 0.5F));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.add(4, new EscapeDangerGoal(this, 1.2D));
        this.goalSelector.add(5, new EatItemGoal());
        this.goalSelector.add(
            8, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, true)
        );
        this.goalSelector.add(9, new WanderAroundGoal(this, 1.0D));
        this.goalSelector.add(
            10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F)
        );
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        //this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(
            2, new FollowTargetGoal<PlayerEntity>(
                this, PlayerEntity.class, 10, true, false, this::shouldAngerAt
            )
        );
        this.targetSelector.add(3, (new RevengeGoal(this, new Class[0])));
        this.targetSelector.add(
            4, new UniversalAngerGoal<MimicEntity>(this, false)
        );
    }

    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        this.angerFromTag((ServerWorld)this.world, tag);
        String food;
        if (tag.contains("Food")) {
           food = tag.getString("Food");
           if (food != null && food.trim() != "") {
               this.setFoodItem(food);
           }
        }
        this.setGainedExperience(tag.getInt("Experience"));
        this.setTier(tag.getInt("Tier"));
        this.inventory.fromTag(tag);
    }

    public void writeCustomDataToTag(CompoundTag tag) {
        super.writeCustomDataToTag(tag);
        this.angerToTag(tag);
        if (this.getFoodItem() != null && this.getFoodItem().trim() != "") {
            tag.putString("Food", this.getFoodItem());
        }
        tag.putInt("Experience", this.getGainedExperience());
        tag.putInt("Tier", this.getTier());
        this.inventory.toTag(tag);
    }

    @Override
    public ScreenHandler createMenu(
        int syncId, 
        PlayerInventory playerInventory, 
        PlayerEntity player
    ) {
        MimicScreenHandler handler;
        switch (getTier()) {
            case 2:
                handler = new MimicScreenHandler(
                    MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x3, 
                    syncId, 
                    playerInventory, 
                    this, 
                    3
                );
                break;
            case 3:
                handler = new MimicScreenHandler(
                    MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x4, 
                    syncId, 
                    playerInventory, 
                    this, 
                    4
                );
                break;
            case 4:
                handler = new MimicScreenHandler(
                    MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x5, 
                    syncId, 
                    playerInventory, 
                    this, 
                    5
                );
                break;
            case 5:
                handler = new MimicScreenHandler(
                    MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x6, 
                    syncId, 
                    playerInventory, 
                    this, 
                    6
                );
                break;
            case 1:
            default:
                handler = new MimicScreenHandler(
                    MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x2, 
                    syncId, 
                    playerInventory, 
                    this, 
                    2
                );
                break;
        }
        return handler;
    }

    @Override
    public int getAngerTime() {
        return (Integer)this.dataTracker.get(ANGER_TIME);
    }

    @Override
    public void setAngerTime(int ticks) {
        this.dataTracker.set(ANGER_TIME, ticks);
    }

    @Override
    public UUID getAngryAt() {
        return this.targetUuid;
    }

    @Override
    public void setAngryAt(UUID uuid) {
        this.targetUuid = uuid;
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.choose(this.random));
    }

    @Override
    public PassiveEntity createChild(
        ServerWorld world, 
        PassiveEntity entity
    ) {
        return null;
    }

    @Override
    public boolean isFireImmune() {
        return this.getTier() == 5 ?
            true : 
            super.isFireImmune();
    }

    // Used to override picking up items to hand slot, which is annoying.
    // May want to refactor item eat behavior to use this instead though.
    @Override
    public boolean canGather(ItemStack stack) {
        return false;
    }

    public boolean canBreatheInWater() {
        return true;
    }

    private void eatItemEntity(ItemEntity item) {
        if (item != null) {
            int points = Math.max(
                1, 
                (64 / item.getStack().getMaxCount()) * 
                    item.getStack().getCount()
            );
            this.heal(points/8);
            if (this.getTier() < 5) 
                this.setGainedExperience(
                    this.getGainedExperience() + points
                );
            if (
                this.getGainedExperience() >= getExperiencetoNextTier() && 
                this.getTier() < 5
            ) {
                this.setTier(this.getTier() + 1);
                this.setGainedExperience(0);
            }
            this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 0.15F, 1.0F);
        }
    }
    
    private int getExperiencetoNextTier() {
        int i = 128;
        return (
            i * Math.max(1, this.getTier())
        );
    }

    @Nullable
    public String getFoodItem() {
        String id = this.dataTracker.get(FOOD_ITEM);
        return (id != null && id.trim() != "") ? id : null;
    }
 
    public void setFoodItem(@Nullable String id) {
        this.dataTracker.set(
           FOOD_ITEM, 
           (id != null && id.trim() != "") ? id : null
        );
    }

    public int getGainedExperience() {
        return (Integer)this.dataTracker.get(EXPERIENCE);
    }

    public void setGainedExperience(int experience) {
        this.dataTracker.set(EXPERIENCE, experience);
    }

    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.world.isClient) {
            return (
                this.isOwner(player) || 
                this.isTamed() || 
                !this.isTamed() && 
                !this.hasAngerTime()
            ) ? 
                ActionResult.CONSUME : 
                ActionResult.PASS;
        } else {
            if (
                !this.isTamed() && 
                !this.hasAngerTime()
            ) {
                this.setAngryAt(player.getUuid());
                this.chooseRandomAngerTime();
            } else if (
                this.isTamed() && 
                this.getOwnerUuid().equals(
                    player.getUuid()
                )
            ) {
                if (
                    player.isSneaking() && 
                    ((ServerPlayerEntity)player)
                        .getStackInHand(hand)
                        .isEmpty() 
                ) {
                    ItemStack stack = new ItemStack(
                        Registry.ITEM.get(
                            new Identifier("mimics:mimic")
                        ), 
                        1
                    );
                    CompoundTag data = new CompoundTag();
                    this.writeCustomDataToTag(data);
                    CompoundTag itemData = new CompoundTag();
                    itemData.put("MimicData", data);
                    stack.setTag(itemData);
                    player.setStackInHand(
                        hand, 
                        stack
                    );
                    this.remove();
                    return ActionResult.SUCCESS;
                } else {
                    player.openHandledScreen(this);
                    return ActionResult.CONSUME;
                }
            }
        }
        return super.interactMob(player, hand);
    }

    public TieredInventory getInventory() {
        return this.inventory;
    }
    
    public int getInventorySize() {
        return 9 + (9 * this.getTier());
    }

    public boolean canPlayerUse(PlayerEntity player) {
        return (
            !this.removed && 
            player.squaredDistanceTo(this) <= 64.0D
        );
    }

    private Box getItemDetectionRange() {
        return this
            .getBoundingBox()
            .expand(16.0D, 8.0D, 16.0D);
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.BLOCK_WOOD_STEP, 0.15F, 1.0F);
    }

    public void remove() {
        if (!this.world.isClient && this.dead) {
           ItemScatterer.spawn(this.world, (Entity)this, this.inventory);
        }
        super.remove();
    }

    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
        if (tamed) {
            this.setTier(1);
        } else {
            this.setTier(4);
        }
    }

    public int getTier() {
        return (int)this.dataTracker.get(TIER);
    }

    public void setTier(int tier) {
        this.dataTracker.set(TIER, Math.max(1, Math.min(5, tier)));
        TieredInventory i = new TieredInventory(9, 9, this.getTier());
        if (this.inventory != null) 
            for (int index = 0; index < this.inventory.size(); ++index)
                i.addToInventory(this.inventory.getStack(index));
        this.inventory = i;
    }

    public void updatePassengerPosition(Entity passenger) {
        super.updatePassengerPosition(passenger);
        passenger.updatePosition(
            passenger.getX(), 
            passenger.getY() - 0.175, 
            passenger.getZ()
        );
        if (passenger instanceof TameableEntity) 
            ((TameableEntity)passenger).setSitting(true);
    }

    // Used for specifying leash position
    @Environment(EnvType.CLIENT)
    public Vec3d method_29919() {
       return new Vec3d(
           0.0D, 
           (double)(0.4F * this.getStandingEyeHeight()), 
           0.0D
        );
    }

    public static DefaultAttributeContainer.Builder createMimicAttributes() {
        return MobEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30000001192092896D)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.2D)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D);
    }

    class EatItemGoal extends Goal {
        public EatItemGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (!MimicEntity.this.canPickUpLoot())
                return false;
            List<ItemEntity> list = MimicEntity.this.world.getEntitiesByClass(
                ItemEntity.class, 
                MimicEntity.this.getItemDetectionRange(), 
                (itemEntity) -> { return isValidTarget(itemEntity); }
            );
            if (!list.isEmpty()) {
                for (ItemEntity item : list) {
                    if (
                        MimicEntity.this.canSee(item) && 
                        MimicEntity.this.inventory.canInsert(
                            item.getStack()
                        )
                    ) return true;
                }
            }
            return false;
        }

        private boolean isValidTarget(ItemEntity target) {
            return 
                !target.cannotPickup() && 
                target.isAlive() && (
                    !MimicEntity.this.isTamed() || 
                    target.getThrower() == null || 
                    target.getThrower().equals(
                        MimicEntity.this.getOwnerUuid()
                    )
                );
        }

        public void start() {
            List<ItemEntity> list = MimicEntity.this.world.getEntitiesByClass(
                ItemEntity.class, 
                MimicEntity.this.getItemDetectionRange(), 
                (itemEntity) -> { return isValidTarget(itemEntity); }
            );
            if (!list.isEmpty()) {
                Entity target = null;
                for (ItemEntity item : list) {
                    if (
                        MimicEntity.this.canSee(item) && 
                        MimicEntity.this.inventory.canInsert(
                            item.getStack()
                        )
                    ) {
                        target = item;
                        break;
                    }
                }
                if (target != null)
                MimicEntity.this
                    .getNavigation()
                    .startMovingTo(
                        target, 
                        1.2000000476837158D
                    );
            }
        }

        public void tick() {
            List<ItemEntity> list = MimicEntity.this.world.getEntitiesByClass(
                ItemEntity.class, 
                MimicEntity.this.getBoundingBox()
                    .expand(1.0D, 8.0D, 1.0D), 
                (itemEntity) -> { return isValidTarget(itemEntity); }
            );
            if (!list.isEmpty()) {
                for (ItemEntity item : list) {
                    if (
                        item.getStack().getItem().toString() == 
                            MimicEntity.this.getFoodItem()
                    ) {
                        MimicEntity.this.eatItemEntity(item);
                        item.kill();
                    } else if (MimicEntity.this.inventory.canInsert(
                        item.getStack()
                    )) {
                        MimicEntity.this.inventory.addToInventory(item.getStack());
                        item.kill();
                    }
                }
            } else {
                list = MimicEntity.this.world.getEntitiesByClass(
                    ItemEntity.class, 
                    MimicEntity.this.getItemDetectionRange(), 
                    (itemEntity) -> { return isValidTarget(itemEntity); }
                );
                if (!list.isEmpty()) {
                    Entity target = null;
                    for (ItemEntity item : list) {
                        if (
                            MimicEntity.this.canSee(item) && 
                            MimicEntity.this.inventory.canInsert(
                                item.getStack()
                            )
                        ) {
                            target = item;
                            break;
                        }
                    }
                    if (target != null)
                    MimicEntity.this
                        .getNavigation()
                        .startMovingTo(
                            target, 
                            1.2000000476837158D
                        );
                }
            }
        }
    }

    static {
        ANGER_TIME = DataTracker.registerData(
            MimicEntity.class, 
            TrackedDataHandlerRegistry.INTEGER
        );
        ANGER_TIME_RANGE = Durations.betweenSeconds(20, 60);
        FOOD_ITEM = DataTracker.registerData(
            MimicEntity.class, 
            TrackedDataHandlerRegistry.STRING
        );
        TIER = DataTracker.registerData(
            MimicEntity.class, 
            TrackedDataHandlerRegistry.INTEGER
        );
        EXPERIENCE = DataTracker.registerData(
            MimicEntity.class, 
            TrackedDataHandlerRegistry.INTEGER
        );
    }
}

// public class MimicEntity extends TameableEntity implements Angerable, Inventory, NamedScreenHandlerFactory {
//     class HideGoal extends Goal {
//         @Override
//         public boolean canStart() {
//             return false;
//         }
//     }
// public class MimicEntity extends ... {

//     // @Environment(EnvType.CLIENT)
//     // public static enum STATES {
//     //     IDLE, 
//     //     HIDING,
//     //     MOVING,
//     //     EATING,
//     //     ATTACKING
//     // }
// }
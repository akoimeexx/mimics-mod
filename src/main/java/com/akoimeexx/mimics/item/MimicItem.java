package com.akoimeexx.mimics.item;

import com.akoimeexx.mimics.entity.EntityTypes;
import com.akoimeexx.mimics.entity.MimicEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class MimicItem extends Item {

    public MimicItem(Settings settings) {
        super(settings);
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!(world instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }

        ItemStack stack = context.getStack().copy();
        BlockPos position = context.getBlockPos();
        Direction direction = context.getSide();
        BlockState state = world.getBlockState(position);

        MimicEntity mimic = EntityTypes.MIMIC.create(
            (ServerWorld)world, 
            null, 
            null, 
            context.getPlayer(), 
            state.getCollisionShape(world, position).isEmpty() ? 
                position : 
                position.offset(direction), 
            SpawnReason.COMMAND, 
            true, 
            !position.equals(position.offset(direction)) && 
                direction == Direction.UP
        );
        CompoundTag data = stack.getOrCreateSubTag("MimicData");
        if (!data.contains("Owner"))
            data.putUuid("Owner", context.getPlayer().getUuid());
        mimic.setTamed(true);
        if (!data.contains("Tier"))
            data.putInt("Tier", 1);
        
        mimic.readCustomDataFromTag(data);
        if (
            world.spawnEntity(mimic) && 
            !context.getPlayer().abilities.creativeMode
        ) {
            context.getStack().decrement(1);
            context.getPlayer().setStackInHand(
                context.getHand(), 
                ItemStack.EMPTY
            );
        }
        return ActionResult.CONSUME;
    }
}

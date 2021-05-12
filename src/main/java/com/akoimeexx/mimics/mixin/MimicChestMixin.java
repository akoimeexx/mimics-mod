package com.akoimeexx.mimics.mixin;

import com.akoimeexx.mimics.entity.EntityTypes;
import com.akoimeexx.mimics.entity.MimicEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(ChestBlock.class)
public class MimicChestMixin {
    @Inject(
        method="onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
        at=@At("HEAD"),
        cancellable=true
    )
    private void mimickedChestChancifier(
        BlockState state, 
        World world, 
        BlockPos position, 
        PlayerEntity player, 
        Hand hand, 
        BlockHitResult hit,
        CallbackInfoReturnable<ActionResult> info
    ) {
        if (world instanceof ServerWorld) {
            ChestBlockEntity chest = (ChestBlockEntity)world.getBlockEntity(position);
            Identifier lootTable = ((LootableAccessorMixin) ((LootableContainerBlockEntity)chest))
                .getLootTableId();
            long lootSeed = ((LootableAccessorMixin) ((LootableContainerBlockEntity)chest))
                .getLootTableSeed();
            if (
                player.getRandom().nextInt(128) < 16 && 
                lootTable != null
            ) {
                MimicEntity mimic = EntityTypes.MIMIC.create(
                    (ServerWorld)world, 
                    null, 
                    null, 
                    player, 
                    position, 
                    SpawnReason.NATURAL, 
                    true, 
                    false
                );
                mimic.setAngryAt(player.getUuid());
                LootTable loot = world
                    .getServer()
                    .getLootManager()
                    .getTable(lootTable);
                
                LootContext.Builder builder = (
                    new LootContext.Builder((ServerWorld)world)
                ).parameter(
                    LootContextParameters.ORIGIN, 
                    Vec3d.ofCenter(position)
                ).random(lootSeed);
                if (player != null) {
                   builder.luck(player.getLuck()).parameter(
                       LootContextParameters.THIS_ENTITY, 
                       player
                    );
                }
       
                loot.supplyInventory(
                    mimic.getInventory(), 
                    builder.build(LootContextTypes.CHEST)
                );

                ((ServerWorld)world).removeBlockEntity(position);
                ((ServerWorld)world).removeBlock(position, false);

                ((ServerWorld)world).spawnEntity(mimic);

                info.setReturnValue(ActionResult.FAIL);
            }
        }
    }    
}

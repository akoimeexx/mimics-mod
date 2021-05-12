package com.akoimeexx.mimics.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.util.Identifier;

@Mixin(LootableContainerBlockEntity.class)
public interface LootableAccessorMixin {
    @Accessor
    @Nullable
    Identifier getLootTableId();

    @Accessor 
    long getLootTableSeed();
}

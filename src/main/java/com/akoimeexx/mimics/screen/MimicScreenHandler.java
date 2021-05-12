package com.akoimeexx.mimics.screen;

import java.util.Optional;

import com.akoimeexx.mimics.entity.MimicEntity;
import com.akoimeexx.mimics.inventory.TieredInventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public class MimicScreenHandler extends ScreenHandler {
    public final Slot foodSlot;
    private final MimicEntity mimic;
    private final int rows;
    private final TieredInventory inventory;

    protected MimicScreenHandler(
        ScreenHandlerType<?> type, 
        int syncId, 
        PlayerInventory inventory, 
        int rows
    ) {
        this(type, syncId, inventory, null, rows);
    }
    public MimicScreenHandler(
        ScreenHandlerType<?> type, 
        int syncId, 
        PlayerInventory playerInventory, 
        MimicEntity mimic,
        int rows
    ) {
        super(type, syncId);
        this.mimic = mimic;
        this.rows = rows;
        this.inventory = this.mimic != null ?
            this.mimic.getInventory() :
            new TieredInventory(9, 9, Math.max(this.rows - 1, 0));
        checkSize(this.inventory, 9 * this.rows);

        this.inventory.onOpen(playerInventory.player);

        // Inventory rows
        for (
            int y = 0; y < this.rows; ++y
        ) for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(
                this.inventory, 
                x + y * 9, 
                8 + x * 18, 
                18 + y * 18
            ));
        }
        // Mimic FoodItem Slot
        this.foodSlot = new Slot(
            new SimpleInventory(1), 
            0, 
            172, 
            this.rows * 18
        );
        this.addSlot(this.foodSlot);        
        if (this.mimic != null) {
            this.addListener(new FoodItemListener());
            if (this.mimic.getFoodItem() != null)
                this.foodSlot.setStack(this.getFoodItemStack());
        }
        // Player inventory rows
        for (
            int y = 0; y < 3; ++y
        ) for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(
                playerInventory, 
                x + y * 9 + 9, 
                8 + x * 18, 
                (32 + this.rows * 18) + y * 18
            ));
        }
        // Player hotbar row
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(
                playerInventory, 
                x, 
                8 + x * 18, 
                (90 + this.rows * 18)
            ));
        }

    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack onSlotClick(
        int slotId, 
        int clickDelta, 
        SlotActionType actionType, 
        PlayerEntity player
    ) {
        if (
            this.foodSlot != null && 
            slotId == this.foodSlot.id
        ) {
            if (!player.inventory.getCursorStack().isEmpty()) {
                this.foodSlot.setStack(
                    player.inventory
                        .getCursorStack()
                        .copy()
                        .split(1)
                );
                this.setFoodItem(
                    this.foodSlot.getStack()
                );
            } else {
                this.foodSlot
                    .setStack(ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        }
        return super.onSlotClick(slotId, clickDelta, actionType, player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            itemStack = slotStack.copy();
            if (index < this.rows * 9) {
                if (!this.insertItem(
                    slotStack, 
                    this.rows * 9 + 1, 
                    this.slots.size(), 
                    true
                )) return ItemStack.EMPTY;
            } else if (!this.insertItem(slotStack, 0, this.rows * 9, false)) {
                return ItemStack.EMPTY;
            }
  
            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return itemStack;
    }
    
    public ItemStack getFoodItemStack() {
        ItemStack stack = ItemStack.EMPTY;
        if (this.mimic != null && this.mimic.getFoodItem() != null) {
            Optional<Item> item = Registry.ITEM.getOrEmpty(
                new Identifier(this.mimic.getFoodItem())
            );
            stack = item.isPresent() ?
                new ItemStack(item.get()) :
                ItemStack.EMPTY;
        }
        return stack;
    }

    public void setFoodItem(ItemStack stack) {
        if (this.mimic != null)
            this.mimic.setFoodItem(
                (stack != null && !stack.isEmpty()) ?
                    stack.getItem().toString() : 
                    null
            );
    }

    public int getRows() {
        return this.rows;
    }    

    public static MimicScreenHandler createTier1ScreenHandler(
        int syncId, 
        PlayerInventory playerInventory
    ) {
        return new MimicScreenHandler(
            MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x2, 
            syncId, 
            playerInventory, 
            2
        );
    }

    public static MimicScreenHandler createTier2ScreenHandler(
        int syncId, 
        PlayerInventory playerInventory
    ) {
        return new MimicScreenHandler(
            MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x3, 
            syncId, 
            playerInventory, 
            3
        );
    }

    public static MimicScreenHandler createTier3ScreenHandler(
        int syncId, 
        PlayerInventory playerInventory
    ) {
        return new MimicScreenHandler(
            MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x4, 
            syncId, 
            playerInventory, 
            4
        );
    }

    public static MimicScreenHandler createTier4ScreenHandler(
        int syncId, 
        PlayerInventory playerInventory
    ) {
        return new MimicScreenHandler(
            MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x5, 
            syncId, 
            playerInventory, 
            5
        );
    }

    public static MimicScreenHandler createTier5ScreenHandler(
        int syncId, 
        PlayerInventory playerInventory
    ) {
        return new MimicScreenHandler(
            MimicScreenHandlerTypes.MIMIC_SCREEN_HANDLER_9x6, 
            syncId, 
            playerInventory, 
            6
        );
    }

    private class FoodItemListener implements ScreenHandlerListener {
        @Override
        public void onHandlerRegistered(
            ScreenHandler handler, DefaultedList<ItemStack> stacks
        ) {
            ((MimicScreenHandler)handler).foodSlot.setStack(
                ((MimicScreenHandler)handler).getFoodItemStack()
            );
        }

        @Override
        public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
            if (slotId == ((MimicScreenHandler)handler).foodSlot.id)
                ((MimicScreenHandler)handler).setFoodItem(stack);
        }

        @Override
        public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}
    }
}
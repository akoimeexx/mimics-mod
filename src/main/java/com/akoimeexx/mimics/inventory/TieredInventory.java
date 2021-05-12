package com.akoimeexx.mimics.inventory;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.collection.DefaultedList;

/**
 * Project-specific implementation of @{link Inventory} that provides more 
 * robust handling of ItemStack manipulation and management than 
 * @{link net.minecraft.inventory.SimpleInventory}, as well as dynamic
 * inventory size based on "tiers".
 * 
 * Heavily ripped from SimpleInventory
 */
public class TieredInventory implements Inventory {
    private int base;
    private int slotsPerTier;
    private int tier;
    private DefaultedList<ItemStack> inventory;
    private List<InventoryChangedListener> listeners;
    
    public TieredInventory() {
        this(9);
    }

    public TieredInventory(int base) {
        this(base, base);
    }

    public TieredInventory(int base, int slotsPerTier) {
        this(base, slotsPerTier, 0);
    }

    public TieredInventory(int base, int slotsPerTier, int currentTier) {
        this.base = base;
        this.slotsPerTier = slotsPerTier;
        this.tier = currentTier;

        this.inventory = DefaultedList.ofSize(this.
            size(), ItemStack.EMPTY
        );
    }

    @Override
    public void clear() {
        this.inventory.clear();
        this.markDirty();
    }

    @Override
    public int size() {
        return this.base + (this.slotsPerTier * this.getTier());
    }

    @Override
    public boolean isEmpty() {
        Iterator<ItemStack> iterator = this.inventory.iterator();
        ItemStack itemStack;
        do {
           if (!iterator.hasNext()) {
              return true;
           }
             itemStack = (ItemStack)iterator.next();
        } while(itemStack.isEmpty());
        return false;
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot >= 0 && slot < this.size() ?
            (ItemStack)this.inventory.get(slot) : 
            ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = Inventories.splitStack(this.inventory, slot, amount);
        if (!stack.isEmpty())
            this.markDirty();
        return stack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack itemStack = (ItemStack)this.inventory.get(slot);
        if (itemStack.isEmpty()) {
           return ItemStack.EMPTY;
        } else {
           this.inventory.set(slot, ItemStack.EMPTY);
           markDirty();
           return itemStack;
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack())
            stack.setCount(this.getMaxCountPerStack());
        this.markDirty();
    }

    @Override
    public void markDirty() {
        if (this.listeners != null) {
            Iterator<InventoryChangedListener> iterator = 
                this.listeners.iterator();
   
            while (iterator.hasNext()) {
                InventoryChangedListener listener = 
                    (InventoryChangedListener)iterator.next();
                listener.onInventoryChanged(this);
            }
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    private boolean canCombine(ItemStack comparator, ItemStack comparand) {
        return 
            comparator.getItem() == comparand.getItem() && 
            ItemStack.areTagsEqual(comparator, comparand);
    }

    public boolean canInsert(ItemStack stack) {
        boolean b = false;
        Iterator<ItemStack> iterator = this.inventory.iterator();
  
        while (iterator.hasNext() && !b) {
            ItemStack itemStack = iterator.next();
            b = itemStack.isEmpty() || 
            this.canCombine(itemStack, stack) && 
            itemStack.getCount() < itemStack.getMaxCount();
        }
        return b;
    } 

    /**
     * Adds {@link ItemStack} stack to {@link DefaultedList} this.inventory, combining with any existing 
     * ItemStacks that can be merged with and/or using first available empty 
     * slot in the inventory
     * @param stack
     */
    public void addToInventory(ItemStack stack) {
        for (int i = 0; i < this.size(); ++i) {
            ItemStack itemStack = this.getStack(i);
            if (this.canCombine(itemStack, stack)) {
                this.transfer(stack, itemStack);
                if (stack.isEmpty()) {
                    this.markDirty();
                    return;
                }
            } else if (itemStack.isEmpty()) {
                this.setStack(i, stack.copy());
                stack.setCount(0);
                this.markDirty();
                return;
            }
        }
    }
    
    private void transfer(ItemStack source, ItemStack target) {
        int transferAmount = Math.min(
            source.getCount(), 
            Math.min(
                this.getMaxCountPerStack(), 
                target.getMaxCount()
            ) - target.getCount()
        );
        if (transferAmount > 0) {
            target.increment(transferAmount);
            source.decrement(transferAmount);
            this.markDirty();
        }
    }

    public ItemStack removeItem(Item item, int count) {
        ItemStack targetItems = new ItemStack(item, 0);
  
        for(int i = this.size() - 1; i >= 0; --i) {
            ItemStack inventoryStack = this.getStack(i);
            if (inventoryStack.getItem().equals(item)) {
                int j = count - targetItems.getCount();
                ItemStack itemStack3 = inventoryStack.split(j);
                targetItems.increment(itemStack3.getCount());
                if (targetItems.getCount() == count) {
                    break;
                }
            }
        }
        if (!targetItems.isEmpty()) {
           this.markDirty();
        }
        return targetItems;
     }

    public void addListener(InventoryChangedListener listener) {
        if (this.listeners == null) {
           this.listeners = Lists.newArrayList();
        }
        this.listeners.add(listener);
    }
  
    public void removeListener(InventoryChangedListener listener) {
        this.listeners.remove(listener);
    }
    
    public void fromTag(CompoundTag tag) {
        Inventories.fromTag(tag, this.inventory);
    }

    public void toTag(CompoundTag tag) {
        if (this.inventory != null)
            Inventories.toTag(tag, this.inventory);
    }

    public int getTier() {
        return this.tier;
    }

    public void setTier(int tier) {
        if (tier >= 0) {
            this.tier = tier;
            this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        }
    }
}

package com.akoimeexx.mimics.client.gui.screen.ingame;

import com.akoimeexx.mimics.MimicsMod;
import com.akoimeexx.mimics.screen.MimicScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

/*
Note: /home/akoimeexx/code/mimics/src/main/java/com/akoimeexx/mimics/client/gui/screen/ingame/MimicScreen.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
*/
@Environment(EnvType.CLIENT)
public class MimicScreen extends HandledScreen<MimicScreenHandler> {
    private static final Text FOOD_ITEM_SLOT_TEXT;
    private static final Identifier TEXTURE_TIER_1;
    private static final Identifier TEXTURE_TIER_2;
    private static final Identifier TEXTURE_TIER_3;
    private static final Identifier TEXTURE_TIER_4;
    private static final Identifier TEXTURE_TIER_5;

    private final int rows;
    

    public MimicScreen(
        MimicScreenHandler handler, 
        PlayerInventory inventory, 
        Text title
    ) {
        super(handler, inventory, title);
        this.rows = handler.getRows();
        this.backgroundWidth = 194;
        this.backgroundHeight = 114 + this.rows * 18;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        switch (this.rows) {
            case 3:
                client.getTextureManager().bindTexture(TEXTURE_TIER_2);
                break;
            case 4:
                client.getTextureManager().bindTexture(TEXTURE_TIER_3);
                break;
            case 5:
                client.getTextureManager().bindTexture(TEXTURE_TIER_4);
                break;
            case 6:
                client.getTextureManager().bindTexture(TEXTURE_TIER_5);
                break;
            case 2:
            default:
                client.getTextureManager().bindTexture(TEXTURE_TIER_1);
        }
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
        
        if (
            this.handler.foodSlot != null && 
            this.isPointWithinBounds(
                this.handler.foodSlot.x, 
                this.handler.foodSlot.y, 
                16, 
                16, 
                (double)mouseX, 
                (double)mouseY
            ) && 
            this.handler.foodSlot.getStack() == ItemStack.EMPTY
        ) {
            this.renderTooltip(matrices, FOOD_ITEM_SLOT_TEXT, mouseX, mouseY);
        }
    }
    
    static {
        TEXTURE_TIER_1 = MimicsMod.id("textures/gui/container/mimic/tier_1.png");
        TEXTURE_TIER_2 = MimicsMod.id("textures/gui/container/mimic/tier_2.png");
        TEXTURE_TIER_3 = MimicsMod.id("textures/gui/container/mimic/tier_3.png");
        TEXTURE_TIER_4 = MimicsMod.id("textures/gui/container/mimic/tier_4.png");
        TEXTURE_TIER_5 = MimicsMod.id("textures/gui/container/mimic/tier_5.png");

        FOOD_ITEM_SLOT_TEXT = new TranslatableText(
            "inventory.mimics.food_slot.tooltip"
        );
    }
}

package net.bluebunnex.pixelgirls.mixin;

import net.bluebunnex.pixelgirls.DialogueContainer;
import net.bluebunnex.pixelgirls.entity.WomanEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.ScreenScaler;
import net.minecraft.util.hit.HitResultType;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
@Environment(EnvType.CLIENT)
public class InGameHudMixin {

    @Shadow
    private static ItemRenderer ITEM_RENDERER = new ItemRenderer();

    @Shadow
    private Minecraft minecraft;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderMixin(float tickDelta, boolean screenOpen, int mouseX, int mouseY, CallbackInfo ci) {

        ScreenScaler scaler = new ScreenScaler(this.minecraft.options, this.minecraft.displayWidth, this.minecraft.displayHeight);
        int cx = scaler.getScaledWidth() / 2;
        int cy = scaler.getScaledHeight() / 2;

        TextRenderer textRenderer = this.minecraft.textRenderer;

        // render dialogue
        String dialogue = ((DialogueContainer) (Object) this.minecraft.player).getDialogue();

        textRenderer.drawWithShadow(dialogue, cx - textRenderer.getWidth(dialogue) / 2, cy + 20, -1);

        // tooltip on woman hover
        if (
            this.minecraft.crosshairTarget != null
            && this.minecraft.crosshairTarget.type == HitResultType.ENTITY
            && this.minecraft.crosshairTarget.entity instanceof WomanEntity woman
        ) {

            // render tooltip
            {
                GL11.glPushMatrix();

                int x1, x2, y1, y2;

                Tessellator tessellator = Tessellator.INSTANCE;
                GL11.glEnable(3042);
                GL11.glDisable(3553);
                GL11.glBlendFunc(770, 771);

                // box behind text to make it easier to read (stolen from DrawContext)
                x1 = 2; x2 = 64;
                y1 = 2; y2 = 30;

                GL11.glColor4f(0f, 0f, 0.03f, 0.5f);
                tessellator.startQuads();
                tessellator.vertex((double) x1, (double) y2, 0.0);
                tessellator.vertex((double) x2, (double) y2, 0.0);
                tessellator.vertex((double) x2, (double) y1, 0.0);
                tessellator.vertex((double) x1, (double) y1, 0.0);
                tessellator.draw();

                // health bar
                x1 += 1; x2 -= 1;
                x2 -= x1;
                x2 = x2 * woman.health / woman.maxHealth;
                x2 += x1;
                y2 = ++y1; y2++;

                GL11.glColor4f(1f, 0f, 0f, 1f);
                tessellator.startQuads();
                tessellator.vertex((double) x1, (double) y2, 0.0);
                tessellator.vertex((double) x2, (double) y2, 0.0);
                tessellator.vertex((double) x2, (double) y1, 0.0);
                tessellator.vertex((double) x1, (double) y1, 0.0);
                tessellator.draw();

                GL11.glEnable(3553);
                GL11.glDisable(3042);

                GL11.glPopMatrix();

                // text
                ITEM_RENDERER.renderGuiItem(
                        textRenderer,
                        this.minecraft.textureManager,
                        woman.favouriteItem.id,
                        0,
                        woman.favouriteItem.getTextureId(0),
                        37,
                        12
                );
                GL11.glDisable(2896); // renderGuiItem is destructive
                GL11.glDisable(2884);

                textRenderer.drawWithShadow(woman.name, 4, 6, -1);
                textRenderer.drawWithShadow("Loves:", 4, 18, -7829368);
            }
        }
    }
}

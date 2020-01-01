
package software.tachyon.starfruit.utility;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.Rotation3;

public class DrawUtility {
    public static void drawCenteredString(TextRenderer textRenderer, String str, double centerX, double y, int color) {
        textRenderer.drawWithShadow(str, (float) (centerX - textRenderer.getStringWidth(str) / 2), (float) y, color);
    }

    public static void drawRightAlignedString(TextRenderer textRenderer, String str, double rightX, double y,
            int color) {
        textRenderer.drawWithShadow(str, (float) (rightX - textRenderer.getStringWidth(str)), (float) y, color);
    }

    public static void fill(double x1, double y1, double x2, double y2, int color) {
        fill(Rotation3.identity().getMatrix(), x1, y1, x2, y2, color);
    }

    public static void fill(Matrix4f matrix4f, double x1, double y1, double x2, double y2, int color) {
        double j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}

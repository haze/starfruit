package software.tachyon.starfruit.mixin.gui;

import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {

//    @Shadow
//    @Final
//    private FontStorage fontStorage;

    @Shadow
    protected abstract void drawGlyph(GlyphRenderer glyphRenderer, boolean bold, boolean italic, float weight, float x,
                                      float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha,
                                      int light);

    @Shadow
    public abstract int getWidth(String text);

    @Shadow
    protected abstract float drawLayer(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int underlineColor, int light);
}

//    @Overwrite
//    public int getWidth(String ogText) {
//        if (ogText == null) {
//            return 0;
//        } else {
//            final String text = StarfruitMod.getFriends().normalizeString(ogText, false);
//            float f = 0.0F;
//            boolean bl = false;
//
//            for (int i = 0; i < text.length(); ++i) {
//                char c = text.charAt(i);
//                if (c == HexShift.CATALYST_CHAR) {
//                    final boolean isValidHexString = i + HexShift.SHIFT_DISTANCE <= text.length();
//                    if (isValidHexString) {
//                        i += HexShift.SHIFT_DISTANCE - 1;
//                    }
//                } else if (c == HexShift.IRIDESCENCE_CHAR) {
//                    ++i;
//                } else if (c == 167 && i < text.length() - 1) {
//                    ++i;
//                    Formatting formatting = Formatting.byCode(text.charAt(i));
//                    if (formatting == Formatting.BOLD) {
//                        bl = true;
//                    } else if (formatting != null && formatting.affectsGlyphWidth()) {
//                        bl = false;
//                    }
//                } else {
//                    f += this.fontStorage.getGlyph(c).getAdvance(bl);
//                }
//            }
//
//            return MathHelper.ceil(f);
//        }
//    }
//
//    @Overwrite
//    private float drawLayer(String ogText, float x, float y, int color, boolean shadow, Matrix4f matrix,
//            VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int underlineColor, int light) {
//
//        final String text = StarfruitMod.getFriends().normalizeString(ogText, true);
//        float f = shadow ? 0.25F : 1.0F;
//        float g = (float) (color >> 16 & 255) / 255.0F * f;
//        float h = (float) (color >> 8 & 255) / 255.0F * f;
//        float i = (float) (color & 255) / 255.0F * f;
//        float j = x;
//        float k = g;
//        float l = h;
//        float m = i;
//        float n = (float) (color >> 24 & 255) / 255.0F;
//        boolean bl = false;
//        boolean bl2 = false;
//        boolean bl3 = false;
//        boolean bl4 = false;
//        boolean bl5 = false;
//
//        final int hexShiftLength = 6;
//        List<GlyphRenderer.Rectangle> list = Lists.newArrayList();
//
//        for (int o = 0; o < text.length(); ++o) {
//            char c = text.charAt(o);
//            if (c == HexShift.CATALYST_CHAR) {
//                final Optional<HexShift> optionalHexShift = HexShift.parseHex(o, text);
//                if (optionalHexShift.isPresent()) {
//                    final HexShift shift = optionalHexShift.get();
//                    k = shift.r * f;
//                    l = shift.g * f;
//                    m = shift.b * f;
//                    o += hexShiftLength;
//                }
//            } else if (c == HexShift.IRIDESCENCE_CHAR) {
//                final int iridescence = StarfruitMod.getGlobalIridescence().getRGB();
//                k = f * (float) (iridescence >> 16 & 255) / 255.0F;
//                l = f * (float) (iridescence >> 8 & 255) / 255.0F;
//                m = f * (float) (iridescence & 255) / 255.0F;
//            } else if (c == 167 && o + 1 < text.length()) {
//                Formatting formatting = Formatting.byCode(text.charAt(o + 1));
//                if (formatting != null) {
//                    if (formatting.affectsGlyphWidth()) {
//                        bl = false;
//                        bl2 = false;
//                        bl5 = false;
//                        bl4 = false;
//                        bl3 = false;
//                        k = g;
//                        l = h;
//                        m = i;
//                    }
//
//                    if (formatting.getColorValue() != null) {
//                        int p = formatting.getColorValue();
//                        k = (float) (p >> 16 & 255) / 255.0F * f;
//                        l = (float) (p >> 8 & 255) / 255.0F * f;
//                        m = (float) (p & 255) / 255.0F * f;
//                    } else if (formatting == Formatting.OBFUSCATED) {
//                        bl = true;
//                    } else if (formatting == Formatting.BOLD) {
//                        bl2 = true;
//                    } else if (formatting == Formatting.STRIKETHROUGH) {
//                        bl5 = true;
//                    } else if (formatting == Formatting.UNDERLINE) {
//                        bl4 = true;
//                    } else if (formatting == Formatting.ITALIC) {
//                        bl3 = true;
//                    }
//                }
//
//                ++o;
//            } else {
//                Glyph glyph = this.fontStorage.getGlyph(c);
//                GlyphRenderer glyphRenderer = bl && c != ' ' ? this.fontStorage.getObfuscatedGlyphRenderer(glyph)
//                        : this.fontStorage.getGlyphRenderer(c);
//                float s;
//                float t;
//                if (!(glyphRenderer instanceof EmptyGlyphRenderer)) {
//                    s = bl2 ? glyph.getBoldOffset() : 0.0F;
//                    t = shadow ? glyph.getShadowOffset() : 0.0F;
//                    VertexConsumer vertexConsumer = vertexConsumerProvider
//                            .getBuffer(glyphRenderer.method_24045(seeThrough));
//                    this.drawGlyph(glyphRenderer, bl2, bl3, s, j + t, y + t, matrix, vertexConsumer, k, l, m, n, light);
//                }
//
//                s = glyph.getAdvance(bl2);
//                t = shadow ? 1.0F : 0.0F;
//                if (bl5) {
//                    list.add(new GlyphRenderer.Rectangle(j + t - 1.0F, y + t + 4.5F, j + t + s, y + t + 4.5F - 1.0F,
//                            -0.01F, k, l, m, n));
//                }
//
//                if (bl4) {
//                    list.add(new GlyphRenderer.Rectangle(j + t - 1.0F, y + t + 9.0F, j + t + s, y + t + 9.0F - 1.0F,
//                            -0.01F, k, l, m, n));
//                }
//
//                j += s;
//            }
//        }
//
//        if (underlineColor != 0) {
//            float u = (float) (underlineColor >> 24 & 255) / 255.0F;
//            float v = (float) (underlineColor >> 16 & 255) / 255.0F;
//            float w = (float) (underlineColor >> 8 & 255) / 255.0F;
//            float z = (float) (underlineColor & 255) / 255.0F;
//            list.add(new GlyphRenderer.Rectangle(x - 1.0F, y + 9.0F, j + 1.0F, y - 1.0F, 0.01F, v, w, z, u));
//        }
//
//        if (!list.isEmpty()) {
//            GlyphRenderer glyphRenderer2 = this.fontStorage.getRectangleRenderer();
//            VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(glyphRenderer2.getLayer(seeThrough));
//            Iterator<?> var39 = list.iterator();
//
//            while (var39.hasNext()) {
//                GlyphRenderer.Rectangle rectangle = (GlyphRenderer.Rectangle) var39.next();
//                glyphRenderer2.drawRectangle(rectangle, matrix, vertexConsumer2, light);
//            }
//        }
//
//        return j;
//    }
//}

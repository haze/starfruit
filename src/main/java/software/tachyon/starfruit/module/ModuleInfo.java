package software.tachyon.starfruit.module;

import net.minecraft.text.MutableText;
import software.tachyon.starfruit.StarfruitMod;

import java.awt.*;

import static software.tachyon.starfruit.utility.TextFactory.PLAIN;
import static software.tachyon.starfruit.utility.TextFactory.text;

public class ModuleInfo {

    public String name;
    public Category category;
    public Color color;
    private final boolean hidden;
    private final String colorHex;

    public MutableText displayText() {
        // should we cache this?
        return text(this.name, PLAIN, this.color.getRGB() | 0xFF000000);
        // why was this not using the CATALYST_CHAR constant?
        // return String.format("%c%s%s%cr", '\u0666', this.colorHex, this.name, StarfruitMod.COLOR_SEPARATOR);
    }

    public boolean isHidden() {
        return this.hidden;
    }

    ModuleInfo(String name, Category category, Color color, boolean hidden) {
        this.name = name;
        this.category = category;
        this.color = color;
        this.hidden = hidden;
        this.colorHex = Integer.toHexString(this.color.getRGB()).substring(2).toUpperCase();
    }

    public enum Category {
        UTILITY("Utility"), MOVEMENT("Movement"), RENDER("Render");

        public String normalized = null;

        Category(String normalized) {
            this.normalized = normalized;
        }
    }

    public static Builder init() {
        return new Builder();
    }

    public static class Builder {
        // defaults
        private String name = null;
        private Category category = null;
        private Color color = null;
        private boolean hidden = false;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public Builder hidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public ModuleInfo build() {
            if (this.color == null)
                this.color = StarfruitMod.Colors.moduleColor((float) Math.random());
            return new ModuleInfo(this.name, this.category, this.color, this.hidden);
        }
    }

}

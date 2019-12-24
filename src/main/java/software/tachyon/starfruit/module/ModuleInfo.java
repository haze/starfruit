package software.tachyon.starfruit.module;

import java.awt.Color;

import software.tachyon.starfruit.StarfruitMod;

public class ModuleInfo {

    public String name;
    public Category category;
    public Color color;
    private final String colorHex;

    public String hexDisplayString() {
        return String.format("%c%s%s%cr", '\u0666', this.colorHex, this.name, StarfruitMod.COLOR_SEPARATOR);
    }

    ModuleInfo(String name, Category category, Color color) {
        this.name = name;
        this.category = category;
        this.color = color;
        this.colorHex = Integer.toHexString(this.color.getRGB()).substring(2).toUpperCase();
    }

    public enum Category {
        MOVEMENT("Movement"), RENDER("Render");

        public String normalized = null;

        Category(String normalized) {
            this.normalized = normalized;
        }
    }

    public static class Builder {
        private String name = null;
        private Category category = null;
        private Color color = null;

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

        public ModuleInfo build() {
            return new ModuleInfo(this.name, this.category, this.color);
        }
    }

}

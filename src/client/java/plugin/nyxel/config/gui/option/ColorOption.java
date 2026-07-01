package plugin.nyxel.config.gui.option;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.config.gui.NyxelTheme;
import plugin.nyxel.config.gui.widget.ColorPicker;

import java.util.function.Consumer;

/** A row hosting an embedded {@link ColorPicker} (color + chroma). */
public final class ColorOption extends OptionRow {

    private final ColorPicker picker;
    private final Consumer<Integer> colorSetter;
    private final Consumer<Boolean> chromaSetter;

    public ColorOption(String name, String description, int color, boolean chroma,
                       Consumer<Integer> colorSetter, Consumer<Boolean> chromaSetter) {
        super(name, description);
        this.picker = new ColorPicker(color, chroma);
        this.colorSetter = colorSetter;
        this.chromaSetter = chromaSetter;
    }

    @Override
    public int height() {
        return 16 + picker.height();
    }

    @Override
    protected void renderControl(DrawContext ctx, TextRenderer tr, int x, int y, int w,
                                 int mouseX, int mouseY, float delta) {
        picker.render(ctx, tr, x + NyxelTheme.PAD, y + 14, w - NyxelTheme.PAD * 2);
        sync();
    }

    private void sync() {
        colorSetter.accept(picker.getColor());
        chromaSetter.accept(picker.isChroma());
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (picker.mouseClicked(mx, my, button)) {
            sync();
            ConfigManager.save();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button) {
        if (picker.mouseDragged(mx, my)) {
            sync();
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased() {
        picker.mouseReleased();
        ConfigManager.save();
    }
}

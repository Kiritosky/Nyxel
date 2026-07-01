package plugin.nyxel.config.gui.option;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.config.gui.NyxelTheme;
import plugin.nyxel.config.gui.widget.NyxelSlider;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** An int option rendered as a slider in the right half of the row. */
public final class SliderOption extends OptionRow {

    private final IntSupplier getter;
    private final IntConsumer setter;
    private final int min;
    private final int max;
    private boolean dragging;

    public SliderOption(String name, String description, int min, int max,
                        IntSupplier getter, IntConsumer setter) {
        super(name, description);
        this.min = min;
        this.max = max;
        this.getter = getter;
        this.setter = setter;
    }

    private int sliderX() {
        return rx + rw / 2;
    }

    private int sliderW() {
        return rw / 2 - NyxelTheme.PAD - 24;
    }

    private int sliderY() {
        return ry + (height() - NyxelSlider.H) / 2;
    }

    @Override
    protected void renderControl(DrawContext ctx, TextRenderer tr, int x, int y, int w,
                                 int mouseX, int mouseY, float delta) {
        float frac = (getter.getAsInt() - min) / (float) (max - min);
        NyxelSlider.render(ctx, sliderX(), sliderY(), sliderW(), frac, NyxelTheme.ACCENT);
        ctx.drawText(tr, Text.literal("§f" + getter.getAsInt()),
                sliderX() + sliderW() + 6, y + (height() - 8) / 2, NyxelTheme.TEXT, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (within(mx, my, sliderX(), sliderY() - 3, sliderW(), NyxelSlider.H + 6)) {
            dragging = true;
            apply(mx);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button) {
        if (dragging) {
            apply(mx);
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased() {
        if (dragging) {
            dragging = false;
            ConfigManager.save();
        }
    }

    private void apply(double mx) {
        float frac = Math.max(0f, Math.min(1f, (float) (mx - sliderX()) / sliderW()));
        setter.accept(Math.round(min + frac * (max - min)));
    }
}

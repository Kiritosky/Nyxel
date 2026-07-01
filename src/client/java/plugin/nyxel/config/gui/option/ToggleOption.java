package plugin.nyxel.config.gui.option;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.config.gui.NyxelTheme;
import plugin.nyxel.config.gui.widget.ToggleSwitch;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** A boolean option rendered as an animated pill toggle. */
public class ToggleOption extends OptionRow {

    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private float anim;

    public ToggleOption(String name, String description,
                        BooleanSupplier getter, Consumer<Boolean> setter) {
        super(name, description);
        this.getter = getter;
        this.setter = setter;
        this.anim = getter.getAsBoolean() ? 1f : 0f;
    }

    private int switchX() {
        return rx + rw - NyxelTheme.PAD - ToggleSwitch.W;
    }

    private int switchY() {
        return ry + (height() - ToggleSwitch.H) / 2;
    }

    @Override
    protected void renderControl(DrawContext ctx, TextRenderer tr, int x, int y, int w,
                                 int mouseX, int mouseY, float delta) {
        float target = getter.getAsBoolean() ? 1f : 0f;
        anim += (target - anim) * 0.3f;
        ToggleSwitch.render(ctx, switchX(), switchY(), anim);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (within(mx, my, switchX(), switchY(), ToggleSwitch.W, ToggleSwitch.H)
                || within(mx, my, rx, ry, rw, height())) {
            setter.accept(!getter.getAsBoolean());
            ConfigManager.save();
            return true;
        }
        return false;
    }
}

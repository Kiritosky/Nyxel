package plugin.nyxel.feature.garden.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.config.gui.NyxelTheme;

/**
 * Small screen to enter the personal Hypixel API key (from
 * developer.hypixel.net). Stored in {@code config/nyxel.json}.
 */
public final class ApiKeyScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget field;

    public ApiKeyScreen(Screen parent) {
        super(Text.literal("Hypixel API Key"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        field = new TextFieldWidget(textRenderer, width / 2 - 150, height / 2 - 10, 300, 18,
                Text.literal("API Key"));
        field.setMaxLength(80);
        field.setText(ConfigManager.get().api.hypixelKey);
        addSelectableChild(field);
        setInitialFocus(field);

        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> {
            ConfigManager.get().api.hypixelKey = field.getText().trim();
            ConfigManager.save();
            close();
        }).dimensions(width / 2 - 150, height / 2 + 16, 145, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> close())
                .dimensions(width / 2 + 5, height / 2 + 16, 145, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§dEnter your Hypixel API key"), width / 2, height / 2 - 34,
                0xFFFFFFFF);
        ctx.drawText(textRenderer, field.getText().isEmpty()
                        ? Text.literal("§8developer.hypixel.net → Create API Key") : Text.literal(""),
                width / 2 - 150, height / 2 + 40, NyxelTheme.TEXT_MUTED, false);
        field.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}

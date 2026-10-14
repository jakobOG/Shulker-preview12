package dev.shulkerpreview;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;

public class ShulkerPreviewMod implements ClientModInitializer {
    public static final String MOD_ID = "shulkerpreview";

    @Override
    public void onInitializeClient() {
        // A) Inventar: Vorschau beim Hovern (Shift = Detailansicht)
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof HandledScreen<?> handled) {
                PreviewController.reset();
                ScreenEvents.afterRender(screen).register((s, ctx, mouseX, mouseY, tickDelta) ->
                        PreviewController.renderScreen(handled, ctx, mouseX, mouseY));
                ScreenEvents.remove(screen).register(s -> PreviewController.reset());
            }
        });

        // B) Spielwelt: Vorschau beim Anschauen einer platzierten Shulkerbox
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CROSSHAIR,
                Identifier.of(MOD_ID, "world_preview"),
                WorldPreview::render);
    }
}

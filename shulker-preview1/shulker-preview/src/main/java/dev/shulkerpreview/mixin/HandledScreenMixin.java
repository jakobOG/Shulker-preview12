package dev.shulkerpreview.mixin;

import dev.shulkerpreview.PreviewController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    /**
     * Den normalen Tooltip der Shulkerbox ersetzen wir durch die Vorschau (Titel steckt im Panel).
     * Bewusst ohne Parameterliste, damit kleine Signatur-Änderungen zwischen Minecraft-Versionen nicht brechen.
     */
    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void shulkerpreview$replaceTooltip(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        var window = mc.getWindow();
        int mx = (int) (mc.mouse.getX() * window.getScaledWidth() / window.getWidth());
        int my = (int) (mc.mouse.getY() * window.getScaledHeight() / window.getHeight());
        if (PreviewController.suppressVanillaTooltip((HandledScreen<?>) (Object) this, mx, my)) {
            ci.cancel();
        }
    }

    /** Klicks (auch Shift-Klicks) auf das Panel dürfen keine Slots darunter auslösen. */
    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    private void shulkerpreview$blockClicksOnPanel(CallbackInfo ci) {
        if (PreviewController.isMouseOnLockedPanel()) {
            ci.cancel();
        }
    }
}

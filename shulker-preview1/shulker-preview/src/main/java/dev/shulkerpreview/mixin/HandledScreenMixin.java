package dev.shulkerpreview.mixin;

import dev.shulkerpreview.PreviewController;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    /** Den normalen Tooltip der Shulkerbox ersetzen wir durch die Vorschau (Titel steckt im Panel). */
    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void shulkerpreview$replaceTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (PreviewController.suppressVanillaTooltip((HandledScreen<?>) (Object) this, x, y)) {
            ci.cancel();
        }
    }

    /** Klicks (auch Shift-Klicks) auf das Panel dürfen keine Slots darunter auslösen. */
    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    private void shulkerpreview$blockClicksOnPanel(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (PreviewController.isMouseOnLockedPanel()) {
            ci.cancel();
        }
    }
}

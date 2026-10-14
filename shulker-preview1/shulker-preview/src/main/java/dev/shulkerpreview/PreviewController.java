package dev.shulkerpreview;

import dev.shulkerpreview.mixin.HandledScreenAccessor;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * Inventar-Vorschau.
 * Ohne Shift: kompaktes Panel folgt dem Cursor.
 * Mit Shift: Panel wird fixiert; Maus darüber -> normaler Item-Tooltip (Verzauberungen usw.).
 * Shift loslassen: sofort wieder normale Vorschau.
 */
public final class PreviewController {
    private static boolean locked;
    private static Slot lockedSlot;
    private static PanelRenderer.Layout lockedLayout;
    private static boolean mouseOnLockedPanel;

    private PreviewController() {}

    public static void reset() {
        locked = false;
        lockedSlot = null;
        lockedLayout = null;
        mouseOnLockedPanel = false;
    }

    public static boolean isMouseOnLockedPanel() {
        return locked && mouseOnLockedPanel;
    }

    private static Slot hoveredShulkerSlot(HandledScreen<?> screen) {
        if (!screen.getScreenHandler().getCursorStack().isEmpty()) return null;
        Slot slot = ((HandledScreenAccessor) screen).shulkerpreview$getFocusedSlot();
        if (slot != null && ShulkerData.isShulker(slot.getStack())) return slot;
        return null;
    }

    public static boolean suppressVanillaTooltip(HandledScreen<?> screen, int mx, int my) {
        if (locked && lockedLayout != null && lockedLayout.contains(mx, my)) return true;
        return hoveredShulkerSlot(screen) != null;
    }

    public static void renderScreen(HandledScreen<?> screen, DrawContext ctx, int mx, int my) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;
        HandledScreenAccessor acc = (HandledScreenAccessor) screen;
        boolean shift = Screen.hasShiftDown();
        Slot hovered = hoveredShulkerSlot(screen);

        // Sperre prüfen
        if (locked) {
            boolean stillValid = lockedSlot != null && ShulkerData.isShulker(lockedSlot.getStack());
            int sx = acc.shulkerpreview$getX() + (lockedSlot != null ? lockedSlot.x : 0);
            int sy = acc.shulkerpreview$getY() + (lockedSlot != null ? lockedSlot.y : 0);
            boolean overSlot = mx >= sx && mx < sx + 16 && my >= sy && my < sy + 16;
            boolean near = lockedLayout != null && lockedLayout.containsExpanded(mx, my, 18);
            if (!shift || !stillValid || !(overSlot || near)) {
                reset();
            }
        }

        Slot source = locked ? lockedSlot : hovered;
        if (source == null) {
            mouseOnLockedPanel = false;
            return;
        }
        ItemStack shulker = source.getStack();
        List<ItemStack> contents = ShulkerData.fromStack(shulker);
        var title = shulker.getName();

        PanelRenderer.Layout layout;
        if (locked) {
            layout = lockedLayout;
        } else {
            layout = PanelRenderer.atCursor(PanelRenderer.size(tr, title, false), mx, my, screen.width, screen.height);
            if (shift) {
                locked = true;
                lockedSlot = source;
                lockedLayout = layout;
            }
        }
        mouseOnLockedPanel = locked && layout.contains(mx, my);

        // Neue Render-Ebene, damit das Panel über allen Inventar-Items liegt.
        ctx.createNewRootLayer();
        int idx = PanelRenderer.draw(ctx, tr, layout, title, contents, null, mx, my, locked);

        if (locked && idx >= 0) {
            ItemStack hoveredItem = contents.get(idx);
            if (!hoveredItem.isEmpty()) {
                // Originaler Minecraft-Item-Tooltip (Name, Verzauberungen, Haltbarkeit, Attribute ...)
                ctx.drawItemTooltip(tr, hoveredItem, mx, my);
            }
        }
    }
}

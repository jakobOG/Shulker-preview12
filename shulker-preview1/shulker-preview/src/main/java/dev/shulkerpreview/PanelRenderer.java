package dev.shulkerpreview;

import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** Zeichnet das Vorschau-Panel im Vanilla-Inventarstil (grauer Hintergrund, quadratische Slots). */
public final class PanelRenderer {
    public static final int SLOT = 18;
    public static final int COLS = 9;
    public static final int ROWS = 3;
    private static final int PAD = 4;
    private static final int TITLE_H = 13;
    private static final int NOTICE_H = 12;

    // Farben (ARGB), angelehnt an den Screenshot
    private static final int BORDER_OUTER = 0xFF0E0F11;
    private static final int BORDER_INNER = 0xFF4B4E57;
    private static final int BACKGROUND = 0xEE22252A;
    private static final int SLOT_FILL = 0xFF30333A;
    private static final int SLOT_SHADE_DARK = 0xFF16181B;
    private static final int SLOT_SHADE_LIGHT = 0xFF4E525C;
    private static final int HOVER = 0x80FFFFFF;

    private PanelRenderer() {}

    public record Layout(int x, int y, int w, int h) {
        public boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }

        public boolean containsExpanded(double mx, double my, int margin) {
            return mx >= x - margin && mx < x + w + margin && my >= y - margin && my < y + h + margin;
        }

        public int gridX() { return x + PAD; }
        public int gridY() { return y + PAD + TITLE_H; }
    }

    public static Layout size(TextRenderer tr, Text title, boolean notice) {
        int w = Math.max(COLS * SLOT, tr.getWidth(title)) + PAD * 2;
        int h = PAD + TITLE_H + ROWS * SLOT + PAD + (notice ? NOTICE_H : 0);
        return new Layout(0, 0, w, h);
    }

    /** Position wie ein Tooltip (rechts unterhalb vom Cursor), aber im Bildschirm gehalten. */
    public static Layout atCursor(Layout size, int mx, int my, int screenW, int screenH) {
        int x = mx + 12;
        int y = my - 12;
        if (x + size.w() > screenW - 2) x = mx - 12 - size.w();
        if (x < 2) x = 2;
        if (y + size.h() > screenH - 2) y = screenH - 2 - size.h();
        if (y < 2) y = 2;
        return new Layout(x, y, size.w(), size.h());
    }

    /**
     * @return Index (0..26) des Slots unter der Maus oder -1.
     */
    public static int draw(DrawContext ctx, TextRenderer tr, Layout l, Text title, List<ItemStack> stacks,
                           Text notice, int mouseX, int mouseY, boolean interactive) {
        // Rahmen + Hintergrund
        ctx.fill(l.x(), l.y(), l.x() + l.w(), l.y() + l.h(), BORDER_OUTER);
        ctx.fill(l.x() + 1, l.y() + 1, l.x() + l.w() - 1, l.y() + l.h() - 1, BORDER_INNER);
        ctx.fill(l.x() + 2, l.y() + 2, l.x() + l.w() - 2, l.y() + l.h() - 2, BACKGROUND);

        // Titel
        ctx.drawText(tr, title, l.x() + PAD, l.y() + PAD, 0xFFFFFFFF, true);

        int hovered = -1;
        int gx = l.gridX();
        int gy = l.gridY();

        // Slot-Hintergründe
        for (int i = 0; i < ShulkerData.SIZE; i++) {
            int sx = gx + (i % COLS) * SLOT;
            int sy = gy + (i / COLS) * SLOT;
            ctx.fill(sx, sy, sx + SLOT, sy + SLOT, SLOT_SHADE_DARK);
            ctx.fill(sx + 1, sy + 1, sx + SLOT, sy + SLOT, SLOT_SHADE_LIGHT);
            ctx.fill(sx + 1, sy + 1, sx + SLOT - 1, sy + SLOT - 1, SLOT_FILL);
            if (interactive && mouseX >= sx && mouseX < sx + SLOT && mouseY >= sy && mouseY < sy + SLOT) {
                hovered = i;
            }
        }

        // Items (Original-Renderer: Textur, Glitzern, Anzahl, Haltbarkeitsbalken)
        for (int i = 0; i < ShulkerData.SIZE && i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) continue;
            int ix = gx + (i % COLS) * SLOT + 1;
            int iy = gy + (i / COLS) * SLOT + 1;
            ctx.drawItem(stack, ix, iy);
            ctx.drawStackOverlay(tr, stack, ix, iy);
        }

        if (hovered >= 0) {
            int sx = gx + (hovered % COLS) * SLOT + 1;
            int sy = gy + (hovered / COLS) * SLOT + 1;
            ctx.fill(sx, sy, sx + 16, sy + 16, HOVER);
        }

        if (notice != null) {
            ctx.drawText(tr, notice, l.x() + PAD, gy + ROWS * SLOT + 3, 0xFFFFAA55, true);
        }
        return hovered;
    }
}

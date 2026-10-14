package dev.shulkerpreview;

import dev.shulkerpreview.mixin.LootableContainerBlockEntityAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Vorschau für platzierte Shulkerboxen (Fadenkreuz).
 * Datenquellen (nur reguläre Daten, nichts wird erraten):
 *  1) Einzelspieler/LAN-Host: Inhalt der integrierten Server-Welt (asynchron auf dem Server-Thread gelesen).
 *  2) Sonst: Block-Entity des Clients. Vanilla-Server senden den Inhalt meist nicht -> Hinweis statt falscher Items.
 */
public final class WorldPreview {
    private record Snapshot(BlockPos pos, List<ItemStack> stacks, Text title) {}

    private static volatile Snapshot serverSnapshot;
    private static long lastRequestNanos;
    private static BlockPos lastRequestPos;

    private WorldPreview() {}

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.currentScreen != null || mc.options.hudHidden) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        if (!(state.getBlock() instanceof ShulkerBoxBlock)) return;

        List<ItemStack> stacks = null;
        Text title = state.getBlock().getName();

        // 1) Integrierter Server (Einzelspieler)
        MinecraftServer server = mc.getServer();
        if (server != null) {
            requestServerSnapshot(mc, server, pos);
            Snapshot snap = serverSnapshot;
            if (snap != null && snap.pos().equals(pos)) {
                stacks = snap.stacks();
                title = snap.title();
            }
        }

        // 2) Client-Block-Entity (Multiplayer, falls der Server Daten sendet)
        if (stacks == null || ShulkerData.allEmpty(stacks)) {
            BlockEntity be = mc.world.getBlockEntity(pos);
            if (be instanceof ShulkerBoxBlockEntity sbe) {
                List<ItemStack> fromClient = readHeld(sbe);
                title = sbe.getDisplayName();
                if (stacks == null || !ShulkerData.allEmpty(fromClient)) {
                    stacks = fromClient;
                }
            }
        }
        if (stacks == null) {
            stacks = emptyList();
        }

        boolean unavailable = ShulkerData.allEmpty(stacks);
        Text notice = unavailable ? Text.translatable("shulkerpreview.world.unavailable") : null;

        var tr = mc.textRenderer;
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        PanelRenderer.Layout size = PanelRenderer.size(tr, title, unavailable);
        int x = (sw - size.w()) / 2;
        int y = sh / 2 + 18;
        if (y + size.h() > sh - 2) y = sh - 2 - size.h();
        if (x < 2) x = 2;
        PanelRenderer.Layout layout = new PanelRenderer.Layout(x, y, size.w(), size.h());

        PanelRenderer.draw(ctx, tr, layout, title, stacks, notice, -1, -1, false);
    }

    private static void requestServerSnapshot(MinecraftClient mc, MinecraftServer server, BlockPos pos) {
        long now = System.nanoTime();
        boolean newPos = !pos.equals(lastRequestPos);
        if (!newPos && now - lastRequestNanos < 150_000_000L) return; // max. ~6 Abfragen pro Sekunde
        lastRequestNanos = now;
        lastRequestPos = pos.toImmutable();
        final BlockPos target = pos.toImmutable();
        final var worldKey = mc.world.getRegistryKey();
        server.execute(() -> {
            ServerWorld sw = server.getWorld(worldKey);
            if (sw == null) return;
            if (sw.getBlockEntity(target) instanceof ShulkerBoxBlockEntity sbe) {
                serverSnapshot = new Snapshot(target, readHeld(sbe), sbe.getDisplayName());
            }
        });
    }

    /** Kopiert die Items, ohne Loot-Tabellen zu generieren. */
    private static List<ItemStack> readHeld(ShulkerBoxBlockEntity be) {
        List<ItemStack> out = emptyList();
        var held = ((LootableContainerBlockEntityAccessor) be).shulkerpreview$getHeldStacks();
        for (int i = 0; i < ShulkerData.SIZE && i < held.size(); i++) {
            out.set(i, held.get(i).copy());
        }
        return out;
    }

    private static List<ItemStack> emptyList() {
        List<ItemStack> l = new ArrayList<>(ShulkerData.SIZE);
        for (int i = 0; i < ShulkerData.SIZE; i++) l.add(ItemStack.EMPTY);
        return l;
    }
}

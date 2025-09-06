package com.arkofreis.dictatorscript;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Container;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.inventory.IInventory;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class AutoScript {
    private static boolean waitingForEnderChest = false;
    private static boolean waitingForShulker = false;
    private static boolean isStealing = false;
    private static List<String> pendingItems;
    private static BlockPos shulkerPos = null;
    private static int stealSlot = 0;
    private static int stealDelay = 0;
    private static Container currentContainer = null;
    private static EnderChestHandler enderChestHandler = null;
    private static TickHandler tickHandler = null;

    public static void runFullRoutine(List<String> items) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        cleanupHandlers();

        pendingItems = items;
        waitingForEnderChest = true;
        waitingForShulker = false;
        isStealing = false;

        enderChestHandler = new EnderChestHandler();
        MinecraftForge.EVENT_BUS.register(enderChestHandler);

        if (mc.currentScreen instanceof GuiChest) {
            GuiChest guiChest = (GuiChest) mc.currentScreen;
            ContainerChest container = (ContainerChest) guiChest.inventorySlots;
            IInventory lowerChest = container.getLowerChestInventory();

            String chestName = lowerChest.getName().toLowerCase();
            if ((chestName.contains("ender") || chestName.contains("chest")) &&
                    lowerChest.getSizeInventory() == 27) {

                waitingForEnderChest = false;
                MinecraftForge.EVENT_BUS.unregister(enderChestHandler);
                enderChestHandler = null;

                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        takeKitAndPlaceShulker();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }

        new Thread(() -> {
            try {
                AutoScript.sendChat("#goto ender_chest");
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void cleanupHandlers() {
        if (enderChestHandler != null) {
            MinecraftForge.EVENT_BUS.unregister(enderChestHandler);
            enderChestHandler = null;
        }
        if (tickHandler != null) {
            MinecraftForge.EVENT_BUS.unregister(tickHandler);
            tickHandler = null;
        }
    }

    private static class EnderChestHandler {
        @SubscribeEvent
        public void onGuiOpen(GuiOpenEvent event) {
            if (!waitingForEnderChest) return;

            if (event.getGui() instanceof GuiChest) {
                GuiChest guiChest = (GuiChest) event.getGui();
                ContainerChest container = (ContainerChest) guiChest.inventorySlots;
                IInventory lowerChest = container.getLowerChestInventory();

                String chestName = lowerChest.getName().toLowerCase();
                if ((chestName.contains("ender") || chestName.contains("chest")) &&
                        lowerChest.getSizeInventory() == 27) {

                    waitingForEnderChest = false;
                    MinecraftForge.EVENT_BUS.unregister(this);

                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            takeKitAndPlaceShulker();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        }
    }

    private static class TickHandler {
        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            handleShulkerStealing();
        }

        private void handleShulkerStealing() {
            if (!isStealing || currentContainer == null) return;

            Minecraft mc = Minecraft.getMinecraft();

            if (mc.currentScreen == null || mc.player.openContainer != currentContainer) {
                finishStealing();
                return;
            }

            int processed = 0;
            while (stealSlot < 27 && processed < 27) {
                Slot slot = currentContainer.getSlot(stealSlot);
                if (slot.getHasStack()) {
                    mc.playerController.windowClick(
                            currentContainer.windowId,
                            stealSlot,
                            0,
                            ClickType.QUICK_MOVE,
                            mc.player
                    );
                }
                stealSlot++;
                processed++;
            }

            if (stealSlot >= 27) {
                finishStealing();
            }
        }

        private void finishStealing() {
            isStealing = false;
            Minecraft mc = Minecraft.getMinecraft();

            if (mc.player != null) {
                mc.player.closeScreen();
                mc.player.sendMessage(new TextComponentString(
                        "\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aFinished stealing from shulker."));

                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        breakShulkerAndMineItems();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    private static void takeKitAndPlaceShulker() throws InterruptedException {
        Minecraft mc = Minecraft.getMinecraft();
        int waited = 0;
        while (!(mc.currentScreen instanceof GuiChest) && waited < 5000) {
            Thread.sleep(100);
            waited += 100;
        }

        if (!(mc.currentScreen instanceof GuiChest)) {
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cEnder chest GUI not open."));
            return;
        }

        GuiChest guiChest = (GuiChest) mc.currentScreen;
        ContainerChest container = (ContainerChest) guiChest.inventorySlots;

        int blackShulkerSlot = -1;
        List<Integer> blackSlots = new ArrayList<>();
        for (int slot = 0; slot < 27; slot++) {
            ItemStack stack = container.getSlot(slot).getStack();
            if (!stack.isEmpty()) {
                String itemName = stack.getItem().getRegistryName().toString();
                if (itemName.equals("minecraft:black_shulker_box")) {
                    blackSlots.add(slot);
                }
            }
        }
        if (!blackSlots.isEmpty()) {
            blackShulkerSlot = blackSlots.get(ThreadLocalRandom.current().nextInt(blackSlots.size()));
        }

        if (blackShulkerSlot != -1) {
            ItemStack chestStack = container.getSlot(blackShulkerSlot).getStack();
            int count = chestStack.isEmpty() ? 0 : chestStack.getCount();

            if (count <= 1) {
                mc.playerController.windowClick(container.windowId, blackShulkerSlot, 0, ClickType.QUICK_MOVE, mc.player);
            } else {
                if (!mc.player.inventory.getItemStack().isEmpty()) {
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cCannot take shulker: cursor not empty."));
                    mc.player.closeScreen();
                    return;
                }

                mc.playerController.windowClick(container.windowId, blackShulkerSlot, 0, ClickType.PICKUP, mc.player);

                int totalSlots = container.inventorySlots.size();
                int hotbarStart = totalSlots - 9;
                int targetPlayerSlot = -1;

                for (int idx = hotbarStart; idx < totalSlots; idx++) {
                    if (!container.getSlot(idx).getHasStack()) {
                        targetPlayerSlot = idx;
                        break;
                    }
                }
                if (targetPlayerSlot == -1) {
                    for (int idx = 27; idx < hotbarStart; idx++) {
                        if (!container.getSlot(idx).getHasStack()) {
                            targetPlayerSlot = idx;
                            break;
                        }
                    }
                }

                if (targetPlayerSlot == -1) {
                    mc.playerController.windowClick(container.windowId, blackShulkerSlot, 0, ClickType.PICKUP, mc.player);
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cNo empty slot to take a shulker."));
                    mc.player.closeScreen();
                    return;
                }
                mc.playerController.windowClick(container.windowId, targetPlayerSlot, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(container.windowId, blackShulkerSlot, 0, ClickType.PICKUP, mc.player);
            }

            Thread.sleep(500);
            mc.player.closeScreen();
            Thread.sleep(1000);

            boolean foundInHotbar = false;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem().getRegistryName().toString().equals("minecraft:black_shulker_box")) {
                    mc.player.inventory.currentItem = i;
                    foundInHotbar = true;
                    break;
                }
            }

            if (!foundInHotbar) {
                for (int i = 9; i < 36; i++) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem().getRegistryName().toString().equals("minecraft:black_shulker_box")) {
                        int hotbarSlot = 0;
                        mc.playerController.windowClick(0, i, hotbarSlot, ClickType.SWAP, mc.player);
                        mc.player.inventory.currentItem = hotbarSlot;
                        foundInHotbar = true;
                        Thread.sleep(500);
                        break;
                    }
                }
            }

            if (!foundInHotbar) {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cBlack shulker box not found."));
                return;
            }

            BlockPos placementPos = findPlacementPosition();
            if (placementPos != null) {
                shulkerPos = placementPos;
                lookAtBlock(placementPos);
                Thread.sleep(500);

                ItemStack heldItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
                if (heldItem.isEmpty() || !heldItem.getItem().getRegistryName().toString().equals("minecraft:black_shulker_box")) {
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cNot holding black shulker box."));
                    return;
                }

                placeShulkerBox(placementPos);
                Thread.sleep(1500);

                lookAtBlock(placementPos);
                Thread.sleep(500);

                RayTraceResult rayTrace = mc.player.rayTrace(5.0, 1.0F);
                if (rayTrace != null && rayTrace.getBlockPos().equals(placementPos)) {
                    mc.playerController.processRightClickBlock(mc.player, mc.world, placementPos,
                            rayTrace.sideHit, rayTrace.hitVec, EnumHand.MAIN_HAND);
                } else {
                    mc.playerController.processRightClickBlock(mc.player, mc.world, placementPos,
                            EnumFacing.UP, new Vec3d(0.5, 1.0, 0.5), EnumHand.MAIN_HAND);
                }

                new Thread(() -> {
                    try {
                        Thread.sleep(1000);

                        mc.addScheduledTask(() -> {
                            if (mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiContainer) {
                                currentContainer = mc.player.openContainer;

                                int totalSlots = currentContainer.inventorySlots.size();
                                if (totalSlots == 63) {
                                    isStealing = true;
                                    stealSlot = 0;
                                    stealDelay = 0;

                                    if (tickHandler == null) {
                                        tickHandler = new TickHandler();
                                        MinecraftForge.EVENT_BUS.register(tickHandler);
                                    }

                                    mc.player.sendMessage(new TextComponentString(
                                            "\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aStarting to steal from shulker."));
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

            } else {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cCould not find placement position."));
            }
        } else {
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cNo black shulker box found in ender chest."));
            mc.player.closeScreen();
        }
    }

    private static void breakShulkerAndMineItems() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            if (shulkerPos != null && mc.player != null) {
                lookAtBlock(shulkerPos);
                mc.playerController.onPlayerDamageBlock(shulkerPos, EnumFacing.UP);

                new Thread(() -> {
                    try {
                        Thread.sleep(2000);

                        if (pendingItems != null && !pendingItems.isEmpty()) {
                            String mineCommand = "#mine " + String.join(" ", pendingItems);
                            mc.addScheduledTask(() -> {
                                if (mc.player != null) {
                                    mc.player.sendMessage(new TextComponentString(
                                            "\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aExecuting: " + mineCommand));
                                    sendChat(mineCommand);
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    private static void lookAtBlock(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;

        double dx = centerX - mc.player.posX;
        double dy = centerY - (mc.player.posY + mc.player.getEyeHeight());
        double dz = centerZ - mc.player.posZ;

        double distance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distance));

        mc.player.rotationYaw = yaw;
        mc.player.rotationPitch = pitch;
        mc.player.rotationYawHead = yaw;
    }

    private static BlockPos findPlacementPosition() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return null;

        if (mc.player.posY > 255.0) {
            return null;
        }

        BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        double radians = Math.toRadians(mc.player.rotationYaw);
        int frontX = (int) Math.round(-Math.sin(radians) * 2);
        int frontZ = (int) Math.round(Math.cos(radians) * 2);

        BlockPos frontPos = playerPos.add(frontX, 0, frontZ);
        BlockPos frontSupport = frontPos.down();
        if (mc.world.isAirBlock(frontPos)
                && !mc.world.isAirBlock(frontSupport)
                && mc.world.getBlockState(frontSupport).isFullBlock()) {
            return frontPos;
        }

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0) continue;
                BlockPos checkPos = playerPos.add(x, 0, z);
                BlockPos support = checkPos.down();
                if (mc.world.isAirBlock(checkPos)
                        && !mc.world.isAirBlock(support)
                        && mc.world.getBlockState(support).isFullBlock()) {
                    return checkPos;
                }
            }
        }
        return null;
    }

    private static void placeShulkerBox(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        if (pos.getY() > 255) {
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cCannot place blocks above Y=255."));
            return;
        }

        BlockPos support = pos.down();
        if (mc.world.isAirBlock(support) || !mc.world.getBlockState(support).isFullBlock()) {
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cNo solid block beneath to place the shulker on."));
            return;
        }

        EnumFacing facing = EnumFacing.UP;
        Vec3d hitVec = new Vec3d(
                support.getX() + 0.5,
                support.getY() + 1.0,
                support.getZ() + 0.5
        );
        mc.playerController.processRightClickBlock(mc.player, mc.world, support, facing, hitVec, EnumHand.MAIN_HAND);
    }

    public static void sendChat(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            mc.addScheduledTask(() -> {
                if (mc.player != null) {
                    mc.player.sendChatMessage(message);
                }
            });
        }
    }
}
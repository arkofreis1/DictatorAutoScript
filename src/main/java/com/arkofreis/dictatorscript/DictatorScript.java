package com.arkofreis.dictatorscript;

import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Mod(modid = DictatorScript.MODID, name = DictatorScript.NAME, version = DictatorScript.VERSION, clientSideOnly = true)
public class DictatorScript {
    public static final String MODID = "dictatorscript";
    public static final String NAME = "Dictator Script";
    public static final String VERSION = "1.0";
    public static boolean enabled = false;
    private static List<String> setItems = new ArrayList<>();
    private static boolean hasShownJoinMessage = false;
    private static boolean deathPending = false;
    private static boolean baritoneSettingsApplied = false;

    public DictatorScript() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        String msg = event.getMessage().trim();

        Minecraft mc = Minecraft.getMinecraft();

        if (!enabled && msg.startsWith(",")) {
            String lower = msg.toLowerCase();
            boolean isKnown = lower.equals(",enable")
                    || lower.equals(",disable")
                    || lower.equals(",start")
                    || lower.equals(",stop")
                    || lower.equals(",help")
                    || lower.equals(",set")
                    || lower.startsWith(",set ");
            if (isKnown && !lower.equals(",help") && !lower.equals(",enable")) {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cMod is currently disabled (Use ,enable)."));
                event.setCanceled(true);
                return;
            }
        }

        if (msg.equalsIgnoreCase(",set")) {
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cIncorrect Usage! Please use ,set {block(s)}."));
            event.setCanceled(true);
            return;
        }

        switch (msg.toLowerCase()) {
            case ",enable":
                enabled = true;
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aEnabled."));
                applyBaritoneDefaultsIfNeeded();
                event.setCanceled(true);
                break;

            case ",disable":
                enabled = false;
                AutoScript.sendChat("#stop");
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cDisabled."));
                event.setCanceled(true);
                break;

            case ",start":
                if (!setItems.isEmpty()) {
                    new Thread(() -> {
                        try {
                            String mineCommand = "#mine " + String.join(" ", setItems);
                            AutoScript.sendChat(mineCommand);
                            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aStarting Baritone."));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cNo items specified. Set blocks first."));
                }
                event.setCanceled(true);
                break;

            case ",stop":
                AutoScript.sendChat("#stop");
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cStopping Baritone."));
                event.setCanceled(true);
                break;

            case ",help":
                mc.player.sendMessage(new TextComponentString("\u00A77--------------------------------------------"));
                mc.player.sendMessage(new TextComponentString("\u00A7cDictator Auto-Script \u00A7f| \u00A77v1.0 \u00A7f| \u00A77by ArkOfReis"));
                mc.player.sendMessage(new TextComponentString("\u00A77--------------------------------------------"));
                mc.player.sendMessage(new TextComponentString("\u00A7a,enable \u00A7f| \u00A77Enables Dictator Auto-Script"));
                mc.player.sendMessage(new TextComponentString("\u00A7a,disable \u00A7f| \u00A77Disables Dictator Auto-Script"));
                mc.player.sendMessage(new TextComponentString("\u00A7a,set {block(s)} \u00A7f| \u00A77Sets the block(s) you want to mine"));
                mc.player.sendMessage(new TextComponentString("\u00A7a,start \u00A7f| \u00A77Start Baritone (selected blocks)"));
                mc.player.sendMessage(new TextComponentString("\u00A7a,stop \u00A7f| \u00A77Stop Baritone"));
                mc.player.sendMessage(new TextComponentString("\u00A77--------------------------------------------"));
                event.setCanceled(true);
                break;

            default:
                if (msg.toLowerCase().startsWith(",set ")) {
                    String argsPart = msg.substring(5).trim();
                    if (!argsPart.isEmpty()) {
                        setItems = new ArrayList<>(Arrays.asList(argsPart.split("\\s+")));

                        List<String> displayItems = new ArrayList<>();
                        for (String item : setItems) {
                            displayItems.add("minecraft:" + item);
                        }
                        mc.player.sendMessage(
                                new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aSet: " + String.join(", ", displayItems))
                        );
                    }

                    else {
                        mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cIncorrect Usage! Please use ,set {block(s)}."));
                    }
                    event.setCanceled(true);
                }
                break;
        }

        if (!event.isCanceled() && msg.startsWith(",")) {
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cIncorrect Usage. Please use ,help!"));
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!enabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        if (event.getEntity() instanceof EntityPlayer && event.getEntity() == mc.player) {
            deathPending = true;
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!enabled) return;
        if (event.getGui() instanceof GuiGameOver) {
            deathPending = true;
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        hasShownJoinMessage = false;
        enabled = false;
        baritoneSettingsApplied = false;
    }

    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        enabled = false;

        new Thread(() -> {
            try {
                Minecraft mc = Minecraft.getMinecraft();
                int attempts = 0;
                while ((mc.player == null || mc.world == null) && attempts < 200) {
                    Thread.sleep(50);
                    attempts++;
                }
                if (mc.player != null && mc.world != null) {
                    Thread.sleep(500);
                    mc.addScheduledTask(() -> {
                        if (!hasShownJoinMessage && mc.player != null) {
                            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7fSuccessfully initialised. Mod is \u00A7cDisabled."));
                            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aRemember to set a block (,set) before starting!"));
                            hasShownJoinMessage = true;
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();

        if (!hasShownJoinMessage && mc.player != null && mc.world != null) {
            mc.addScheduledTask(() -> {
                if (!hasShownJoinMessage && mc.player != null) {
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7fSuccessfully initialised. Mod is \u00A7cDisabled."));
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aRemember to set a block (,set) before starting!"));
                    hasShownJoinMessage = true;
                }
            });
        }

        if (deathPending && mc.player != null && mc.world != null
                && mc.currentScreen == null && !mc.player.isDead && mc.player.getHealth() > 0.0F) {

            deathPending = false;

            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    if (enabled && !setItems.isEmpty()) {
                        int spawnY = (int) Math.floor(mc.player.posY);
                        boolean reachedTarget = true;
                        if (spawnY == 256) {
                            mc.addScheduledTask(() -> {
                                if (mc.player != null) {
                                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7eDetected Y = 256. Moving to Y=250."));
                                }
                            });
                            AutoScript.sendChat("#goto ~ 250 ~");

                            int checks = 0;
                            reachedTarget = false;
                            while (checks < 300) {
                                Thread.sleep(200);
                                if (mc.player == null) break;
                                if (mc.player.posY <= 250.5) {
                                    reachedTarget = true;
                                    break;
                                }
                                checks++;
                            }

                            AutoScript.sendChat("#stop");
                            Thread.sleep(300);

                            if (!reachedTarget) {
                                mc.addScheduledTask(() -> {
                                    if (mc.player != null) {
                                        mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cCould not reach Y=250. Suiciding."));
                                    }
                                });
                                AutoScript.sendChat("/kill");
                                return;
                            }
                        }

                        mc.addScheduledTask(() -> {
                            if (mc.player != null) {
                                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aRunning."));
                            }
                        });
                        AutoScript.runFullRoutine(setItems);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @SubscribeEvent
    public void onClientDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        hasShownJoinMessage = false;
        enabled = false;
        baritoneSettingsApplied = false;
    }

    private static void applyBaritoneDefaultsIfNeeded() {
        if (baritoneSettingsApplied) return;

        baritoneSettingsApplied = true;

        new Thread(() -> {
            try {
                AutoScript.sendChat("#autoTool true");
                Thread.sleep(100);
                AutoScript.sendChat("#allowSprint true");
                Thread.sleep(100);
                AutoScript.sendChat("#allowDiagonalAscend true");
                Thread.sleep(100);
                AutoScript.sendChat("#allowDiagonalDescend true");
                Thread.sleep(100);
                AutoScript.sendChat("#allowDownward true");
                Thread.sleep(100);
                AutoScript.sendChat("#allowInventory true");
                Thread.sleep(100);
                AutoScript.sendChat("#allowParkour true");
                Thread.sleep(100);
                AutoScript.sendChat("#allowParkourAscend true");
                Thread.sleep(100);
                AutoScript.sendChat("#allowOvershootDiagonalDescend true");
                Thread.sleep(100);
                AutoScript.sendChat("#sprintAscends true");
                Thread.sleep(100);
                AutoScript.sendChat("#walkWhileBreaking true");
                Thread.sleep(100);
                AutoScript.sendChat("#simplifyUnloadedYCoord true");
                Thread.sleep(100);
                AutoScript.sendChat("#blacklistClosestOnFailure true");
                Thread.sleep(100);
                AutoScript.sendChat("#antiCheatCompatibility true");
                Thread.sleep(100);
                AutoScript.sendChat("#maxFallHeightNoWater 12");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static List<String> getSetItems() {
        return setItems;
    }
}
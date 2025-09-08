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
    public static final String VERSION = "1.1";
    public static boolean enabled = false;
    private static List<String> setItems = new ArrayList<>();
    private static boolean hasShownJoinMessage = false;
    private static boolean deathPending = false;
    private static boolean baritoneSettingsApplied = false;
    private static final int WORLD_BORDER_HALF = 1000;
    private static long lastBoundaryWarnMs = 0L;

    public DictatorScript() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        String msg = event.getMessage().trim();

        Minecraft mc = Minecraft.getMinecraft();
        String prefix = ConfigManager.getCommandPrefix();

        if (!enabled && msg.startsWith(prefix)) {
            String lower = msg.toLowerCase();
            boolean isKnown = lower.equals((prefix + "enable").toLowerCase())
                    || lower.equals((prefix + "disable").toLowerCase())
                    || lower.equals((prefix + "start").toLowerCase())
                    || lower.equals((prefix + "stop").toLowerCase())
                    || lower.equals((prefix + "help").toLowerCase())
                    || lower.equals((prefix + "set").toLowerCase())
                    || lower.startsWith((prefix + "set ").toLowerCase())
                    || lower.equals((prefix + "autowhisper").toLowerCase())
                    || lower.equals((prefix + "aw").toLowerCase())
                    || lower.equals((prefix + "config").toLowerCase())
                    || lower.equals((prefix + "conf").toLowerCase())
                    || lower.startsWith((prefix + "config ").toLowerCase())
                    || lower.startsWith((prefix + "conf ").toLowerCase())
                    || lower.startsWith((prefix + "modprefix").toLowerCase())
                    || lower.startsWith((prefix + "mp").toLowerCase())
                    || lower.startsWith((prefix + "gotoground").toLowerCase())
                    || lower.startsWith((prefix + "gtg").toLowerCase())
                    || lower.startsWith((prefix + "shulkercolor").toLowerCase())
                    || lower.startsWith((prefix + "sc").toLowerCase())
                    || lower.startsWith((prefix + "ignorestackedshulkers").toLowerCase())
                    || lower.startsWith((prefix + "iss").toLowerCase())
                    || lower.equals((prefix + "auto").toLowerCase())
                    || lower.equals((prefix + "cls").toLowerCase());
                boolean allowedWhileDisabled =
                        lower.equals((prefix + "help").toLowerCase())
                                || lower.equals((prefix + "enable").toLowerCase())
                                || lower.equals((prefix + "config").toLowerCase())
                                || lower.equals((prefix + "conf").toLowerCase())
                                || lower.startsWith((prefix + "modprefix").toLowerCase())
                                || lower.startsWith((prefix + "mp").toLowerCase())
                                || lower.startsWith((prefix + "gotoground").toLowerCase())
                                || lower.startsWith((prefix + "gtg").toLowerCase())
                                || lower.startsWith((prefix + "shulkercolor").toLowerCase())
                                || lower.startsWith((prefix + "sc").toLowerCase())
                                || lower.startsWith((prefix + "ignorestackedshulkers").toLowerCase())
                                || lower.startsWith((prefix + "iss").toLowerCase())
                            || lower.startsWith((prefix + "autowhisper").toLowerCase())
                            || lower.startsWith((prefix + "aw").toLowerCase())
                            || lower.equals((prefix + "auto").toLowerCase())
                            || lower.startsWith((prefix + "set").toLowerCase())
                            || lower.equals((prefix + "cls").toLowerCase());
            if (isKnown && !allowedWhileDisabled) {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cMod is currently disabled (Use " + prefix + "enable)."));
                event.setCanceled(true);
                return;
            }
        }

        if (msg.equalsIgnoreCase(prefix + "set")) {
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cIncorrect Usage! Please use " + prefix + "set {block(s)}."));
            event.setCanceled(true);
            return;
        }

        String lmsg = msg.toLowerCase();
        if (lmsg.equals((prefix + "enable").toLowerCase())) {
            enabled = true;
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aEnabled."));
            applyBaritoneDefaultsIfNeeded();
            event.setCanceled(true);

        } else if (lmsg.equals((prefix + "disable").toLowerCase())) {
            enabled = false;
            AutoScript.sendChat("#stop");
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cDisabled."));
            event.setCanceled(true);

        } else if (lmsg.equals((prefix + "start").toLowerCase())) {
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

        } else if (lmsg.equals((prefix + "stop").toLowerCase())) {
            AutoScript.sendChat("#stop");
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cStopping Baritone."));
            event.setCanceled(true);

        } else if (lmsg.equals((prefix + "auto").toLowerCase())) {
            new Thread(() -> {
                try {
                    String blocks = ConfigManager.getSetBlock();
                    if (blocks == null || blocks.trim().isEmpty()) {
                        mc.addScheduledTask(() -> {
                            if (mc.player != null) {
                                String dynPrefix = ConfigManager.getCommandPrefix();
                                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cNo setBlock configured. Use " + dynPrefix + "set {block(s)} first."));
                            }
                        });
                        return;
                    }

                    mc.addScheduledTask(() -> {
                        if (mc.player != null) {
                            enabled = true;
                            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aEnabled."));
                            applyBaritoneDefaultsIfNeeded();
                        }
                    });
                    Thread.sleep(2000);

                    mc.addScheduledTask(() -> {
                        if (mc.player != null) {
                            setItems = new ArrayList<>(Arrays.asList(blocks.split("\\s+")));
                            ConfigManager.setSetBlock(blocks);
                            java.util.List<String> displayItems = new ArrayList<>();
                            for (String item : setItems) {
                                displayItems.add("minecraft:" + item);
                            }
                            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aSet: " + String.join(", ", displayItems)));
                        }
                    });
                    Thread.sleep(2000);

                    AutoScript.sendChat("/kill");
                } catch (InterruptedException ignored) {}
            }).start();
            event.setCanceled(true);

        } else if (lmsg.equals((prefix + "cls").toLowerCase())) {
            mc.addScheduledTask(() -> {
                if (mc.ingameGUI != null && mc.ingameGUI.getChatGUI() != null) {
                    mc.ingameGUI.getChatGUI().clearChatMessages(true);
                }
            });
            event.setCanceled(true);

        } else if (lmsg.equals((prefix + "help").toLowerCase())) {
            mc.player.sendMessage(new TextComponentString("\u00A77------------------------------------------------"));
            mc.player.sendMessage(new TextComponentString("\u00A7cDictator Auto-Script \u00A7f| \u00A77v1.1 \u00A7f| \u00A76by ArkOfReis"));
            mc.player.sendMessage(new TextComponentString("\u00A77------------------------------------------------"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "enable \u00A7f| \u00A77Enables Dictator Auto-Script"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "disable \u00A7f| \u00A77Disables Dictator Auto-Script"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "set {block(s)} \u00A7f| \u00A77Sets the block(s) you want to mine"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "start \u00A7f| \u00A77Start Baritone (selected blocks)"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "stop \u00A7f| \u00A77Stop Baritone"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "auto \u00A7f| \u00A77Auto starts script process (enable, set, kill)"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "cls \u00A7f| \u00A77Clears chat, client-side only"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "config \u00A7f| \u00A77Show the configuration menu"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "autoWhisper \u00A7f| \u00A77Sends the configured message"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "autoWhisper set {string} \u00A7f| \u00A77Sets the configured message"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "modPrefix {prefix} \u00A7f| \u00A77Set prefix for mod"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "shulkerColor {color} \u00A7f| \u00A77Set the color of Shulker that the mod looks for"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "goToGround {bool} \u00A7f| \u00A77Choose whether to go to Y=5 before mining"));
            mc.player.sendMessage(new TextComponentString("\u00A7a" + prefix + "ignoreStackedShulkers {bool} \u00A7f| \u00A77Choose whether to ignore stacked shulkers (true by default)"));
            mc.player.sendMessage(new TextComponentString("\u00A77------------------------------------------------"));
            event.setCanceled(true);

        } else if (lmsg.equals((prefix + "config").toLowerCase()) || lmsg.equals((prefix + "conf").toLowerCase())) {
            mc.player.sendMessage(new TextComponentString("\u00A77------------------------------------------------"));
            mc.player.sendMessage(new TextComponentString("\u00A7cDictator Auto-Script \u00A7f| \u00A77v1.1 \u00A7f| \u00A76Mod Configuration"));
            mc.player.sendMessage(new TextComponentString("\u00A77------------------------------------------------"));
            mc.player.sendMessage(new TextComponentString("\u00A7fsetBlock: \u00A7a" + (ConfigManager.getSetBlock().isEmpty() ? "{block(s)}" : ConfigManager.getSetBlock())));
            mc.player.sendMessage(new TextComponentString("\u00A7fautoWhisper: \u00A7a" + (ConfigManager.getAutoWhisper().isEmpty() ? "{string}" : ConfigManager.getAutoWhisper())));
            mc.player.sendMessage(new TextComponentString("\u00A7fmodPrefix: \u00A7a" + ConfigManager.getCommandPrefix()));
            mc.player.sendMessage(new TextComponentString("\u00A7fshulkerColor: \u00A7a" + ConfigManager.getShulkerColorDisplay()));
            boolean gtg = ConfigManager.getGoToGround();
            mc.player.sendMessage(new TextComponentString("\u00A7fgoToGround: " + (gtg ? "\u00A7a" : "\u00A7c") + (gtg ? "true" : "false")));
            boolean ign = ConfigManager.getIgnoreStackedShulkers();
            mc.player.sendMessage(new TextComponentString("\u00A7fignoreStackedShulkers: " + (ign ? "\u00A7a" : "\u00A7c") + (ign ? "true" : "false")));
            mc.player.sendMessage(new TextComponentString("\u00A77------------------------------------------------"));
            event.setCanceled(true);

        } else if (lmsg.startsWith((prefix + "config ").toLowerCase()) || lmsg.startsWith((prefix + "conf ").toLowerCase())) {
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7eUse base commands to change values:"));
            mc.player.sendMessage(new TextComponentString("\u00A7f- " + prefix + "modPrefix {prefix}"));
            mc.player.sendMessage(new TextComponentString("\u00A7f- " + prefix + "shulkerColor {color}"));
            mc.player.sendMessage(new TextComponentString("\u00A7f- " + prefix + "goToGround {bool}"));
            mc.player.sendMessage(new TextComponentString("\u00A7f- " + prefix + "ignoreStackedShulkers {bool}"));
            mc.player.sendMessage(new TextComponentString("\u00A7f- " + prefix + "autoWhisper \u00A77(to send your message)"));
            event.setCanceled(true);

        } else if (lmsg.startsWith((prefix + "autowhisper").toLowerCase()) || lmsg.startsWith((prefix + "aw").toLowerCase())) {
            String longCmd = prefix + "autowhisper";
            String shortCmd = prefix + "aw";
            String base = lmsg.startsWith(longCmd.toLowerCase()) ? longCmd : shortCmd;
            String remainder = msg.substring(base.length()).trim();
            if (remainder.toLowerCase().startsWith("set ")) {
                String value = remainder.substring(4).trim();
                ConfigManager.setAutoWhisper(value);
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aautoWhisper set."));
                event.setCanceled(true);
            }
            else if (remainder.isEmpty()) {
                String msgText = ConfigManager.getAutoWhisper();
                if (msgText == null || msgText.trim().isEmpty()) {
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cSet a message first."));
                } else if (mc.getConnection() == null) {
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cNot connected."));
                } else {
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7eWhispering to everyone..."));
                    new Thread(() -> {
                        try {
                            String self = mc.player.getName();
                            java.util.Collection<net.minecraft.client.network.NetworkPlayerInfo> players =
                                    mc.getConnection().getPlayerInfoMap();
                            int count = 0;
                            for (net.minecraft.client.network.NetworkPlayerInfo info : players) {
                                String name = info.getGameProfile().getName();
                                if (name == null || name.equalsIgnoreCase(self)) continue;
                                AutoScript.sendChat("/msg " + name + " " + msgText);
                                count++;
                                Thread.sleep(1000);
                            }
                            final int sent = count;
                            mc.addScheduledTask(() -> {
                                if (mc.player != null) {
                                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aSent " + sent + " DM(s)."));
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
                event.setCanceled(true);
            }
            else {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7eUsage: "
                        + "\u00A7f" + prefix + "autoWhisper set {string} \u00A77or \u00A7f" + prefix + "autoWhisper"));
                event.setCanceled(true);
            }

        } else if (lmsg.startsWith((prefix + "modprefix").toLowerCase()) || lmsg.startsWith((prefix + "mp").toLowerCase())) {
            String longCmd = prefix + "modprefix";
            String shortCmd = prefix + "mp";
            String base = lmsg.startsWith(longCmd.toLowerCase()) ? longCmd : shortCmd;
            String arg = msg.substring(base.length()).trim();
            if (ConfigManager.setCommandPrefix(arg)) {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7amodPrefix set to '\u00A7f" + ConfigManager.getCommandPrefix() + "\u00A7a'."));
            } else {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cPlease enter a character/symbol."));
            }
            event.setCanceled(true);

        } else if (lmsg.startsWith((prefix + "gotoground").toLowerCase()) || lmsg.startsWith((prefix + "gtg").toLowerCase())) {
            String longCmd = prefix + "gotoground";
            String shortCmd = prefix + "gtg";
            String base = lmsg.startsWith(longCmd.toLowerCase()) ? longCmd : shortCmd;
            String arg = msg.substring(base.length()).trim();
            if (ConfigManager.setGoToGround(arg)) {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7agoToGround set to \u00A7f" + (ConfigManager.getGoToGround() ? "true" : "false") + "\u00A7a."));
            } else {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cPlease enter a valid bool."));
            }
            event.setCanceled(true);

        } else if (lmsg.startsWith((prefix + "shulkercolor").toLowerCase()) || lmsg.startsWith((prefix + "sc").toLowerCase())) {
            String longCmd = prefix + "shulkercolor";
            String shortCmd = prefix + "sc";
            String base = lmsg.startsWith(longCmd.toLowerCase()) ? longCmd : shortCmd;
            String arg = msg.substring(base.length()).trim();
            if (ConfigManager.setShulkerColor(arg)) {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7ashulkerColor set to \u00A7f" + ConfigManager.getShulkerColorDisplay() + "\u00A7a."));
            } else {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cPlease enter a valid color."));
                mc.player.sendMessage(new TextComponentString("\u00A77\u00A7c(White, Light Gray, Gray, Black, Brown, Red, Orange, Yellow, Lime, Green, Cyan, Light Blue, Blue, Purple, Magenta, Pink)"));
            }
            event.setCanceled(true);

            } else if (lmsg.startsWith((prefix + "ignorestackedshulkers").toLowerCase()) || lmsg.startsWith((prefix + "iss").toLowerCase())) {
                String longCmd = prefix + "ignorestackedshulkers";
                String shortCmd = prefix + "iss";
                String base = lmsg.startsWith(longCmd.toLowerCase()) ? longCmd : shortCmd;
                String arg = msg.substring(base.length()).trim();
                if (ConfigManager.setIgnoreStackedShulkers(arg)) {
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aignoreStackedShulkers set to \u00A7f" + (ConfigManager.getIgnoreStackedShulkers() ? "true" : "false") + "\u00A7a."));
                } else {
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cPlease enter a valid bool."));
                }
                event.setCanceled(true);

        } else if (lmsg.startsWith((prefix + "ignorestackedshulkers").toLowerCase()) || lmsg.startsWith((prefix + "iss").toLowerCase())) {
            String longCmd = prefix + "ignorestackedshulkers";
            String shortCmd = prefix + "iss";
            String base = lmsg.startsWith(longCmd.toLowerCase()) ? longCmd : shortCmd;
            String arg = msg.substring(base.length()).trim();
            if (ConfigManager.setIgnoreStackedShulkers(arg)) {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aignoreStackedShulkers set to \u00A7f" + (ConfigManager.getIgnoreStackedShulkers() ? "true" : "false") + "\u00A7a."));
            } else {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cPlease enter a valid bool."));
            }
            event.setCanceled(true);

        } else {
            if (lmsg.startsWith((prefix + "set ").toLowerCase())) {
                int cmdLen = (prefix + "set").length();
                String argsPart = msg.substring(cmdLen).trim();
                if (!argsPart.isEmpty()) {
                    setItems = new ArrayList<>(Arrays.asList(argsPart.split("\\s+")));
                    ConfigManager.setSetBlock(argsPart);

                    List<String> displayItems = new ArrayList<>();
                    for (String item : setItems) {
                        displayItems.add("minecraft:" + item);
                    }
                    mc.player.sendMessage(
                            new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aSet: " + String.join(", ", displayItems))
                    );
                } else {
                    mc.player.sendMessage(new TextComponentString("Incorrect Usage! Please use " + prefix + "set {block(s)}."));
                }
                event.setCanceled(true);
            }
        }

        if (!event.isCanceled() && msg.startsWith(prefix)) {
            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7cIncorrect Usage. Please use " + prefix + "help!"));
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!enabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        String prefix = ConfigManager.getCommandPrefix();
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
                            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7fSuccessfully initialised. Mod is \u00A7cdisabled."));
                            String pfx = ConfigManager.getCommandPrefix();
                            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aSet a block (" + pfx + "set) before starting."));
                            mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A76Your prefix is: " + pfx));
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
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7fSuccessfully initialised. Mod is \u00A7cdisabled."));
                    String pfx = ConfigManager.getCommandPrefix();
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7aSet a block (" + pfx + "set) before starting."));
                    mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A76Your prefix is: " + pfx));
                    hasShownJoinMessage = true;
                }
            });
        }
        enforceWorldBorder(mc);

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
                                if (enabled) {
                                    AutoScript.sendChat("/kill");
                                }
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
                Thread.sleep(100);
                AutoScript.sendChat("#acceptableThrowawayItems obsidian");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void enforceWorldBorder(Minecraft mc) {
        if (mc.player == null || mc.world == null) return;

        double x = mc.player.posX;
        double z = mc.player.posZ;

        double clampedX = x;
        double clampedZ = z;
        boolean outside = false;

        if (x > WORLD_BORDER_HALF) {
            clampedX = WORLD_BORDER_HALF - 0.3;
            outside = true;
        } else if (x < -WORLD_BORDER_HALF) {
            clampedX = -WORLD_BORDER_HALF + 0.3;
            outside = true;
        }

        if (z > WORLD_BORDER_HALF) {
            clampedZ = WORLD_BORDER_HALF - 0.3;
            outside = true;
        } else if (z < -WORLD_BORDER_HALF) {
            clampedZ = -WORLD_BORDER_HALF + 0.3;
            outside = true;
        }

        if (outside) {
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
            mc.player.setPosition(clampedX, mc.player.posY, clampedZ);

            long now = System.currentTimeMillis();
            if (now - lastBoundaryWarnMs > 3000L) {
                mc.player.sendMessage(new TextComponentString("\u00A77[\u00A7fDictatorScript\u00A77] \u00A7eWorld border reached. Stay within X/Z [-1000, 1000]."));
                AutoScript.sendChat("#stop");
                lastBoundaryWarnMs = now;
            }
        }
    }


    public static List<String> getSetItems() {
        return setItems;
    }
}
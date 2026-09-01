package com.hfr.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hfr.clowder.Clowder;
import com.hfr.data.ClowderData;
import com.hfr.lib.RefStrings;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.client.ResearchOpenGuiPacket;
import com.hfr.technology.Technology;
import com.hfr.technology.TechnologyManager;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ResearchCommandHandler {

    public static final String ERROR = EnumChatFormatting.RED.toString();
    public static final String INFO = EnumChatFormatting.GREEN.toString();
    public static final String TITLE = EnumChatFormatting.GOLD.toString();
    public static final String LIST = EnumChatFormatting.YELLOW.toString();

    public static void execute(ICommandSender sender, String[] args) throws CommandException {
        if (!TechnologyManager.isConfigEnabled()) {
            sender.addChatMessage(new ChatComponentText(ERROR + "Technology system is disabled in hfr.cfg (hfr_enabled = false)."));
            return;
        }
        if (args.length == 0) {
            usage(sender);
            return;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("gui") || sub.equals("menu") || sub.equals("tree")) {
            EntityPlayerMP player = requirePlayer(sender);
            PacketDispatcher.wrapper.sendTo(new ResearchOpenGuiPacket(), player);
            return;
        }
        if (sub.equals("status") || sub.equals("list")) {
            cmdStatus(sender);
            return;
        }
        if (sub.equals("reload")) {
            reloadTree(sender);
            return;
        }
        if (sub.equals("info") && args.length >= 2) {
            cmdInfo(sender, args[1]);
            return;
        }
        if (sub.equals("research")) {
            if (args.length < 2) { sender.addChatMessage(new ChatComponentText(ERROR + "Usage: /xc research research <techId>")); return; }
            cmdResearch(sender, args[1]);
            return;
        }
        if (sub.equals("purchase") || sub.equals("buy")) {
            if (args.length < 2) { sender.addChatMessage(new ChatComponentText(ERROR + "Usage: /xc research purchase <techId>")); return; }
            cmdPurchase(sender, args[1]);
            return;
        }
        if (sub.equals("reset")) {
            if (args.length < 2) { sender.addChatMessage(new ChatComponentText(ERROR + "Usage: /xc research reset <player|faction> [techId|all]")); return; }
            cmdReset(sender, joinArgs(args, 1));
            return;
        }
        if (sub.equals("grant") || sub.equals("force")) {
            if (args.length < 3) { sender.addChatMessage(new ChatComponentText(ERROR + "Usage: /xc research grant <player|faction> <techId>")); return; }
            cmdGrant(sender, args[1], args[2]);
            return;
        }
        usage(sender);
    }

    private static void usage(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(TITLE + "Research commands:"));
        sender.addChatMessage(new ChatComponentText(INFO + "/xc research gui" + EnumChatFormatting.GRAY + " - open the technology tree GUI"));
        sender.addChatMessage(new ChatComponentText(INFO + "/xc research status" + EnumChatFormatting.GRAY + " - list loaded technologies"));
        sender.addChatMessage(new ChatComponentText(INFO + "/xc research info <techId>" + EnumChatFormatting.GRAY + " - show details"));
        sender.addChatMessage(new ChatComponentText(INFO + "/xc research research <techId>" + EnumChatFormatting.GRAY + " - research a tech (requires prerequisites)"));
        sender.addChatMessage(new ChatComponentText(INFO + "/xc research purchase <techId>" + EnumChatFormatting.GRAY + " - purchase a tech license"));
        sender.addChatMessage(new ChatComponentText(INFO + "/xc research reload" + EnumChatFormatting.GRAY + " - reload the technology tree JSON"));
        sender.addChatMessage(new ChatComponentText(INFO + "/xc research grant <player|faction> <techId>" + EnumChatFormatting.GRAY + " - admin force-grant"));
        sender.addChatMessage(new ChatComponentText(INFO + "/xc research reset <player|faction> [techId|all]" + EnumChatFormatting.GRAY + " - clear research/purchase"));
    }

    private static void cmdStatus(ICommandSender sender) {
        if (TechnologyManager.tree == null) {
            sender.addChatMessage(new ChatComponentText(ERROR + "No technology tree loaded. Drop a JSON at config/research/technology_tree.json and use /xc research reload."));
            return;
        }
        sender.addChatMessage(new ChatComponentText(TITLE + "Loaded technologies (" + TechnologyManager.tree.technologies.size() + "):"));
        sender.addChatMessage(new ChatComponentText(LIST + "Research currency: " + TechnologyManager.getResearchCurrency()));
        sender.addChatMessage(new ChatComponentText(LIST + "Purchase currency: " + TechnologyManager.getPurchaseCurrency()));
        int perPage = 12;
        int total = TechnologyManager.tree.technologies.size();
        int pages = (total + perPage - 1) / perPage;
        int page = 0;
        if (sender instanceof EntityPlayer) {
            String last = sender.getCommandSenderName();
        }
        for (int i = 0; i < total; i++) {
            Technology t = TechnologyManager.tree.technologies.get(i);
            String status = isUnlockedForViewer(sender, t) ? EnumChatFormatting.GREEN + "[OK]" : EnumChatFormatting.GRAY + "[locked]";
            sender.addChatMessage(new ChatComponentText(status + " " + EnumChatFormatting.AQUA + t.id + EnumChatFormatting.WHITE + " - " + t.name + EnumChatFormatting.GRAY + " (" + t.type + ")"));
        }
    }

    private static boolean isUnlockedForViewer(ICommandSender sender, Technology t) {
        if (sender instanceof EntityPlayer) {
            return TechnologyManager.isUnlocked((EntityPlayer) sender, t.id);
        }
        return true;
    }

    private static void cmdInfo(ICommandSender sender, String id) {
        Technology t = TechnologyManager.getTechnology(id);
        if (t == null) {
            sender.addChatMessage(new ChatComponentText(ERROR + "Unknown technology: " + id));
            return;
        }
        sender.addChatMessage(new ChatComponentText(TITLE + t.name + EnumChatFormatting.GRAY + " (" + t.id + ")"));
        sender.addChatMessage(new ChatComponentText(INFO + "Type: " + t.type + "  Tier: " + t.tier));
        if (t.researchCost > 0)
            sender.addChatMessage(new ChatComponentText(INFO + "Research cost: " + t.researchCost + " " + TechnologyManager.getResearchCurrency()));
        if (t.purchaseCost > 0)
            sender.addChatMessage(new ChatComponentText(INFO + "Purchase cost: " + t.purchaseCost + " " + TechnologyManager.getPurchaseCurrency()));
        if (!t.prerequisites.isEmpty())
            sender.addChatMessage(new ChatComponentText(INFO + "Requires: " + String.join(", ", t.prerequisites)));
        sender.addChatMessage(new ChatComponentText(INFO + "Items governed: " + t.items.size() + "  Recipes governed: " + t.recipes.size()));
    }

    private static void cmdResearch(ICommandSender sender, String id) {
        EntityPlayerMP player = requirePlayer(sender);
        Technology t = TechnologyManager.getTechnology(id);
        if (t == null) {
            sender.addChatMessage(new ChatComponentText(ERROR + "Unknown technology: " + id));
            return;
        }
        if (t.researchCost <= 0) {
            sender.addChatMessage(new ChatComponentText(ERROR + "That technology is not researchable."));
            return;
        }
        if (!TechnologyManager.canResearch(player, id)) {
            sender.addChatMessage(new ChatComponentText(ERROR + "You cannot research " + id + " (missing prerequisites or already researched)."));
            return;
        }
        TechnologyManager.research(player, id);
        ClowderData.getData(sender.getEntityWorld()).markDirty();
        sender.addChatMessage(new ChatComponentText(INFO + "Researched " + t.name + "."));
    }

    private static void cmdPurchase(ICommandSender sender, String id) {
        EntityPlayerMP player = requirePlayer(sender);
        Technology t = TechnologyManager.getTechnology(id);
        if (t == null) {
            sender.addChatMessage(new ChatComponentText(ERROR + "Unknown technology: " + id));
            return;
        }
        if (t.purchaseCost <= 0) {
            sender.addChatMessage(new ChatComponentText(ERROR + "That technology is not purchasable."));
            return;
        }
        if (!TechnologyManager.canPurchase(player, id)) {
            sender.addChatMessage(new ChatComponentText(ERROR + "You cannot purchase " + id + " right now."));
            return;
        }
        TechnologyManager.purchase(player, id);
        ClowderData.getData(sender.getEntityWorld()).markDirty();
        sender.addChatMessage(new ChatComponentText(INFO + "Purchased license for " + t.name + "."));
    }

    private static void reloadTree(ICommandSender sender) {
        if (TechnologyManager.config == null) {
            sender.addChatMessage(new ChatComponentText(ERROR + "Technology config not initialized."));
            return;
        }
        TechnologyManager.tree = com.hfr.technology.TechnologyLoader.load(com.hfr.main.MainRegistry.config);
        if (TechnologyManager.tree == null) {
            sender.addChatMessage(new ChatComponentText(ERROR + "Reload failed: tree still null. Check console for details."));
        } else {
            sender.addChatMessage(new ChatComponentText(INFO + "Reloaded " + TechnologyManager.tree.technologies.size() + " technologies."));
        }
    }

    private static void cmdGrant(ICommandSender sender, String target, String id) {
        Technology t = TechnologyManager.getTechnology(id);
        if (t == null) {
            sender.addChatMessage(new ChatComponentText(ERROR + "Unknown technology: " + id));
            return;
        }
        if (resolveFaction(sender, target) != null) {
            String factionName = Clowder.canonicalizeClowderName(target);
            for (Clowder c : Clowder.clowders) {
                if (c.name.equalsIgnoreCase(factionName) && c.techData != null) {
                    c.techData.setResearched(id, true);
                    c.techData.setPurchased(id, true);
                }
            }
            ClowderData.getData(sender.getEntityWorld()).markDirty();
            sender.addChatMessage(new ChatComponentText(INFO + "Granted " + id + " to faction " + factionName + "."));
            return;
        }
        EntityPlayerMP player = resolvePlayer(sender, target);
        if (player != null) {
            Clowder clowder = Clowder.getClowderFromPlayer(player);
            if (clowder != null && clowder.techData != null) {
                clowder.techData.setResearched(id, true);
                clowder.techData.setPurchased(id, true);
                ClowderData.getData(sender.getEntityWorld()).markDirty();
                sender.addChatMessage(new ChatComponentText(INFO + "Granted " + id + " to " + player.getCommandSenderName() + "."));
            } else {
                sender.addChatMessage(new ChatComponentText(ERROR + target + " is not in a faction."));
            }
            return;
        }
        sender.addChatMessage(new ChatComponentText(ERROR + "Unknown target: " + target));
    }

    private static void cmdReset(ICommandSender sender, String argsLine) {
        String[] parts = argsLine.split(" ", 2);
        String target = parts[0];
        String idSpec = parts.length > 1 ? parts[1] : "all";
        Clowder faction = resolveFaction(sender, target);
        if (faction != null) {
            applyReset(sender, faction.techData, idSpec, "faction " + faction.name);
            ClowderData.getData(sender.getEntityWorld()).markDirty();
            return;
        }
        EntityPlayerMP player = resolvePlayer(sender, target);
        if (player != null) {
            Clowder clowder = Clowder.getClowderFromPlayer(player);
            if (clowder != null && clowder.techData != null) {
                applyReset(sender, clowder.techData, idSpec, player.getCommandSenderName());
                ClowderData.getData(sender.getEntityWorld()).markDirty();
            } else {
                sender.addChatMessage(new ChatComponentText(ERROR + target + " has no faction."));
            }
            return;
        }
        sender.addChatMessage(new ChatComponentText(ERROR + "Unknown target: " + target));
    }

    private static void applyReset(ICommandSender sender, com.hfr.technology.FactionTechnologyData data, String idSpec, String who) {
        if (idSpec.equalsIgnoreCase("all")) {
            data.clearAll();
            sender.addChatMessage(new ChatComponentText(INFO + "Reset all research/purchase for " + who + "."));
        } else {
            data.setResearched(idSpec, false);
            data.setPurchased(idSpec, false);
            sender.addChatMessage(new ChatComponentText(INFO + "Reset " + idSpec + " for " + who + "."));
        }
    }

    public static Clowder resolveFaction(ICommandSender sender, String name) {
        String canon = Clowder.canonicalizeClowderName(name);
        for (Clowder c : Clowder.clowders) {
            if (c.name.equalsIgnoreCase(canon)) return c;
        }
        return null;
    }

    public static EntityPlayerMP resolvePlayer(ICommandSender sender, String name) {
        String[] matches = MinecraftServer.getServer().getAllUsernames();
        for (String n : matches) {
            if (n.equalsIgnoreCase(name)) {
                return MinecraftServer.getServer().getConfigurationManager().func_152612_a(name);
            }
        }
        return null;
    }

    public static EntityPlayerMP requirePlayer(ICommandSender sender) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) throw new CommandException("Must be a player.");
        return (EntityPlayerMP) sender;
    }

    public static String joinArgs(String[] args, int from) {
        return joinArgs(args, from, args.length);
    }

    public static String joinArgs(String[] args, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (i > from) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }

    public static List<String> getSubCommandCompletions(String[] args) {
        List<String> out = new ArrayList<String>();
        if (args.length == 1) {
            out.addAll(Arrays.asList("gui", "status", "info", "research", "purchase", "reload", "grant", "reset"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("research") || args[0].equalsIgnoreCase("purchase"))) {
            if (TechnologyManager.tree != null) {
                for (Technology t : TechnologyManager.tree.technologies) out.add(t.id);
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("grant"))) {
            for (Technology t : TechnologyManager.tree != null ? TechnologyManager.tree.technologies : java.util.Collections.<Technology>emptyList())
                out.add(t.id);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("reset")) {
            if (TechnologyManager.tree != null) {
                for (Technology t : TechnologyManager.tree.technologies) out.add(t.id);
                out.add("all");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            for (Clowder c : Clowder.clowders) out.add(c.name);
            String[] matches = MinecraftServer.getServer().getAllUsernames();
            for (String n : matches) out.add(n);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("grant")) {
            for (Clowder c : Clowder.clowders) out.add(c.name);
            String[] matches = MinecraftServer.getServer().getAllUsernames();
            for (String n : matches) out.add(n);
        }
        return out;
    }

    public static String USAGE = "research <gui|status|info|research|purchase|reload|grant|reset>";
}
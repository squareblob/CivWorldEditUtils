package com.squareblob.civworldeditutils.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.math.BlockVector3;
import com.squareblob.civworldeditutils.CivWorldEditUtils;
import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

@CivCommand(id = "setreinforcement")
public class setReinforcement extends StandaloneCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        // masks may contain spaces - temporary code as too lazy to use whatever worldedit uses for mask parsing :/
        int maskArgLength = 0;
        if (args[0].startsWith("\"")) {
            while (args[maskArgLength].contains("\"")) {
                maskArgLength++;
            }
        } else {
            maskArgLength = 1;
        }

        // initialize defaults for optional args
        Float health = null;
        ReinforcementType reinType = null;

        String[] optionalArgs = Arrays.copyOfRange(args, maskArgLength + 1, args.length);
        for (String arg : optionalArgs) {
            String[] argDeclaration = arg.split("=");
            String argName = argDeclaration[0];
            String argValue = argDeclaration[1];
            switch (argName.toLowerCase()) {
                case "health":
                    health = Float.parseFloat(argValue);
                    break;
                case "material":
                case "mat":
                    reinType = Citadel.getInstance().getReinforcementTypeManager().getAllTypes().stream()
                            .filter(type -> type.getName().equalsIgnoreCase(argValue)).findFirst().orElse(null);
                    if (reinType == null) {
                        CitadelUtility.sendAndLog(p, ChatColor.RED, "Unable to parse given reinforcement type");
                        return false;
                    }
                    break;
            }
        }
        if (reinType == null) {
            reinType = Citadel.getInstance().getReinforcementTypeManager()
                    .getByItemStack(p.getInventory().getItemInMainHand());
        }
        MaskIntersection mask;
        try {
            String[] masksToParse = Arrays.copyOfRange(args, 0, maskArgLength);
            mask = CivWorldEditUtils.getInstance().parseMask(p, masksToParse);
        } catch (InputParseException e) {
            e.printStackTrace();
            CitadelUtility.sendAndLog(p, ChatColor.RED, "Unable to parse given mask");
            return false;
        }
        Group group = GroupManager.getGroup(args[maskArgLength]);
        if (group == null) {
            CitadelUtility.sendAndLog(p, ChatColor.RED, "That group does not exist.");
            return false;
        }
        if (reinType == null) {
            CitadelUtility.sendAndLog(p, ChatColor.RED, "This item is not a valid reinforcement material");
            return false;
        }
        try {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
            int affected = 0;
            for (BlockVector3 pos : session.getSelection(session.getSelectionWorld())) {
                if (mask.test(pos)) {
                    Location loc = new Location(p.getWorld(), pos.getX(), pos.getY(), pos.getZ());
                    Block block = p.getWorld().getBlockAt(loc);
                    boolean output;
                    if (health != null) {
                        output = CivWorldEditUtils.getInstance().reinforceBlock(p, block, reinType, group, 5, health);
                    } else {
                        output = CivWorldEditUtils.getInstance().reinforceBlock(p, block, reinType, group, 5);
                    }
                    if (output) {
                        affected++;
                    }
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        for (BlockVector3 pos : session.getSelection(session.getSelectionWorld())) {
                            Location loc = new Location(p.getWorld(), pos.getX(), pos.getY(), pos.getZ());
                            BastionBlock bastionBlock = Bastion.getBastionStorage().getBastionBlock(loc);
                            if (bastionBlock != null) {
                                bastionBlock.mature();
                            }
                        }
                    } catch (IncompleteRegionException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskLater(CivWorldEditUtils.getInstance(), 10);
            CitadelUtility.sendAndLog(p, ChatColor.GREEN, "Successfully created " + affected + " reinforcements");
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // masks may contain spaces - temporary code as too lazy to use whatever worldedit uses for mask parsing :/
        int maskOffset = 2;
        if (args.length <= 1) {
            return WorldEdit.getInstance().getMaskFactory().getSuggestions(args[args.length - 1]);
        } else if (args[0].startsWith("\"")) {
            for (int i = 1; i < args.length; i++) {
                if (args[i].endsWith("\"")) {
                    maskOffset = i;
                    break;
                }
            }
            if (maskOffset == 2) {
                return WorldEdit.getInstance().getMaskFactory().getSuggestions(args[args.length - 1]);
            }
        }
        if (args.length == maskOffset) {
            return GroupTabCompleter.complete(args[args.length - 1], null, (Player) sender);
        } else {
            if (args[args.length - 1].startsWith("material=")) {
                return Citadel.getInstance().getReinforcementTypeManager().getAllTypes()
                        .stream().map(ReinforcementType::getName)
                        .filter(name -> name.toLowerCase()
                                .startsWith(args[args.length - 1].toLowerCase().replace("material=", "")))
                        .map(s -> "material=" + s)
                        .collect(Collectors.toList());
            } else {
                List<String> optionalArgStrings = new ArrayList<>();
                optionalArgStrings.add("material=");
                optionalArgStrings.add("health=");
                return optionalArgStrings;
            }
        }
    }
}
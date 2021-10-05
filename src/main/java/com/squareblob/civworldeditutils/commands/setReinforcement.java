package com.squareblob.civworldeditutils.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.math.BlockVector3;
import com.squareblob.civworldeditutils.CivWorldEditUtils;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        String groupToParse = args[args.length - 1];
        String[] masksToParse = Arrays.copyOfRange(args, 0, args.length - 1);
        MaskIntersection mask;

        try {
            mask = CivWorldEditUtils.getInstance().parseMask(p, masksToParse);
        } catch (InputParseException e) {
            e.printStackTrace();
            CitadelUtility.sendAndLog(p, ChatColor.RED, "Unable to parse given mask");
            return false;
        }

        Group group = GroupManager.getGroup(groupToParse);
        if (group == null) {
            CitadelUtility.sendAndLog(p, ChatColor.RED, "That group does not exist.");
            return false;
        }

        ReinforcementType reinType = Citadel.getInstance().getReinforcementTypeManager()
                .getByItemStack(p.getInventory().getItemInMainHand());
        if (reinType == null) {
            CitadelUtility.sendAndLog(p, ChatColor.RED, "The item you are holding is not a possible reinforcement");
            return false;
        }

        try {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
            int affected = 0;
            for (BlockVector3 pos : session.getSelection(session.getSelectionWorld())) {
                if (mask.test(pos)) {
                    Location loc = new Location(p.getWorld(), pos.getX(), pos.getY(), pos.getZ());
                    Block block = p.getWorld().getBlockAt(loc);
                    boolean output = CivWorldEditUtils.getInstance().reinforceBlock(p, block, reinType, group);
                    if (output) {
                        affected++;
                    }
                }
            }
            CitadelUtility.sendAndLog(p, ChatColor.GREEN, "Successfully created " + affected + " reinforcements");
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return WorldEdit.getInstance().getMaskFactory().getSuggestions(args[args.length - 1]);
        } else {
            return GroupTabCompleter.complete(args[args.length - 1], null, (Player) sender);
        }
    }
}
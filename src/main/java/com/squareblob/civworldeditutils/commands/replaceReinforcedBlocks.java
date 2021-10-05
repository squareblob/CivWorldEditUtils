package com.squareblob.civworldeditutils.commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.squareblob.civworldeditutils.CivWorldEditUtils;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

@CivCommand(id = "replacereinforcedblocks")
public class replaceReinforcedBlocks extends StandaloneCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        String groupToParse = args[0];
        String patternToParse = args[1];
        Pattern pattern;

        try {
            pattern = CivWorldEditUtils.getInstance().parsePattern(p, patternToParse);
        } catch (InputParseException e) {
            e.printStackTrace();
            CitadelUtility.sendAndLog(p, ChatColor.RED, "Unable to parse given pattern");
            return false;
        }

        Group group = GroupManager.getGroup(groupToParse);
        if (group == null) {
            CitadelUtility.sendAndLog(p, ChatColor.RED, "That group does not exist.");
            return false;
        }

        try {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
            ReinforcementManager rm = Citadel.getInstance().getReinforcementManager();
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(p.getWorld()))) {
                int affected = 0;
                for (BlockVector3 pos : session.getSelection(session.getSelectionWorld())) {
                    Location loc = new Location(p.getWorld(), pos.getX(), pos.getY(), pos.getZ());
                    Reinforcement rein = rm.getReinforcement(loc);
                    if (rein == null) {
                        continue;
                    }
                    if (rein.getGroup().equals(group)) {
                        editSession.setBlock(pos, pattern);
                        affected++;
                    }
                }
                session.remember(editSession);
                CitadelUtility.sendAndLog(p, ChatColor.GREEN, "Successfully affected " + affected + " blocks");
            }
        } catch (IncompleteRegionException | MaxChangedBlocksException e) {
            e.printStackTrace();
            CitadelUtility.sendAndLog(p, ChatColor.RED,
                    "Failed to create bastions. Ensure you have a valid region selected");
            return false;
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return GroupTabCompleter.complete(args[0], null, (Player) sender);
        } else {
            return WorldEdit.getInstance().getPatternFactory().getSuggestions(args[args.length - 1]);
        }
    }
}
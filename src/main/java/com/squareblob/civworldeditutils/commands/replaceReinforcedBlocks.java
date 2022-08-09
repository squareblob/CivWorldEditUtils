package com.squareblob.civworldeditutils.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.squareblob.civworldeditutils.CivWorldEditUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

public class replaceReinforcedBlocks extends BaseCommand {

    @CommandAlias("replacereinforcedblocks|replacereinblocks")
    @Syntax("<group> <pattern> <reinmat>")
    @Description("Replace blocks reinforced to a given group in current WorldEdit selection with a block pattern selection")
    @CommandCompletion("@groups @patterns @reintypes")
    @CommandPermission("civworldeditutils.admin")
    public void execute(Player player, String groupToParse, String patternToParse, @Optional String targetRein) {
        ReinforcementType reinTypeToHighlight = null;
        if (targetRein != null) {
            Material reinMat = Material.matchMaterial(targetRein);
            if (reinMat == null) {
                CitadelUtility.sendAndLog(player, ChatColor.RED, "Unable to parse given material");
                return;
            }
            reinTypeToHighlight = Citadel.getInstance().getReinforcementTypeManager()
                    .getByItemStack(new ItemStack(reinMat), player.getWorld().getName());
        }
        Pattern pattern;

        try {
            pattern = CivWorldEditUtils.getInstance().parsePattern(player, patternToParse);
        } catch (InputParseException e) {
            e.printStackTrace();
            CitadelUtility.sendAndLog(player, ChatColor.RED, "Unable to parse given pattern");
            return;
        }

        Group group = GroupManager.getGroup(groupToParse);
        if (group == null) {
            CitadelUtility.sendAndLog(player, ChatColor.RED, "That group does not exist.");
            return;
        }

        try {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            ReinforcementManager rm = Citadel.getInstance().getReinforcementManager();
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getWorld()))) {
                int affected = 0;
                Region selection = session.getSelection(session.getSelectionWorld());
                if (selection.getMaximumPoint().getY() > 255) {
                    CitadelUtility.sendAndLog(player, ChatColor.RED, "Your selection may not extend beyond y255");
                    return;
                }
                for (BlockVector3 pos : selection) {
                    Location loc = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());
                    Reinforcement rein = rm.getReinforcement(loc);
                    if (rein == null) {
                        continue;
                    }
                    if (!rein.getGroup().equals(group)) {
                        continue;
                    }
                    if (reinTypeToHighlight != null) {
                        if (!rein.getType().equals(reinTypeToHighlight)) {
                            continue;
                        }
                    }
                    editSession.setBlock(pos, pattern);
                    affected++;
                }
                session.remember(editSession);
                CitadelUtility.sendAndLog(player, ChatColor.GREEN, "Successfully affected " + affected + " blocks");
            }
        } catch (IncompleteRegionException | MaxChangedBlocksException e) {
            e.printStackTrace();
            CitadelUtility.sendAndLog(player, ChatColor.RED,
                    "Error replacing reinforced blocks. Ensure you have a valid region selected");
        }
    }
}
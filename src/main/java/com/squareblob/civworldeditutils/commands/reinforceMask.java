package com.squareblob.civworldeditutils.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.squareblob.civworldeditutils.CivWorldEditUtils;
import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

public class reinforceMask extends BaseCommand {

    @CommandAlias("reinforcemask|reinmask")
    @Syntax("<group> <material> <health>")
    @Description("Reinforce blocks in WorldEdit selection matching current civmask")
    @CommandCompletion("@groups @reintypes")
    @CommandPermission("civworldeditutils.admin")
    @Conditions("maskset")
    public void execute(Player player, String groupName, String reinName, @Optional Float health) {
        if (CivWorldEditUtils.getInstance().getCivmask() == null) {
            CitadelUtility.sendAndLog(player, ChatColor.RED, "You must first set a mask");
            return;
        }
        Group group = GroupManager.getGroup(groupName);
        if (group == null) {
            CitadelUtility.sendAndLog(player, ChatColor.RED, "That group does not exist.");
            return;
        }
        ReinforcementType reinType = Citadel.getInstance().getReinforcementTypeManager().getAllTypes().stream()
                .filter(type -> type.getName().equalsIgnoreCase(reinName)).findFirst().orElse(null);
        if (reinType == null) {
            CitadelUtility.sendAndLog(player, ChatColor.RED, "Unable to parse given reinforcement type");
            return;
        }
        try {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            Region selectionRegion =  session.getSelection(session.getSelectionWorld());
            Mask mask = CivWorldEditUtils.getInstance().getCivmask();
            int affected = 0;
            for (BlockVector3 pos : selectionRegion) {
                if (mask.test(pos)) {
                    Location loc = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());
                    Block block = player.getWorld().getBlockAt(loc);
                    boolean output;
                    if (health != null) {
                        output = CivWorldEditUtils.getInstance().reinforceBlock(player, block, reinType, group, 5, health);
                    } else {
                        output = CivWorldEditUtils.getInstance().reinforceBlock(player, block, reinType, group, 5);
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
                            Location loc = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());
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
            CitadelUtility.sendAndLog(player, ChatColor.GREEN, "Successfully created " + affected + " reinforcements");
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        }
    }
}
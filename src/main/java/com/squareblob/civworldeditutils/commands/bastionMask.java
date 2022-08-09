package com.squareblob.civworldeditutils.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.squareblob.civworldeditutils.CivWorldEditUtils;
import isaac.bastion.Bastion;
import isaac.bastion.BastionType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.CitadelUtility;

public class bastionMask extends BaseCommand {

    @CommandAlias("bastionmask")
    @Syntax("<bastiontype>")
    @Description("Create pending bastions in WorldEdit selection from blocks matching current civmask")
    @CommandCompletion("@bastiontypes")
    @CommandPermission("civworldeditutils.admin")
    @CatchUnknown @Default
    public void execute(Player player, String bastionName) {
        if (CivWorldEditUtils.getInstance().getCivmask() == null) {
            CitadelUtility.sendAndLog(player, ChatColor.RED, "You must first set a mask");
            return;
        }
        BastionType type = BastionType.getBastionType(bastionName);
        if (type == null) {
            CitadelUtility.sendAndLog(player, ChatColor.RED, "Unable to parse given bastion type");
            return;
        }
        try {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            Mask mask = CivWorldEditUtils.getInstance().getCivmask();
            int affected = 0;
            for (BlockVector3 pos : session.getSelection(session.getSelectionWorld())) {
                if (mask.test(pos)) {
                    Location loc = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());
                    Bastion.getBastionStorage().addPendingBastion(loc, type);
                    affected++;
                }
            }
            CitadelUtility.sendAndLog(player, ChatColor.GREEN, "Successfully created " + affected + " pending bastions");
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
            CitadelUtility.sendAndLog(player, ChatColor.RED,
                    "Failed to create bastions. Ensure you have a valid region selected");
        }
    }
}
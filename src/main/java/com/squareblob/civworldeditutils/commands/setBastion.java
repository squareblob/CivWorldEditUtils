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
import isaac.bastion.BastionType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "setbastion")
public class setBastion extends StandaloneCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        String[] masksToParse = Arrays.copyOfRange(args, 0, args.length - 1);
        String typeToParse = args[args.length - 1];
        MaskIntersection mask;

        try {
            mask = CivWorldEditUtils.getInstance().parseMask(p, masksToParse);
        } catch (InputParseException e) {
            e.printStackTrace();
            CitadelUtility.sendAndLog(p, ChatColor.RED, "Unable to parse given mask");
            return false;
        }

        BastionType type = BastionType.getBastionType(typeToParse);
        if (type == null) {
            CitadelUtility.sendAndLog(p, ChatColor.RED, "Unable to parse given bastion type");
            return false;
        }

        try {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
            int affected = 0;
            for (BlockVector3 pos : session.getSelection(session.getSelectionWorld())) {
                if (mask.test(pos)) {
                    Location loc = new Location(p.getWorld(), pos.getX(), pos.getY(), pos.getZ());
                    Bastion.getBastionStorage().addPendingBastion(loc, type);
                    affected++;
                }
            }
            CitadelUtility.sendAndLog(p, ChatColor.GREEN, "Successfully created " + affected + " pending bastions");

        } catch (IncompleteRegionException e) {
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
            return WorldEdit.getInstance().getMaskFactory().getSuggestions(args[args.length - 1]);
        } else {
            return Collections.singletonList(BastionType.getDefaultType());
        }
    }
}
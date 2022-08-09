package com.squareblob.civworldeditutils.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.*;
import com.squareblob.civworldeditutils.CivWorldEditUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.CitadelUtility;

public class civMask extends BaseCommand {

    @CommandAlias("civmask|cmask")
    @Syntax("<mask>")
    @Description("Creates a mask to apply to future commands")
    @CommandCompletion("@masks")
    @CommandPermission("civworldeditutils.admin")
    public void execute(Player player, String[] mask) {
        ParserContext parserContext = new ParserContext();
        parserContext.setExtent(BukkitAdapter.adapt(player).getExtent());
        parserContext.setWorld(BukkitAdapter.adapt(player.getWorld()));
        try {
            Mask m = WorldEdit.getInstance().getMaskFactory().parseFromInput(String.join(" ", mask), parserContext);
            CivWorldEditUtils.getInstance().setCivmask(m);
            CitadelUtility.sendAndLog(player, ChatColor.GREEN, "Mask set");
        } catch (InputParseException e) {
            CitadelUtility.sendAndLog(player, ChatColor.RED, "That mask is invalid");
        }
      }
}

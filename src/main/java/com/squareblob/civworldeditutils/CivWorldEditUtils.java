package com.squareblob.civworldeditutils;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.pattern.Pattern;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.namelayer.group.Group;

public class CivWorldEditUtils extends ACivMod {
    private static CivWorldEditUtils instance;

    public static CivWorldEditUtils getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
    }

    public MaskIntersection parseMask(Player p, String[] args) throws InputParseException {
        MaskIntersection mask = new MaskIntersection();
        ParserContext parserContext = new ParserContext();
        parserContext.setExtent(BukkitAdapter.adapt(p).getExtent());
        parserContext.setWorld(BukkitAdapter.adapt(p.getWorld()));
        for (String maskToParse : args) {
            maskToParse = maskToParse.replaceAll("\"", "");
            Mask maskTemp = WorldEdit.getInstance().getMaskFactory().parseFromInput(maskToParse, parserContext);
            mask.add(maskTemp);
        }
        return mask;
    }

    public Pattern parsePattern(Player p, String patternToParse) throws InputParseException {
        ParserContext parserContext = new ParserContext();
        parserContext.setActor(BukkitAdapter.adapt(p));
        parserContext.setExtent(BukkitAdapter.adapt(p).getExtent());
        parserContext.setWorld(BukkitAdapter.adapt(p.getWorld()));
        return WorldEdit.getInstance().getPatternFactory().parseFromInput(patternToParse, parserContext);
    }

    public boolean reinforceBlock(Player p, Block block, ReinforcementType reinType, Group group) {
        if (!reinType.canBeReinforced(block.getType())) {
            return false;
        }
        Block protecting = ReinforcementLogic.getResponsibleBlock(block);
        if (!block.getLocation().equals(protecting.getLocation())) {
            return false;
        }
        if (block.getType().isAir()) {
            return false;
        }
        Reinforcement rein = ReinforcementLogic.callReinforcementCreationEvent(p, block, reinType, group);
        if (rein != null) {
            ReinforcementLogic.createReinforcement(rein);
        }
        return true;
    }


}

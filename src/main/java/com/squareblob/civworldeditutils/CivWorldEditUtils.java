package com.squareblob.civworldeditutils;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.MaskUnion;
import com.sk89q.worldedit.function.pattern.Pattern;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.namelayer.group.Group;

public class CivWorldEditUtils extends ACivMod {
    private static CivWorldEditUtils instance;
    private CWEUConfigManager config;

    public static CivWorldEditUtils getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        config = new CWEUConfigManager(this);
        if (!config.parse()) {
            getLogger().severe("Errors in config file, shutting down");
            Bukkit.shutdown();
        }
    }

    // Don't do this... I'm sure there's something in the WE api
    public MaskIntersection parseMask(Player p, String[] args) throws InputParseException {
        MaskIntersection mask = new MaskIntersection();
        ParserContext parserContext = new ParserContext();
        parserContext.setExtent(BukkitAdapter.adapt(p).getExtent());
        parserContext.setWorld(BukkitAdapter.adapt(p.getWorld()));
        for (String maskToParse : args) {
            maskToParse = maskToParse.replaceAll("\"", "");
            MaskUnion maskUnion = new MaskUnion();
            for (String maskUnionToParse : maskToParse.split(",")) {
                Mask maskTemp =
                        WorldEdit.getInstance().getMaskFactory().parseFromInput(maskUnionToParse, parserContext);
                maskUnion.add(maskTemp);
            }
            mask.add(maskUnion);
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

    public boolean reinforceBlock(Player p, Block block, ReinforcementType reinType, Group group, long daysOld) {
        return reinforceBlock(p, block, reinType, group, daysOld, reinType.getHealth());
    }

    public boolean reinforceBlock(Player p, Block block, ReinforcementType reinType, Group group, long daysOld,
                                  float health) {
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

        Reinforcement rein = new Reinforcement(block.getLocation(), reinType, group.getGroupId(),
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysOld),
                health,
                false,
                true
        );
        ReinforcementCreationEvent event = new ReinforcementCreationEvent(p, rein);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            ReinforcementLogic.createReinforcement(rein);
        }
        return true;
    }

    public CWEUConfigManager getConfigManager() {
        return config;
    }

}
package com.squareblob.civworldeditutils.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import com.sk89q.worldedit.WorldEdit;
import com.squareblob.civworldeditutils.CivWorldEditUtils;
import com.squareblob.civworldeditutils.reinforcementPreset;
import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.storage.BastionBlockStorage;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.stream.Collectors;

public class CWEUCommandManager extends CommandManager {

    public CWEUCommandManager(Plugin plugin) {
        super(plugin);
        init();
    }

    @Override
    public void registerCommands() {
        registerCommand(new bastionMask());
        registerCommand(new civMask());
        registerCommand(new reinforceMask());
        registerCommand(new replaceReinforcedBlocks());
        registerCommand(new setReinforcementPreset());
    }

    @Override
    public void registerCompletions(@Nonnull CommandCompletions<BukkitCommandCompletionContext> completions) {
        super.registerCompletions(completions);
        completions.registerCompletion("groups", (context) -> GroupTabCompleter.complete(context.getInput(), null, context.getPlayer()));
        completions.registerCompletion("patterns", (context) -> WorldEdit.getInstance().getPatternFactory().getSuggestions(context.getInput()));
        completions.registerCompletion("masks", (context -> WorldEdit.getInstance().getMaskFactory().getSuggestions(context.getInput())));
        completions.registerCompletion("reintypes", (context) -> Citadel.getInstance().getReinforcementTypeManager().getAllTypes()
                .stream().map(ReinforcementType::getName)
                .filter(name -> name.toLowerCase().startsWith(context.getInput().toLowerCase()))
                .collect(Collectors.toList()));
        // todo
        completions.registerCompletion("bastiontypes", (context -> Collections.singletonList(BastionType.getDefaultType())));
        completions.registerCompletion("reinpresets", context -> CivWorldEditUtils.getInstance().getConfigManager().getReinforcementPresets().stream().map(
                reinforcementPreset::getName).collect(Collectors.toList()));
    }
}
package com.squareblob.civworldeditutils.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.squareblob.civworldeditutils.CWEUConfigManager;
import com.squareblob.civworldeditutils.CivWorldEditUtils;
import com.squareblob.civworldeditutils.reinforcementPreset;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.citadel.CitadelUtility;

public class setReinforcementPreset extends BaseCommand {

    @CommandAlias("setreinforcementpreset")
    @Syntax("<presetName> <groupA> ... <groupZ>")
    @Description("Run a list of reinforcement commands specified in config.yml")
    @CommandCompletion("@reinpresets @groups")
    @CommandPermission("civworldeditutils.admin")
    public boolean execute(CommandSender sender, String presetName, String[] args) {
        CWEUConfigManager configManager = CivWorldEditUtils.getInstance().getConfigManager();
        reinforcementPreset preset = configManager.getReinforcementPresets().stream()
                .filter(p -> p.getName().equalsIgnoreCase(presetName)).findFirst().orElse(null);
        if (preset == null) {
            CitadelUtility.sendAndLog(sender, ChatColor.RED, "No preset called " + presetName + " could be found");
            return false;
        }
        int numGroups = preset.getGroups().size();
        if (numGroups != args.length) {
            CitadelUtility.sendAndLog(sender, ChatColor.RED, "The preset " + presetName + " requires" + numGroups + " groups");
            return false;
        }
        for (String command : preset.getCommands()) {
            for (int i = 0; i < preset.getGroups().size(); i++) {
                command = command.replace("$" + preset.getGroups().get(i), args[i + 1]);
            }
            // TODO : Only run command if it belongs to this plugin
            sender.sendMessage(ChatColor.BLUE + "running " + ChatColor.AQUA + command);
            Bukkit.getServer().dispatchCommand(sender, command);
        }
        return true;
    }
}
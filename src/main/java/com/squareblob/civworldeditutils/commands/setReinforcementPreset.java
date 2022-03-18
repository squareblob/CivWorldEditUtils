package com.squareblob.civworldeditutils.commands;

import com.squareblob.civworldeditutils.CWEUConfigManager;
import com.squareblob.civworldeditutils.CivWorldEditUtils;
import com.squareblob.civworldeditutils.reinforcementPreset;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;

@CivCommand(id = "setreinforcementpreset")
public class setReinforcementPreset extends StandaloneCommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        String name = args[0];
        CWEUConfigManager configManager = CivWorldEditUtils.getInstance().getConfigManager();
        reinforcementPreset preset = configManager.getReinforcementPresets().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        if (preset == null) {
            CitadelUtility.sendAndLog(sender, ChatColor.RED, "No preset called " + name + " could be found");
            return false;
        }
        if (preset.getGroups().size() != args.length - 1) {
            CitadelUtility.sendAndLog(sender, ChatColor.RED, "You must the specified number of valid groups");
            return false;
        }
        for (String command : preset.getCommands()) {
            for (int i = 0; i < preset.getGroups().size(); i++) {
                command = command.replace("$" + preset.getGroups().get(i), args[i + 1]);
            }
            sender.sendMessage(ChatColor.BLUE + "running " + ChatColor.AQUA + command);
            Bukkit.getServer().dispatchCommand(sender, command);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return CivWorldEditUtils.getInstance().getConfigManager().getReinforcementPresets().stream().map(
                    reinforcementPreset::getName).collect(Collectors.toList());
        } else {
            return GroupTabCompleter.complete(args[args.length - 1], null, (Player) sender);
        }
    }
}
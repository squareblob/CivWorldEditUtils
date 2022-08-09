package com.squareblob.civworldeditutils;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.config.ConfigParser;

public class CWEUConfigManager extends ConfigParser {

    private List<reinforcementPreset> reinforcementPresets;

    public CWEUConfigManager(ACivMod plugin) {
        super(plugin);
    }

    public List<reinforcementPreset> getReinforcementPresets() {
        return reinforcementPresets;
    }

    @Override
    protected boolean parseInternal(ConfigurationSection config) {
        ConfigurationSection presetList = config.getConfigurationSection("reinforcement_presets");
        reinforcementPresets = new LinkedList<>();
        if (presetList == null) {
            logger.info("No reinforcement presetList found in config");
            return false;
        }
        for (String key : presetList.getKeys(false)) {
            if (!presetList.isConfigurationSection(key)) {
                logger.warning("Ignoring invalid entry " + key + " at " + presetList.getCurrentPath());
                continue;
            }
            ConfigurationSection preset = presetList.getConfigurationSection(key);
            if (preset == null) {
                logger.warning("Ignoring invalid preset " + key + " at " + presetList.getCurrentPath());
                continue;
            }
            List<String> groups = preset.getStringList("groups");
            List<String> commands = preset.getStringList("commands");
            reinforcementPresets.add(new reinforcementPreset(key, groups, commands));
        }
        return true;
    }

}

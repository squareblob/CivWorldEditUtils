package com.squareblob.civworldeditutils;

import java.util.List;

public class reinforcementPreset {
    private String name;
    private List<String> groups;
    private List<String> commands;

    public reinforcementPreset(String key, List<String> groups, List<String> commands) {
        this.name = key;
        this.groups = groups;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public List<String> getGroups() {
        return groups;
    }

    public List<String> getCommands() {
        return commands;
    }
}

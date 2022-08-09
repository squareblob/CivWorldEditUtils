Some hacky commands to make it easier to use Civ plugins with WorldEdit

# Usage Examples

## civMask
Saves a WorldEdit [mask](https://worldedit.enginehub.org/en/latest/usage/general/masks/) which can later be used with commands like `reinforceMask` and `bastionMask`.

Examples:
`/cmask obsidian <##doors` will select obsidian directly beneath a door

## reinforceMask
Reinforce blocks in WorldEdit selection matching current civmask

## bastionMask
Create pending bastions in WorldEdit selection from blocks matching current civmask

## setReinforcementPreset
Run a list of civmask and reinforcement commands specified in config.yml

Format:
`/setreinpreset default someGroup`

## replaceReinforcedBlocks
Replace blocks reinforced to a given group in current WorldEdit selection with a block pattern
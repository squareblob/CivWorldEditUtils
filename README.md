Some hacky commands to make it easier to use Civ plugins with WorldEdit

# Usage Examples

## setReinforcement

Set all blocks matching a [mask](https://worldedit.enginehub.org/en/latest/usage/general/masks/) in current WorldEdit
selection to given group. Optional arguments for reinforcement material (held item in main hand by default) and health.
Examples:

`/setrein #existing someGroup` (reinforces all blocks in selection)

`/setrein "obsidian <##doors" someGroup mat=diamond` (reinforces obsidian beneath doors)

Note that this plugin parses masks in a very janky manner and not all types of masks are supported.

## setReinforcementPreset

Run a list of reinforcement commands specified in config.yml

`/setreinpreset default someGroup`

## setBastion

Create pending bastions for blocks matching a given mask in current WorldEdit selection

## replaceReinforcedBlocks

Replace blocks reinforced to a given group in current WorldEdit selection with a block pattern
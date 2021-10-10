Simple and hacky commands to make it easier to use Civ plugins with WorldEdit

# Usage Examples

## setreinforcement

Sets all blocks matching a [mask](https://worldedit.enginehub.org/en/latest/usage/general/masks/) in current WorldEdit
selection to given group. Some masks include:

`/setrein #existing someGroup`

`/setrein "obsidian <doors" someGroup`

`/setrein ##doors,##trapdoors someGroup`

Note that this plugin parses masks in a very janky manner and not all types of masks are supported.

# todo

- properly parse masks and fix args accepted by commands
- override FAWE mask syntax


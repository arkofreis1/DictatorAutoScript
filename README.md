# Dictator Auto-Script

**Dictator Auto-Script** is a client-side automation tool for Dictator members on the Minecraft server 5b5t.org. It allows you to automate block mining using Baritone and automatically regears if killed via Baritone path finding to an Ender Chest.

---

## Requirements

- Baritone for 1.12.2.
- AutoArmor, AutoTotem, AutoRespawn, AutoReconnect.
- The script looks for Black shulker boxes in the Ender Chest.

---

## Installation

1. Make sure you have **Minecraft Forge 1.12.2** installed.
2. Place the compiled `.jar` file into your `mods` folder via `%appdata%` -> `.minecraft`.
3. Launch Minecraft with the Forge profile selected.

---

## Commands

All commands are prefixed with a comma `,`:

| Command | Description |
|---------|-------------|
| `,enable` | Enables Dictator Auto-Script and Baritone arguments. |
| `,disable` | Disables Dictator Auto-Script. |
| `,set {block(s)}` | Set blocks you want to mine. Separate multiple blocks with spaces. Example: `,set diamond_ore gold_ore`. |
| `,start` | Start mining the selected blocks. |
| `,stop` | Stops Baritone automation. |
| `,help` | Shows a help message with available commands. |

---

## Example Workflow

1. Enable the mod:

```text
,enable (Enables mod and runs all the necessary Baritone commands)
```

2. Set the blocks you want to mine:

```text
,set diamond_ore iron_ore (Tells Baritone what to mine & look for)
```

3. Start mining:

```text
/kill (Script automatically runs onDeath)
```

4. Stop automation anytime:

```text
,disable
```

---

## Contributing

Contributions are welcome! Feel free to submit bug reports, feature requests, or pull requests.

---

## License

This project is free for personal use. Redistribution or use on public servers is permitted.


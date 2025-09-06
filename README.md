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
2. Place the compiled `.jar` file into your `mods` folder via `%appdata% -> `.minecraft`.
3. Launch Minecraft with the Forge profile selected.

---

## Commands

All commands are prefixed with a comma `,`:

| Command | Description |
|---------|-------------|
| `,enable` | Enables Dictator Script automation. |
| `,disable` | Disables automation. |
| `,set {block(s)}` | Set blocks you want to mine. Separate multiple blocks with spaces. Example: `,set diamond_ore gold_ore`. |
| `,start` | Start mining the selected blocks. |
| `,stop` | Stops Baritone automation. |
| `,help` | Shows a help message with available commands. |

---

## Example Workflow

1. Enable the mod:

```text
,enable
```

2. Set the blocks you want to mine:

```text
,set diamond_ore iron_ore
```

3. Start mining:

```text
,start
```

4. Stop automation anytime:

```text
,stop
```

---

## Technical Details

- Written in **Java**, fully compatible with **Forge**.
- Uses **Minecraft Forge EventBus** to listen to chat, GUI, and tick events.
- Integrates with **Baritone** for efficient automated mining.
- Thread-safe routines for GUI interaction and automated actions.

---

## Contributing

Contributions are welcome! Feel free to submit bug reports, feature requests, or pull requests.  

---

## License

This project is for educational purposes and personal use. Redistribution or use on public servers without permission is **not allowed**.


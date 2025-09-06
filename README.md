# Dictator Script

**Dictator Script** is a client-side automation tool for Minecraft. It allows you to automate block mining, inventory management, and routine tasks with ease. This mod is designed for **single-player or private server use** where automation is allowed.  

> ⚠️ **Warning:** Using automation on public servers may violate server rules. Use responsibly.

---

## Features

- Toggle automation on and off with simple commands.
- Set target blocks to mine automatically.
- Integrates with Baritone for pathfinding and mining.
- Automatically equips tools, handles inventory, and manages movement.
- Can automatically retrieve kits from chests and manage shulker boxes.
- Customizable mining routines with easy-to-use commands.

---

## Installation

1. Ensure you have **Minecraft Forge** installed for your version.
2. Place the compiled `.jar` file into your `mods` folder.
3. Launch Minecraft with the Forge profile.
4. Join your world or server (private world recommended).

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


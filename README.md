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

## Example

1. Enable the mod:

```text
,enable (Enables mod and runs all the necessary Baritone commands)
```

2. Set the blocks you want to mine:

```text
,set diamond_ore iron_ore (Tells Baritone what to mine & look for)
```

3. Start script:

```text
/kill (Script automatically runs onDeath)
```

4. Stop automation anytime:

```text
,stop (Stops only Baritone, does not affect script)
```

5. Resume automation anytime:

```text
,start (Resumes only Baritone, does not affect script)
```

---

## FAQ

**Q: What if I want to completely stop the script from running when I die?**

A: Use ,disable to stop the mod.

**Q: How do I use ,set to mine more than 1 block?**

A: ,set supports spaces between arguments to allow multiple blocks. `,set dirt cobblestone`

**Q: What if I spawn at Y 256 and it can't find an Ender Chest?**

A: After 1 minute, if it can't find an Ender Chest, it will run /kill to respawn elsewhere.

**Q: Can I change the prefix or colour of Shulker box that the script looks for?**

A: Not at the moment, but it is a feature I will add in future updates.

**Q: What if the server restarts and after using AutoReconnect the script is disabled?**

A: I will be adding an AutoResume function very soon to counteract this.

---

## Contributing

Contributions are welcome! Feel free to submit bug reports, feature requests, or pull requests.

---

## License

This project is free for personal use. Redistribution or use on public servers is permitted.


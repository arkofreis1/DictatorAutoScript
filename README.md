# Dictator Auto-Script

**Dictator Auto-Script** is a client-side automation tool for Dictator members on the Minecraft server 5b5t.org. It allows you to automate block mining using Baritone and automatically regears if killed via Baritone path finding to an Ender Chest.

---

## Requirements

- Baritone for 1.12.2.
- AutoArmor, AutoTotem, AutoRespawn, AutoReconnect, AutoEat.
- Black shulker boxes in the Ender Chest.

---

## Installation

1. Make sure you have **Minecraft Forge 1.12.2** installed.
2. Place the compiled `.jar` file into your `mods` folder via `%appdata%` -> `.minecraft`.
3. Launch Minecraft with the Forge profile selected.

---

## Commands

The default prefix is comma `,`:

| Command | Description |
|---------|-------------|
| `,enable` | Enables Dictator Auto-Script and Baritone arguments. |
| `,disable` | Disables Dictator Auto-Script, Baritone arguments and stops Baritone. |
| `,set {block(s)}` | Set blocks you want to mine. Separate multiple blocks with spaces. `,set dirt cobblestone`. |
| `,start` | Start mining the selected blocks if already alive with items. (Otherwise use `/kill` after `,set` to fully run the script.) |
| `,stop` | Stops mining and stops Baritone. |
| `,help` | Shows a help message with available commands. |
| `,auto` | Auto starts script process (enable, set, kill). |
| `,cls` | Clears chat, client-side only. |
| `,config / ,conf` | Show the configuration menu. |
| `,autoWhisper / ,aw` | Sends the configured message. |
| `,autoWhisper / ,aw set {string}` | Sets the configured message. |
| `,modPrefix / ,mp {prefix}` | Set prefix for mod. |
| `,shulkerColor / ,sc {color}` | Set the color of Shulker that the mod looks for. |
| `,goToGround / ,gtg {bool}` | Choose whether to go to Y=5 before mining. |
| `,ignoreStackedShulkers / ,iss {bool}` | Choose whether to ignore stacked shulkers (true is default). |

---

## Example

1. Enable the mod:

```text
,enable (Enables mod and runs all the necessary Baritone commands)
```

2. Set the blocks you want to mine:

```text
,set dirt cobblestone (Tells Baritone what to mine & look for)
```

3. Start script:

```text
/kill (Script automatically runs onDeath)
```

4. Stop Baritone:

```text
,stop (Stops Baritone. Recommended if you want to change the selected blocks.)
```

5. Resume Baritone:

```text
,start (Resumes Baritone. Recommended if you already are geared up.)
```

---

## FAQ

**Q: What if I want to completely stop the script from running when I die?**

A: Use ,disable to completely stop the mod.


**Q: How do I use `,set` to mine more than 1 block?**

A: It supports spaces between arguments to allow multiple blocks. `,set dirt cobblestone`


**Q: What if I spawn at `Y 256` and it can't find an Ender Chest?**

A: After 1 minute, if it can't find an Ender Chest, it will run /kill to respawn elsewhere.


**Q: Can I change the prefix or colour of shulker box that the script looks for?**

A: Not at the moment, but it is a feature I will add in future updates.


**Q: What if the server restarts and after using AutoReconnect the script is disabled?**

A: I will be adding an AutoResume function very soon to counteract this.


**Q: Can I pause Baritone `,stop` and change the selected blocks `,set` and then resume `,start`?**

A: Yes! That is the purpose for the `,stop` and `,start` function. Not recommended if you have no items.


**Q: What if the black shulker boxes are stacked?**

A: Currently it doesn't support stacked shulker boxes but it will be implemented in the future.

---

## Contributing

Contributions are welcome! Feel free to submit bug reports, feature requests, or pull requests.

Message me on Discord: `arkofreis_` Or join: https://discord.gg/5FmVE4nbax/

---

## License

This project is free for personal use. Redistribution or use on public servers is permitted.


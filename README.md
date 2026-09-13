## Notes

Thanks to [Cairn4](https://cairn4.com/) for creating **MewnBase**. Explore an alien planet as a space cat, gather resources and build a base to survive.

Porter: **Pixelforge ports (Ronax)**.

This universal **MewnBase.zip** is a bring-your-own-data PortMaster package for compatible
**64-bit ARM Linux firmware**. Supply the owned game files below. PortMaster provides Java 17
and Westonpack. Device testing is requested for muOS on RG34XX SP and other RGXX models,
R36S with compatible firmware, and other ARM64 handhelds. A device name alone does not guarantee
a compatible 64-bit userspace or graphics driver.

## Get the game files

Use your owned Windows **MewnBase 1.0.1** installation or saved download from [Cairn4](https://cairn4.itch.io/mewnbase).
Extract the Windows ZIP, then copy `game/desktop-1.0.jar` and the **entire matching `data/` folder**
into the installed `mewnbase/` directory. Keep both folder names. Do not copy the Windows EXE,
Windows Java runtime, settings or personal saves. No conversion is required.

The current store download may be newer. Version **1.0.2 remains unverified**; no 1.0.2 installation
was available for compatibility testing. To test your owned 1.0.2, use a separate installation,
replace both `game/` and `data/` with matching files and create an empty
`mewnbase/allow-unverified-1.0.2.txt`. The host checks the version and basic dependencies,
but this is not a promise that 1.0.2 will run. Saves are separated by version.

Supported 1.0.1 archive SHA-256 (`game/desktop-1.0.jar`):

```text
9452d5588c457995599a3fb95ef6d570fa4aa2f3d367bd0dbe2d63a303272b6c
```

Compare it with `Get-FileHash -Algorithm SHA256 "game/desktop-1.0.jar"` in PowerShell,
or `sha256sum "game/desktop-1.0.jar"` on Linux. A different build needs a compatibility check.

## Installation

1. Update PortMaster. Put **MewnBase.zip** in PortMaster's `autoinstall/` directory, then open PortMaster to install it. Connect to the network to download Java 17 and Westonpack if they are not installed yet.
2. Copy the owned file to **`<ports directory>/mewnbase/game/desktop-1.0.jar`**, and the complete matching data folder to `<ports directory>/mewnbase/data/`.
3. Launch **MewnBase** from your firmware's ports menu.

For manual installation on **muOS**, extract the ZIP on your computer and copy `MewnBase.sh`
to `<SD card>/roms/PORTS/`, and the `mewnbase/` folder to `<SD card>/ports/` on the card
configured as the firmware's ports location. The required file is
**`<SD card>/ports/mewnbase/game/desktop-1.0.jar`**.

For **ArkOS/dArkOS and standard ports layouts**, extract the ZIP into your configured
ports directory (for example `/roms/ports/` or `/roms2/ports/`) so `MewnBase.sh` and
`mewnbase/` are beside each other. Use the firmware's configured ports location; the launcher
uses PortMaster's `directory` value. Avoid creating an extra `MewnBase/` wrapper folder.

## Controls

| Control | Action |
|---|---|
| D-pad | Move mouse cursor, including on devices without sticks |
| Left stick | Walk (W/A/S/D), when present |
| Right stick | Move mouse cursor, when present |
| A | Left mouse button; hold to drag or use tools |
| B | Right mouse button |
| X | Inventory / research (Tab) |
| Y | Map (M) |
| Start | Pause / options / back (Escape) |
| Select | Hold for alternate keyboard controls |
| Select + Y / A / X / B | Walk up / down / left / right (W/S/A/D) |
| L1 | Hold for slower mouse movement |
| R1 | Rotate tile (R) |
| L2 | Enter / vehicle interaction |
| R2 | Space / vehicle drift |
| Select + L1 | Begin text entry, then release both buttons |
| Select + R1 | Flashlight (F) |
| Select + L2 | Drop item (Q) |
| Select + R2 | Shift + Q |
| Select + D-pad up / down | Scroll mouse wheel up / down |
| Select + Start | Exit; save through the game first |

On RG34XX SP and other devices without sticks, use the D-pad for the cursor and
hold Select with the face buttons to walk. There is no host cursor/movement toggle.
gptokeyb2 supplies real keyboard, mouse-button and pointer events. Use the cursor to click slots and drag scrollbars.

Click a text field, press Select + L1, then release both buttons. During text entry, D-pad up/down chooses a letter, right advances, left deletes,
A adds a letter; B deletes. Start confirms and leaves text entry. Select cancels. Focus a game text field before starting.

## Controller support

All input is supplied through PortMaster's gptokeyb2 and the shipped `.ini` mapping.
Update PortMaster before installing. Native Xbox 360 emulation is not enabled in this
host: its native controller path is disabled. The mapper's `-x` mode replaces keyboard
and mouse output and requires a working native controller backend in the game.
Do not add `-x` to this launcher; it would bypass the controls listed above.

## Display

The display helper accepts 640x480, 720x480, 720x720, 1024x768 and 1280x720, and other
valid dimensions supplied by PortMaster. The host preserves the game view's aspect ratio;
black borders may appear. This includes RG35XX/RG40XX/R36S, RG34XX/SP, CubeXX, TrimUI Brick
and Smart Pro display shapes when their firmware and hardware meet the runtime requirements.

If automatic detection is incorrect, create `mewnbase/resolution.txt` containing the actual
size, for example `720x480`. Use `auto` or remove the file to restore automatic detection.

## Saves and troubleshooting

Back up **`mewnbase/userdata/<game-version>/`** before updates. Worlds are in its `saves/` subfolder.
Read **`mewnbase/log.txt`** if startup fails. Report your device, exact firmware version,
resolution and steps to reproduce, and attach the log. Test menu navigation, gameplay,
audio, game speed, save/reload, suspend/resume and clean exit. Keep purchased game files private.

## Licenses

The original port and host use the MIT license; their separate notices and the gptokeyb2
GPL license are in `mewnbase/licenses/`. Upstream copyright notices remain intact.
The game and screenshot retain Cairn4's rights. Java, Westonpack and the mapper are installed separately by PortMaster.

## Build the PortMaster package

Requires Python 3.9+ and JDK 17 or newer. **No purchased JAR or DAT is required to compile
the host or build the ZIP.** From this source directory, on Windows:

```bat
python tools/build.py --jdk "C:\Program Files\Java\jdk-17"
```

Replace the quoted path with your installed JDK directory, for example `jdk-26.0.2.1`.
Use double quotes in Windows Command Prompt. On Linux:

```sh
python3 tools/build.py --jdk "/path/to/installed/jdk-17"
```

The first build downloads checksum-pinned public compile dependencies. Later builds may add
`--offline` to use the cache. Handwritten `compile-api/` declarations are compile-only;
only `org/portmaster/mewnbase/` host classes go into `mewnbase-host.jar`.


The only release artifact is **`dist/MewnBase.zip`**, a universal BYO-data ZIP.
The build also prepares **`ports/mewnbase/`** in the PortMaster source submission layout.
It never packages the owned game archive, MewnBase data, Windows runtimes or personal saves.
After editing package documentation or controls, rebuild with:

```sh
python tools/build.py --package-only
python tools/verify_package.py
```

`--package-only` requires a previously built host. The optional `--game-jar` argument checks
a supplied archive's fingerprint; it does not participate in compilation. Downloading a
public compile dependency does not supply the commercial game. Copy the owned files after installing.

Run `bash tests/verify_display.sh` for display-helper checks. Run `python tests/verify_launcher.py` for lifecycle checks. These tests use
mock runtimes and do not mount or run games. See `VALIDATION.md` for the recorded checks
and `testing_thread.txt` for the Discord testing post. Upload source files using Git;
`build/`, `dist/`, generated `ports/` and owned data are excluded by `.gitignore`.

The Discord draft stays in source `testing_thread.txt`; it is not installed by the ZIP.

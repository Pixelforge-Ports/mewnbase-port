#!/bin/bash
XDG_DATA_HOME=${XDG_DATA_HOME:-$HOME/.local/share}

if [ -d "/opt/system/Tools/PortMaster/" ]; then
  controlfolder="/opt/system/Tools/PortMaster"
elif [ -d "/opt/tools/PortMaster/" ]; then
  controlfolder="/opt/tools/PortMaster"
elif [ -d "$XDG_DATA_HOME/PortMaster/" ]; then
  controlfolder="$XDG_DATA_HOME/PortMaster"
else
  controlfolder="/roms/ports/PortMaster"
fi

source "$controlfolder/control.txt"
[ -f "$controlfolder/mod_${CFW_NAME}.txt" ] && source "$controlfolder/mod_${CFW_NAME}.txt"
get_controls

GAMEDIR="/${directory#/}/ports/mewnbase"
java_runtime="zulu17.54.21-ca-jre17.0.13-linux"
jar_filename="game/desktop-1.0.jar"

cd "$GAMEDIR" || { pm_message "MewnBase: game folder missing."; pm_finish; exit 1; }
exec > >(tee "$GAMEDIR/log.txt") 2>&1

SAVEDIR="$GAMEDIR/saves/"
CACHEDIR="$GAMEDIR/cache/"

weston_dir=/tmp/weston
export JAVA_HOME="/tmp/javaruntime/"
weston_mounted=0
java_mounted=0
weston_started=0
mapper_pid=""

cleanup() {
  if [ -n "$mapper_pid" ]; then
    kill "$mapper_pid" 2>/dev/null
    wait "$mapper_pid" 2>/dev/null
    mapper_pid=""
  fi
  if [ "$weston_started" = 1 ]; then
    $ESUDO "$weston_dir/westonwrap.sh" cleanup
  fi

  if [ "$PM_CAN_MOUNT" != N ]; then
    if [ "$java_mounted" = 1 ]; then
      $ESUDO umount "$JAVA_HOME"
    fi
    if [ "$weston_mounted" = 1 ]; then
      $ESUDO umount "$weston_dir"
    fi
  fi
  pm_finish
}

fail() {
  pm_message "MewnBase: $* See mewnbase/log.txt."
  sleep 5
  cleanup
  exit 1
}
$ESUDO mkdir -p "$SAVEDIR" "$CACHEDIR" || fail "Cannot create save folders."
[ "$DEVICE_ARCH" = aarch64 ] || fail "64-bit ARM firmware is required."
[ "$(getconf LONG_BIT)" = 64 ] || fail "64-bit userland is required."
[ -f "$GAMEDIR/$jar_filename" ] || fail "Copy your owned game/desktop-1.0.jar to mewnbase/game/desktop-1.0.jar."
[ -n "$GPTOKEYB2" ] || fail "Update PortMaster for controller support."

$ESUDO mkdir -p "${weston_dir}" || fail "Cannot create Weston directory."
weston_runtime="weston_pkg_0.2"
if [ ! -f "$controlfolder/libs/${weston_runtime}.squashfs" ]; then
  if [ ! -f "$controlfolder/harbourmaster" ]; then
    fail "This port requires the latest PortMaster to run, please go to https://portmaster.games/ for more info."
  fi
  $ESUDO "$controlfolder/harbourmaster" --quiet --no-check runtime_check "${weston_runtime}.squashfs" || fail "Cannot download Weston."
fi
if [[ "$PM_CAN_MOUNT" != "N" ]]; then
    $ESUDO umount "${weston_dir}" 2>/dev/null || true
fi
$ESUDO mount "$controlfolder/libs/${weston_runtime}.squashfs" "$weston_dir" \
  || fail "Cannot mount Weston."
weston_mounted=1

$ESUDO mkdir -p "${JAVA_HOME}" || fail "Cannot create Java directory."
if [ ! -f "$controlfolder/libs/${java_runtime}.squashfs" ]; then
  if [ ! -f "$controlfolder/harbourmaster" ]; then
    fail "This port requires the latest PortMaster to run, please go to https://portmaster.games/ for more info."
  fi
  $ESUDO "$controlfolder/harbourmaster" --quiet --no-check runtime_check "${java_runtime}.squashfs" || fail "Cannot download Java."
fi
if [[ "$PM_CAN_MOUNT" != "N" ]]; then
    $ESUDO umount "${JAVA_HOME}" 2>/dev/null || true
fi
$ESUDO mount "$controlfolder/libs/${java_runtime}.squashfs" "$JAVA_HOME" \
  || fail "Cannot mount Java."
java_mounted=1
export PATH="$JAVA_HOME/bin:$PATH"

cd "$GAMEDIR" || fail "Cannot open the game directory."

"$JAVA_HOME/bin/java" -Xmx64m -cp runtime/mewnbase-host.jar org.portmaster.mewnbase.VerifyGame "$GAMEDIR" || fail "Game file validation failed. Read the specific error above; copy the matching game/ and complete data/ folders."
game_version=$(cat "$GAMEDIR/game-version.txt")
case "$game_version" in 1.0.1|1.0.2) ;; *) fail "Invalid game version." ;; esac
SAVEDIR="$GAMEDIR/userdata/$game_version"
CACHEDIR="$SAVEDIR/cache"
mkdir -p "$SAVEDIR/saves" "$CACHEDIR" || fail "Cannot create versioned saves."
cd "$SAVEDIR" || fail "Cannot open saves directory."
source "$GAMEDIR/display.inc" || fail "Display helper missing."
mewnbase_display_setup || fail "Use auto or WIDTHxHEIGHT in resolution.txt."
printf 'Firmware: %s; display: %s\n' "$CFW_NAME" "$mewnbase_display_description"
export SDL_GAMECONTROLLERCONFIG="$sdl_controllerconfig"
export HOTKEY=back
export SDL_TOUCH_MOUSE_EVENTS=0
$GPTOKEYB2 java -c "$GAMEDIR/mewnbase.ini" &
mapper_pid=$!
pm_platform_helper "$JAVA_HOME/bin/java"

weston_started=1
$ESUDO env "${display_env[@]}" "$weston_dir/westonwrap.sh" headless noop kiosk crusty_glx_gl4es \
  "PATH=$JAVA_HOME/bin:$PATH" "JAVA_HOME=$JAVA_HOME" "HOME=$SAVEDIR" \
  "XDG_DATA_HOME=$SAVEDIR" "XDG_CONFIG_HOME=$SAVEDIR/config" \
  "XDG_CACHE_HOME=$CACHEDIR" "WAYLAND_DISPLAY=" "SDL_TOUCH_MOUSE_EVENTS=0" \
  "$JAVA_HOME/bin/java" -Xms32m -Xmx512m -XX:+UseSerialGC \
  "-Duser.home=$SAVEDIR" "-Djava.io.tmpdir=$CACHEDIR" \
  "-Dmewnbase.root=$GAMEDIR" "-Dmewnbase.data=$GAMEDIR/data/" \
  -Dmewnbase.fullscreen=true "${display_java[@]}" \
  -cp "$GAMEDIR/runtime/mewnbase-host.jar:$GAMEDIR/$jar_filename" org.portmaster.mewnbase.Main

status=$?
cleanup
exit "$status"
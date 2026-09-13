#!/bin/bash
# SPDX-License-Identifier: MIT
# Sourced after PortMaster's device/firmware helpers. No device model allowlist.
# Outputs are consumed by the launcher after mewnbase_display_setup.
# shellcheck disable=SC2034
mewnbase_valid_dimensions() {
    [[ "$1" =~ ^[0-9]{3,4}$ && "$2" =~ ^[0-9]{3,4}$ ]] || return 1
    (( 10#$1 >= 160 && 10#$1 <= 8192 && 10#$2 >= 160 && 10#$2 <= 8192 ))
}

mewnbase_display_setup() {
    local requested="${MEWNBASE_RESOLUTION:-}" width height
    display_env=()
    display_java=()
    mewnbase_display_description="automatic (Weston display mode)"
    if [[ -z "$requested" && -f "$GAMEDIR/resolution.txt" ]]; then
        IFS= read -r requested < "$GAMEDIR/resolution.txt" || true
        requested="${requested%$'\r'}"
    fi
    if [[ -n "$requested" && "$requested" != auto ]]; then
        [[ "$requested" =~ ^([0-9]{3,4})x([0-9]{3,4})$ ]] || return 1
        width="${BASH_REMATCH[1]}" height="${BASH_REMATCH[2]}"
        mewnbase_valid_dimensions "$width" "$height" || return 1
        mewnbase_display_description="override"
    elif mewnbase_valid_dimensions "${DISPLAY_WIDTH:-}" "${DISPLAY_HEIGHT:-}"; then
        width="$DISPLAY_WIDTH" height="$DISPLAY_HEIGHT"
        mewnbase_display_description="PortMaster"
    else
        # Leave Weston in charge when firmware detection is unavailable.
        return 0
    fi
    width=$((10#$width)) height=$((10#$height))
    display_env=("WESTON_HEADLESS_WIDTH=$width" "WESTON_HEADLESS_HEIGHT=$height")
    display_java=("-Dmewnbase.width=$width" "-Dmewnbase.height=$height")
    mewnbase_display_description="$width x $height ($mewnbase_display_description)"
}

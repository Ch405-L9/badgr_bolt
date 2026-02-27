#!/bin/bash
# OrbReaderRSVP – ADB & Emulator diagnostic fix script
# Run from any directory: bash fix_adb_emulator.sh

set -e

echo "=== 1. Kill hung Gradle daemons (fixes 44-min build) ==="
pkill -f "GradleDaemon" 2>/dev/null || true
# If gradle is on PATH:
gradle --stop 2>/dev/null || true
echo "Gradle daemons stopped."

echo ""
echo "=== 2. Check ADB ==="
# Android Studio bundles ADB – find it
STUDIO_ADB=$(find /usr /opt ~/android-studio ~/.local -name "adb" 2>/dev/null | head -1)
SDK_ADB="$HOME/Android/Sdk/platform-tools/adb"

if [ -f "$SDK_ADB" ]; then
    ADB="$SDK_ADB"
elif [ -n "$STUDIO_ADB" ]; then
    ADB="$STUDIO_ADB"
else
    echo "ADB not found in standard locations."
    echo "Install platform-tools via Android Studio SDK Manager:"
    echo "  SDK Manager → SDK Tools → Android SDK Platform-Tools  ✓"
    exit 1
fi

echo "Using ADB at: $ADB"
$ADB version
$ADB kill-server
sleep 1
$ADB start-server
echo "ADB restarted."

echo ""
echo "=== 3. Check KVM (required for x86/x86_64 emulators on Linux) ==="
if ls /dev/kvm &>/dev/null; then
    echo "✅ /dev/kvm exists"
    # Check current user has access
    if [ -r /dev/kvm ] && [ -w /dev/kvm ]; then
        echo "✅ Current user has read/write access to /dev/kvm"
    else
        echo "⚠️  /dev/kvm exists but user lacks access. Fixing..."
        sudo usermod -aG kvm "$USER"
        sudo chmod 666 /dev/kvm
        echo "Added $USER to kvm group. LOG OUT AND LOG BACK IN for this to take effect."
    fi
else
    echo "❌ /dev/kvm not found – KVM not enabled."
    echo "   On Ubuntu/Debian:  sudo apt install qemu-kvm libvirt-daemon-system"
    echo "   Then check BIOS:   Intel VT-x or AMD-V must be enabled."
    echo "   Workaround:        Use an ARM64 system image AVD instead of x86_64"
fi

echo ""
echo "=== 4. List running + available AVDs ==="
EMULATOR=$(find "$HOME/Android/Sdk" -name "emulator" 2>/dev/null | head -1)
if [ -n "$EMULATOR" ]; then
    echo "Available AVDs:"
    $EMULATOR -list-avds
else
    echo "emulator binary not found – install via SDK Manager → SDK Tools → Android Emulator"
fi

echo ""
echo "=== 5. Check JAVA_HOME matches gradle.properties ==="
GRADLE_JAVA=$(grep "org.gradle.java.home" "$(dirname "$0")/gradle.properties" 2>/dev/null | cut -d= -f2)
echo "gradle.properties java.home : ${GRADLE_JAVA:-not set in project}"
echo "System JAVA_HOME            : ${JAVA_HOME:-not set}"
echo "java -version               : $(java -version 2>&1 | head -1)"

echo ""
echo "=== Done. Next steps ==="
echo "1. Delete org.gradle.java.home from ~/.gradle/gradle.properties if it also appears there."
echo "2. In Android Studio: File → Invalidate Caches → Invalidate and Restart"
echo "3. Then: Build → Clean Project, then Run."

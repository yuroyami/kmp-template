#!/usr/bin/env bash
# Runs every check of this project, one job after the other. CI runs the same script.
# `scripts/verify.sh` runs all that this machine can run. `scripts/verify.sh apple` runs only the iOS part.
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ "${1:-}" != apple ]]; then
  ./kotlin check kiteAndroid kiteIos
  ./kotlin build -p jvm -p android
  ./kotlin test -m shared -p jvm
  # R8 runs only on a release build. Without keystore.properties the APK is unsigned.
  ./kotlin build -m androidApp -p android -v release
fi

if [[ "$(uname)" == Darwin ]]; then
  ./kotlin test -m shared -p iosSimulatorArm64
  ./kotlin build -m iosApp -p iosSimulatorArm64
fi

#!/usr/bin/env bash
# Builds Subscription Manager as a standalone desktop app with its own bundled
# Java runtime, so it can be launched like any other app (no "java -jar" needed).
#
# Usage:
#   ./package-app.sh              # app folder you can run directly (default)
#   ./package-app.sh installer    # native installer: .msi/.exe (Windows), .dmg/.pkg (macOS), .deb/.rpm (Linux)
#
# Requires JDK 17+ (for jpackage) and Maven. Must be run on the OS you are packaging for.
set -euo pipefail
cd "$(dirname "$0")"

mvn -q -B clean package

TYPE="app-image"
if [[ "${1:-}" == "installer" ]]; then
  case "$(uname -s)" in
    Darwin) TYPE="dmg" ;;
    Linux) TYPE="deb" ;;
    MINGW*|MSYS*|CYGWIN*) TYPE="msi" ;;
  esac
fi

EXTRA=()
case "$(uname -s)" in
  Darwin) EXTRA+=(--mac-package-name "Subscription Manager") ;;
  MINGW*|MSYS*|CYGWIN*) EXTRA+=(--win-menu --win-shortcut --win-dir-chooser) ;;
  Linux) [[ "$TYPE" != "app-image" ]] && EXTRA+=(--linux-shortcut) ;;
esac

# Stage only the app jar so build leftovers don't end up inside the app.
rm -rf dist target/package-input
mkdir -p target/package-input
cp target/subscription-manager.jar target/package-input/

jpackage \
  --type "$TYPE" \
  --name "SubscriptionManager" \
  --app-version "1.0.0" \
  --description "Track your subscriptions and their total monthly cost" \
  --input target/package-input \
  --main-jar subscription-manager.jar \
  --main-class com.subscriptionmanager.App \
  --add-modules java.desktop \
  --dest dist \
  "${EXTRA[@]}"

echo "Done. Output is in: $(pwd)/dist"

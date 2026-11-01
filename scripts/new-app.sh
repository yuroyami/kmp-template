#!/usr/bin/env bash
# Renames this template to a new app, then deletes the template-only files. Run it once.
# Usage: scripts/new-app.sh "App Name" com.example.app [ios.bundle.id]
set -euo pipefail
cd "$(dirname "$0")/.."

name="${1:?Give the app name.}"
id="${2:?Give the application ID.}"
ios_id="${3:-$id.ios}"

[[ "$name" =~ ^[A-Za-z0-9][A-Za-z0-9\ ]*$ ]] || { echo "Use only letters, digits and spaces in the app name." >&2; exit 1; }
[[ "$id" =~ ^[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)+$ ]] || { echo "\"$id\" is not a valid application ID." >&2; exit 1; }
[[ "$ios_id" =~ ^[a-zA-Z][a-zA-Z0-9-]*(\.[a-zA-Z][a-zA-Z0-9-]*)+$ ]] || { echo "\"$ios_id\" is not a valid bundle ID." >&2; exit 1; }

files=(
  app-identity.module-template.yaml
  androidApp/module.yaml
  androidApp/src/AndroidManifest.xml
  iosApp/module.xcodeproj/project.pbxproj
  README.md
  CLAUDE.md
)

# The iOS ID comes first, because it contains the application ID token.
perl -pi -e "s/com\.example\.kmptemplate\.ios/$ios_id/g; s/com\.example\.kmptemplate/$id/g; s/KmpTemplate/$name/g" "${files[@]}"
perl -0pi -e 's/<!-- template:start -->.*?<!-- template:end -->\n+//s' README.md

rm -f .github/workflows/template.yml scripts/new-app.sh
echo "Renamed to $name. Run ./kotlin check kiteAndroid kiteIos next."

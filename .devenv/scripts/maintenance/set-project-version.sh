#!/usr/bin/env bash

# Copyright 2025 the Operaton contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

if [[ ! -f "mvnw" ]]; then
  echo "⚠️ Maven Wrapper not found. You must execute this script from the project root directory. Exiting..."
  exit 1
fi

if [[ -z "$1" ]]; then
  echo "⚠️ You must provide the new version as an argument. Exiting..."
  exit 1
fi
NEW_VERSION=$1
CURRENT_VERSION=$(./mvnw help:evaluate -N -q -Dexpression=project.version -DforceStdout |tail -n 1)

# Downstream change (see FORK-PATCHES.md): in-place edits use perl instead of `sed -i ''` (BSD only) so the script
# runs in Git Bash, WSL and macOS; versions are matched literally (\Q...\E), not as regex; no backup POMs; without
# npm only the root version of each package-lock.json is changed; the jreleaser and next-version steps were removed.
export CURRENT_VERSION NEW_VERSION

echo "🔍 Current project version is $CURRENT_VERSION, updating to $NEW_VERSION"

echo "🔄 Updating version in pom.xml files"

./mvnw versions:set -q -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false || exit 1

echo "🔄 Updating version in package.json files"
PACKAGE_JSON_FILES=($(find . -name package.json -not -path "*/node_modules/*" -not -path "*/target/*"))

for PACKAGE_JSON_FILE in "${PACKAGE_JSON_FILES[@]}"; do
  perl -pi -e 's/"version": "\Q$ENV{CURRENT_VERSION}\E"/"version": "$ENV{NEW_VERSION}"/' "$PACKAGE_JSON_FILE"
done

PACKAGE_LOCK_FILES=($(find . -name package-lock.json -not -path "*/node_modules/*" -not -path "*/target/*"))

for PACKAGE_LOCK_FILE in "${PACKAGE_LOCK_FILES[@]}"; do
  PACKAGE_LOCK_DIR=$(dirname "$PACKAGE_LOCK_FILE")
  if command -v npm > /dev/null; then
    echo "🔄 Regenerating $PACKAGE_LOCK_FILE via npm install --package-lock-only --ignore-scripts"
    (cd "$PACKAGE_LOCK_DIR" && npm install --package-lock-only --ignore-scripts)
  else
    echo "🔄 npm not found, updating only the root version in $PACKAGE_LOCK_FILE"
    # the file's top-level "version" and the one in packages[""]; dependency entries are left alone
    perl -0pi -e 's/\A(\{\s*"name": "[^"]*",\s*"version": ")\Q$ENV{CURRENT_VERSION}\E"/$1$ENV{NEW_VERSION}"/;
                  s/(""\s*:\s*\{\s*"name": "[^"]*",\s*"version": ")\Q$ENV{CURRENT_VERSION}\E"/$1$ENV{NEW_VERSION}"/' \
      "$PACKAGE_LOCK_FILE"
  fi
done

echo "🔄 Updating version in pom.xml files that are not part of the reactor"
POM_FILES=(\
./distro/run/core/pom.xml \
./distro/run/distro/pom.xml \
./distro/run/assembly/pom.xml \
./distro/run/pom.xml \
./distro/run/qa/example-plugin/pom.xml \
./distro/run/qa/runtime/pom.xml \
./distro/run/qa/integration-tests/pom.xml \
./distro/run/qa/pom.xml \
./distro/run/modules/webapps/pom.xml \
./distro/run/modules/example/pom.xml \
./distro/run/modules/oauth2/pom.xml \
./distro/run/modules/pom.xml \
./distro/run/modules/rest/pom.xml \
)

for POM_FILE in "${POM_FILES[@]}"; do
  perl -pi -e 's/<version>\Q$ENV{CURRENT_VERSION}\E<\/version>/<version>$ENV{NEW_VERSION}<\/version>/g' "$POM_FILE"
done

MISSED_FILES=$(grep -RF -e ">$CURRENT_VERSION<" -e "\"$CURRENT_VERSION\"" --include pom.xml --include package.json \
  --exclude-dir node_modules --exclude-dir target .)
if [[ -n "$MISSED_FILES" ]]; then
  echo "⚠️ The following files still contain the old version:"
  echo "$MISSED_FILES"
fi

echo "✅ Version updated to $NEW_VERSION"

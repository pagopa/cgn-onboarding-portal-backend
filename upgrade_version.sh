#!/bin/bash

# Check if a profile is provided as an argument
if [ -z "$1" ]; then
  echo "❌ Error: No profile specified."
  echo "Usage: $0 [increment-patch | increment-minor | increment-major]"
  exit 1
fi

PROFILE=$1

# Validate the provided profile
if [[ "$PROFILE" != "increment-patch" && "$PROFILE" != "increment-minor" && "$PROFILE" != "increment-major" ]]; then
  echo "❌ Error: Invalid profile."
  echo "Choose from: increment-patch, increment-minor, increment-major"
  exit 1
fi

echo "🔄 Updating version using profile: $PROFILE..."

# Get the new version based on the selected profile
NEW_VERSION=$(mvn validate help:evaluate -Dexpression=newVersion -q -DforceStdout -P "$PROFILE")

# Check if the version was retrieved successfully
if [ -z "$NEW_VERSION" ]; then
  echo "❌ Error: Unable to retrieve the new version."
  exit 1
fi

echo "✅ New calculated version: $NEW_VERSION"

# Execute the command to update the version
mvn versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false

# Check if the version update was successful
if [ $? -ne 0 ]; then
  echo "❌ Error while updating the version."
  exit 1
fi

echo "🚀 Running mvn clean install..."
mvn clean install -DskipTests

# Check if the build was successful
if [ $? -ne 0 ]; then
  echo "❌ Error during mvn clean install."
  exit 1
fi

echo "🎉 Build completed successfully!"
exit 0

#!/bin/bash

# Appspiriment Project Setup Script
# This script renames the project and package name in a newly cloned template.

set -e

echo "----------------------------------------------------"
echo "🚀 Appspiriment Project Setup"
echo "----------------------------------------------------"

# Detect OS for sed compatibility
case "$(uname)" in
    Darwin*)  SED_I=(sed -i '') ;;
    *)        SED_I=(sed -i) ;;
esac

# 1. Get Input
read -p "Enter Project Name (e.g. MySuperApp): " APP_NAME
if [ -z "$APP_NAME" ]; then
    echo "❌ Project Name cannot be empty."
    exit 1
fi

read -p "Enter Package Name (e.g. com.company.myapp): " NEW_PACKAGE
if [ -z "$NEW_PACKAGE" ]; then
    echo "❌ Package Name cannot be empty."
    exit 1
fi

OLD_PACKAGE="com.example.app" # The default in your template
OLD_PATH=${OLD_PACKAGE//./\/}
NEW_PATH=${NEW_PACKAGE//./\/}

echo ""
echo "Summary:"
echo "  Project Name: $APP_NAME"
echo "  Package Name: $NEW_PACKAGE"
echo "  Target Path:  app/src/main/java/$NEW_PATH"
echo "----------------------------------------------------"
read -p "Proceed? (y/n): " confirm
if [[ $confirm != [yY] ]]; then
    exit 1
fi

# 2. Rename root project in settings.gradle.kts
echo "📝 Updating settings.gradle.kts..."
"${SED_I[@]}" "s/rootProject.name = \".*\"/rootProject.name = \"$APP_NAME\"/g" settings.gradle.kts

# 3. Update Package Name in all build.gradle.kts, .kt, and .xml files
echo "📝 Updating package names in files..."
find . -type f \( -name "*.kts" -o -name "*.kt" -o -name "*.xml" \) -not -path "*/build/*" -not -path "*/.gradle/*" | while read -r file; do
    "${SED_I[@]}" "s/$OLD_PACKAGE/$NEW_PACKAGE/g" "$file"
done

# 4. Physically move folders to match new package
echo "📂 Moving source folders..."
find . -path "*/src/*/java/$OLD_PATH" -type d | while read -r dir; do
    # Calculate the base directory (e.g., app/src/main/java/)
    BASE_DIR=$(echo "$dir" | sed "s|$OLD_PATH||")

    # Create the new directory structure
    mkdir -p "$BASE_DIR$NEW_PATH"

    # Move files if the directory is not empty
    if [ "$(ls -A "$dir")" ]; then
        mv "$dir"/* "$BASE_DIR$NEW_PATH/"
    fi

    # Clean up old empty directories (optional but recommended)
    # We move up from the old leaf node and delete if empty
    PARENT_DIR=$(dirname "$dir")
    while [[ "$PARENT_DIR" == *"$OLD_PACKAGE"* || "$PARENT_DIR" == *"com/example"* ]]; do
        if [ -d "$PARENT_DIR" ] && [ ! "$(ls -A "$PARENT_DIR")" ]; then
            rmdir "$PARENT_DIR"
            PARENT_DIR=$(dirname "$PARENT_DIR")
        else
            break
        fi
    done
done

echo ""
echo "✅ Setup Complete!"
echo "🗑️  Deleting this script..."
rm -- "$0"
echo "----------------------------------------------------"
echo "Open the project in Android Studio to begin."

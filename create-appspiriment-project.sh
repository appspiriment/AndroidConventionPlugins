#!/bin/bash

# Appspiriment Project Spawner
# Usage: ./create-appspiriment-project.sh

set -e

# Configuration
TEMPLATE_DIR="./project-template/root"
OLD_PACKAGE="com.example.app"
OLD_PATH="com/example/app"

echo "----------------------------------------------------"
echo "🌟 Appspiriment Project Spawner"
echo "----------------------------------------------------"

# 1. Collect Inputs
read -p "Enter Project Name (e.g. MyNewApp): " APP_NAME
read -p "Enter Package Name (e.g. com.company.myapp): " NEW_PACKAGE
read -p "Enter GitHub Repo Name (default: $APP_NAME): " REPO_NAME
REPO_NAME=${REPO_NAME:-$APP_NAME}

DEST_DIR="../$APP_NAME"

if [ -d "$DEST_DIR" ]; then
    echo "❌ Error: Directory $DEST_DIR already exists."
    exit 1
fi

# 2. Copy Template
echo "📂 Copying template to $DEST_DIR..."
cp -r "$TEMPLATE_DIR" "$DEST_DIR"
rm -f "$DEST_DIR/setup.sh" # Remove the internal setup script

# 3. Configure Renaming
echo "📝 Configuring $APP_NAME..."
NEW_PATH=${NEW_PACKAGE//./\/}

# OS Detection for sed
case "$(uname)" in
    Darwin*)  SED_I=(sed -i '') ;;
    *)        SED_I=(sed -i) ;;
esac

pushd "$DEST_DIR" > /dev/null

# Update package names in files
find . -type f \( -name "*.kts" -o -name "*.kt" -o -name "*.xml" \) -not -path "*/build/*" | while read -r file; do
    "${SED_I[@]}" "s/$OLD_PACKAGE/$NEW_PACKAGE/g" "$file"
done

# Update project name in settings
"${SED_I[@]}" "s/rootProject.name = \".*\"/rootProject.name = \"$APP_NAME\"/g" settings.gradle.kts

# Move folders to match new package
echo "📂 Refactoring folder structure..."
mkdir -p "app/src/main/java/$NEW_PATH"
if [ -d "app/src/main/java/$OLD_PATH" ]; then
    mv "app/src/main/java/$OLD_PATH"/* "app/src/main/java/$NEW_PATH/"
    # Cleanup old empty dirs
    rm -rf "app/src/main/java/com/example"
fi

# 4. Git Initialization
echo "🗄️ Initializing Local Git..."
git init -b main
git add .
git commit -m "Initial commit from Appspiriment Template"

# 5. GitHub Creation
if command -v gh >/dev/null 2>&1; then
    echo "🌎 Creating GitHub Repository: $REPO_NAME..."
    # This creates the repo and pushes the main branch
    gh repo create "$REPO_NAME" --public --source=. --remote=origin --push
    echo "----------------------------------------------------"
    echo "✅ SUCCESS!"
    echo "🔗 URL: https://github.com/$(gh api user -q .login)/$REPO_NAME"
else
    echo "----------------------------------------------------"
    echo "✅ Project created locally at $DEST_DIR"
    echo "⚠️  GitHub CLI (gh) not found. Please create the repo manually."
fi

popd > /dev/null
echo "----------------------------------------------------"
echo "Run: cd $DEST_DIR && studio ."

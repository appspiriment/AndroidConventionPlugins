# Android Convention Plugins

Welcome to the **Android Convention Plugins** repository! These Gradle plugins are designed to simplify the configuration and setup of Android projects by providing out-of-the-box solutions for common requirements such as Dependency Injection, version management, and library setup.

## Plugins Overview

### 1. **Android Application Convention Plugin**
- **ID:** `io.github.appspiriment.application`
- **Description:** Configures Android application modules with Hilt Dependency Injection (DI) and automatic versioning. It updates the `version.properties` file with each build and integrates seamlessly with Hilt.
- **Features:**
  - Automatic Hilt DI setup.
  - Version management via `version.properties`.
  - **Warning:** Avoid manually modifying the `appspirimentlibs.versions.toml` file as it may be overwritten during updates.
- **Tags:** android, application, conventions

### 2. **Android Library Convention Plugin**
- **ID:** `io.github.appspiriment.library`
- **Description:** Configures Android Library modules with Hilt DI (via KSP) and optional Compose capabilities. The plugin updates the `appspirimentlibs.versions.toml` file to include required dependencies.
- **Features:**
  - Hilt DI support using KSP.
  - Optional Compose setup with `isComposeLibrary` extension.
  - Automatic dependency management.
- **Tags:** android, library, conventions

### 3. **Android Project Root Convention Plugin**
- **ID:** `io.github.appspiriment.project`
- **Description:** Simplifies initial project setup and version management. Automatically configures `appspirimentlibs.versions.toml` and adjusts the root `settings.gradle.kts` file.
- **Features:**
  - Clean project initialization.
  - Automatic migration to Kotlin DSL if using Groovy.
  - Updates plugin versions easily by syncing.
  - **Note:** Removes existing configurations in the app module's Gradle file.
- **Tags:** android, settings, conventions

### 4. **Android Room Convention Plugin**
- **ID:** `io.github.appspiriment.room`
- **Description:** Simplifies Room setup by applying necessary libraries and configurations. Fetches required Room versions from `appspirimentlibs.versions.toml`.
- **Features:**
  - Automatic Room integration.
  - Ensures compatibility with specified versions.
- **Tags:** android, room, conventions

## Getting Started

1. Add the desired plugin to your `build.gradle.kts` or `settings.gradle.kts` file. Ensure you specify the correct version.
2. Sync your project to apply the plugin.
3. Follow any specific configuration instructions provided by each plugin.

## Contributing

Contributions are welcome! Feel free to submit issues or pull requests to improve these plugins.

## License

This project is licensed under the [MIT License](LICENSE).

---
For more details, visit the [GitHub Repository](https://github.com/appspiriment/AndroidConventionPlugins).


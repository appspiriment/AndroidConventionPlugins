plugins {
    alias(appspirimentlibs.plugins.appspiriment.application)
}

android {
    namespace = "com.example.app"
    defaultConfig {
        applicationId = "com.example.app"
        versionCode = appspirimentlibs.versions.versionCode.get().toInt()
        versionName = appspirimentlibs.versions.versionName.get()
    }
}

// Optional: override defaults
// appspiriment {
//     enableUtils.set(true)          // default: true — adds appspiriment-utils + logutils
//     enableMinify.set(false)        // default: false
//     addDevSuffixToDebug.set(true)  // default: true — debug builds get:
//                                    //   applicationId: com.example.app.dev
//                                    //   versionName:   1.0.0.20260504.143022
// }

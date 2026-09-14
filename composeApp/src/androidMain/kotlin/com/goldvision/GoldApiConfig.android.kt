package com.goldvision

// يُقرأ من BuildConfig، المولَّد من قيمة GOLDAPI_KEY في local.properties
// (انظر composeApp/build.gradle.kts)
internal actual val goldApiKey: String = BuildConfig.GOLDAPI_KEY

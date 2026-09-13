import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // أهداف iOS مضافة مسبقاً حتى تكون جاهزة عند بدء العمل على تطبيق آيفون؛
    // لن تُبنى فعلياً إلا على جهاز Mac مزوّد بـ Xcode
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.ktor.client.okhttp)
            // تتبّع الأعطال (Sentry) — يُهيَّأ تلقائياً من AndroidManifest.xml
            // (وسم io.sentry.dsn)، بلا حاجة لأي كود تهيئة إضافي. لا يُرسل أي
            // بيانات شخصية افتراضياً (sendDefaultPii = false)، متّسق مع سياسة
            // الخصوصية المحلية للتطبيق
            implementation("io.sentry:sentry-android:8.56.0")
            // تحليلات استخدام مجهولة الهوية (PostHog) — تُهيَّأ من
            // GoldVisionApplication.kt، بلا أي تعريف شخصي (لا identify())
            implementation("com.posthog:posthog-android:3.64.0")
            // جدولة إشعار سعر الذهب اليومي (افتتاح/إغلاق) حتى عند إغلاق
            // التطبيق — PriceNotificationWorker.android.kt
            implementation("androidx.work:work-runtime-ktx:2.9.1")
            // تسجيل الدخول بالإيميل (Firebase Authentication) — مجاني بالكامل
            // على خطة Spark بلا حد أقصى لعدد المستخدمين. BoM يضبط كل إصدارات
            // مكتبات Firebase معاً حتى تبقى متوافقة
            implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
            implementation("com.google.firebase:firebase-auth")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            // شعار GOLD VISION الفعلي (صورة حقيقية) في composeResources/drawable
            implementation(compose.components.resources)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

compose.resources {
    packageOfResClass = "com.goldvision.resources"
}

android {
    namespace = "com.goldvision"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.goldvision"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/res")
    }

    // تصغير وتعمية الكود (R8) في نسخة الإصدار (release) فقط — لا يؤثر على
    // بناء التجربة العادي (debug) الذي تستخدمه الآن من Android Studio.
    // يجعل هندسة الكود العكسية أصعب بكثير (أسماء أصناف/دوال بلا معنى،
    // وحذف أي كود غير مستخدم)، وهذا معيار أمان قياسي في التطبيقات
    // الاحترافية (البنكية وغيرها)
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // مطلوب لأن kotlinx-datetime تعتمد على java.time، وهي متاحة أصلاً
        // من Android API 26 فقط؛ التفعيل هنا يجعلها تعمل ابتداءً من minSdk 24
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
}

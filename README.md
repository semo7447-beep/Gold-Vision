# Gold Vision

تطبيق تتبع أسعار الذهب والزكاة، مبني بـ Kotlin Multiplatform + Compose
Multiplatform حتى يعمل على أندرويد الآن، ويمكن إضافة نسخة آيفون لاحقاً من
نفس الكود دون إعادة كتابته.

## هيكل المشروع

```
composeApp/
  src/
    commonMain/   ← كل واجهات وشاشات التطبيق (مشتركة بين أندرويد و iOS)
    androidMain/  ← نقطة الدخول الخاصة بأندرويد (MainActivity)
    iosMain/      ← نقطة الدخول الخاصة بـ iOS (MainViewController) — جاهزة
                    لكن لم تُربط بعد بمشروع Xcode
```

كل الشاشات، الحاسبة، الرسوم البيانية، شاشة الزكاة، المحفظة... إلخ موجودة في
`composeApp/src/commonMain/kotlin/com/goldvision/App.kt`، وهي الملف الذي عدّل
عليه أي تعديل يظهر أثره على أندرويد و iOS معاً.

## تشغيله الآن على أندرويد

1. افتح مجلد المشروع في **Android Studio** (نسخة Ladybug أو أحدث تدعم KMP).
2. اتركه يزامن (Sync) المشروع — سيقوم بتنزيل AGP و Kotlin و Compose
   Multiplatform تلقائياً (يحتاج اتصال إنترنت عادي، هذه الخطوة لا يمكن
   إتمامها من هذه البيئة السحابية لأنها لا تملك وصولاً لخوادم Google Maven).
3. اختر جهاز/محاكي أندرويد ثم اضغط ▶ Run لتشغيل تهيئة `composeApp`.

بديل بدون Android Studio (من الطرفية، بعد تركيب Android SDK محلياً):
```
./gradlew :composeApp:installDebug
```

## ملاحظة مهمة حول هذه الجلسة

بيئة التنفيذ السحابية الحالية لا تملك Android SDK ولا اتصالاً بخوادم
`dl.google.com` (تحجبها سياسة الشبكة)، لذلك لم يكن ممكناً بناء أو تشغيل
التطبيق فعلياً هنا. تم التأكد يدوياً من:
- خلو كل كود `commonMain` من أي استدعاء خاص بأندرويد فقط (`android.*`,
  `java.util.Calendar`, `java.text.SimpleDateFormat`, `String.format`)
  حتى يبقى قابلاً للتصريف على iOS مستقبلاً.
- توازن الأقواس/الحاضنات في الملف الرئيسي بعد كل التعديلات.
- صحة إصدارات Gradle/AGP/Kotlin/Compose Multiplatform المستخدمة عبر
  فهرس Maven Central.

**الخطوة التالية المطلوبة منك**: افتح المشروع في Android Studio على جهازك
وشغّله، وأخبرني بأي خطأ بناء يظهر (إن ظهر) لتصحيحه فوراً.

## إضافة نسخة آيفون لاحقاً

الكود المشترك (`commonMain`) جاهز بالفعل لإضافة هدف iOS: تم تجهيز
`iosMain/MainViewController.kt` وأهداف `iosX64/iosArm64/iosSimulatorArm64`
في `composeApp/build.gradle.kts`. يتبقى فقط:
1. إنشاء مشروع Xcode بسيط (`iosApp/`) يستدعي `MainViewController()` —
   يُفضَّل توليده من داخل Xcode نفسه على جهاز Mac (Android Studio لا يولّد
   ملفات `.xcodeproj` من بيئة لينكس).
2. بناؤه وتشغيله على محاكي آيفون من Xcode.

هذه الخطوة تحتاج جهاز Mac ولا يمكن إنجازها من هذه البيئة السحابية.

## الإصدارات المستخدمة

| المكوّن | الإصدار |
|---|---|
| Kotlin | 2.1.20 |
| Compose Multiplatform | 1.7.3 |
| Android Gradle Plugin | 8.7.2 |
| kotlinx-datetime | 0.6.1 |
| compileSdk / targetSdk | 35 |
| minSdk | 24 |

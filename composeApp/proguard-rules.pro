# قواعد إضافية لتصغير/تعمية الكود (R8) في نسخة الإصدار — تحافظ على عمل
# المكتبات الحسّاسة للتعمية دون كسرها، بينما تترك كل شيء آخر يُعمَّى
# ويُحذف بحرية لتقليل حجم الملف وصعوبة الهندسة العكسية

# ---- kotlinx.serialization: بدون هذه القواعد قد يتعطل حفظ/تحميل بيانات
# المحفظة والملف الشخصي وإعدادات الإشعارات صامتاً بعد التعمية، لأن الكود
# المولَّد وقت الترجمة (serializer الخاص بكل صنف @Serializable) قد يُحذف
# أو يُعاد تسميته ظناً منه أنه غير مستخدم ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.goldvision.**$$serializer { *; }
-keepclassmembers class com.goldvision.** {
    *** Companion;
}
-keepclasseswithmembers class com.goldvision.** {
    kotlinx.serialization.KSerializer serializer(...);
}

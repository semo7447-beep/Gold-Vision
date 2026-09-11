package com.goldvision

// زر اختبار مؤقت للتأكد من ربط Sentry فعلياً — يُرسل عطلاً تجريبياً
// (بلا إغلاق التطبيق) ليظهر في لوحة Sentry خلال ثوانٍ. يُزال من الكود
// بعد التأكيد
expect fun sendTestCrashReport()

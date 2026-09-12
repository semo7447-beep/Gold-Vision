package com.goldvision

// يرسل رسالة تشخيصية صامتة لتتبّع الأعطال (Sentry) دون إظهار أي شيء
// للمستخدم — يساعد على تشخيص أعطال الشبكة (مثل فشل تحديث الأسعار) من
// بعيد بدل الاعتماد على لقطات شاشة فقط
expect fun reportSilentError(message: String)

package com.goldvision

import android.app.Application
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig

// تحليلات استخدام مجهولة الهوية بالكامل: لا نستدعي identify() في أي
// مكان بالتطبيق، فيبقى كل مستخدم معرَّفاً فقط بمعرّف عشوائي (UUID) يولّده
// SDK محلياً على الجهاز — لا اسم ولا بريد ولا أي معرّف شخصي يُرسل معه
class GoldVisionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppStorage.init(this)

        val config = PostHogAndroidConfig(
            apiKey = "phc_s4FaeH5VD87obtNSE5SzFc28TqFx3zn2m5BmFx6c8uWq",
            host = "https://us.i.posthog.com"
        )
        PostHogAndroid.setup(this, config)
    }
}

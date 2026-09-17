package com.goldvision

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal actual object NetworkMonitor {
    private var online by mutableStateOf(true)
    actual val isOnline: Boolean get() = online

    // يُعلَّم/يُمسَح هنا مباشرة (بلا انتظار LaunchedEffect داخل شجرة Compose
    // بـ App.kt)، حتى تتحدث نقطة "مباشر/غير محدث" بويدجتات الشاشة الرئيسية
    // فوراً أيضاً — الويدجتات ليست Composable وتعمل حتى لو لم تكن واجهة
    // التطبيق مفتوحة فعلياً حالياً، فتعتمد على هذا المسار المباشر لا على
    // LaunchedEffect (الذي يبقى مسؤولاً فقط عن إعادة التحديث الفعلية من
    // الشبكة بعد عودة الاتصال، غير مكرَّر هنا لتفادي طلب شبكي مزدوج)
    private fun markConnectivity(isOnline: Boolean) {
        // onCapabilitiesChanged قد يتكرر كثيراً بلا تغيّر فعلي بحالة
        // الاتصال (تغيّر قوة الإشارة مثلاً) — نتجاهل الاستدعاءات المكرَّرة
        // حتى لا تُعاد كتابة كل الويدجتات بلا داعٍ في كل مرة
        if (online == isOnline) return
        online = isOnline
        if (isOnline) {
            GoldMarket.clearOfflineMark()
            GoldHistory.clearOfflineMark()
            GoldNews.clearOfflineMark()
        } else {
            GoldMarket.markOffline()
            GoldHistory.markOffline()
            GoldNews.markOffline()
        }
        refreshWidgetsOnConnectivityChange()
    }

    fun init(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return

        // تهيئة فورية بالحالة الحالية عند بدء التطبيق، بدل افتراض "متصل"
        // حتى أول تغيّر فعلي بالشبكة
        val activeCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        online = activeCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                markConnectivity(true)
            }

            override fun onLost(network: Network) {
                markConnectivity(false)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                markConnectivity(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            }
        })
    }
}

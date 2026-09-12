package com.goldvision

import androidx.compose.runtime.Composable

// يعتمد على androidx.activity.compose.BackHandler، المبني على
// OnBackPressedDispatcher نفسه المستخدَم لزر الرجوع الفعلي/على الشاشة
// وإيماءة السحب من الحافة (predictive back) معاً — بلا حاجة لتمييزهما
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}

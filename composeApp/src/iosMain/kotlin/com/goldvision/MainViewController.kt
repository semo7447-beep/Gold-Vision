package com.goldvision

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

// نقطة الدخول التي سيستدعيها مشروع Xcode لاحقاً عند إضافة تطبيق iOS
// (عبر SwiftUI: ComposeView() { MainViewController() })
fun MainViewController(): UIViewController = ComposeUIViewController { App() }

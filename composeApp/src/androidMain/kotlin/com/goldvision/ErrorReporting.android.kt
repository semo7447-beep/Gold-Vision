package com.goldvision

import io.sentry.Sentry

actual fun reportSilentError(message: String) {
    Sentry.captureMessage(message)
}

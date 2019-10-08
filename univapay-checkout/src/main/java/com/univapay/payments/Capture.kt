package com.univapay.payments

import com.univapay.models.CaptureSettings

interface Capture {

    val captureSettings: CaptureSettings?
}

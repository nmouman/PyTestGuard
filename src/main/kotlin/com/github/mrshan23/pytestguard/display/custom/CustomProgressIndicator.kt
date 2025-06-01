package com.github.mrshan23.pytestguard.display.custom

import com.intellij.openapi.progress.ProgressIndicator

class CustomProgressIndicator(private val indicator: ProgressIndicator) {
    fun setText(text: String) {
        indicator.text = text
    }

    fun isCanceled(): Boolean = indicator.isCanceled

    fun isRunning(): Boolean = indicator.isRunning

    fun stop() {
        indicator.stop()
    }

}
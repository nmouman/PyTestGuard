package com.github.mrshan23.pytestguard.settings

data class PluginSettingsState(
    var apiKey: String = "",
    var enableSimpleMode : Boolean = false,
    var enableCoverageChangeMode: Boolean = false,
    var testSuitePath: String = ""
)

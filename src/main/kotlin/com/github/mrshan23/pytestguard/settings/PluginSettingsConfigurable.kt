package com.github.mrshan23.pytestguard.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class PluginSettingsConfigurable (val project: Project) : Configurable {

    private val settingsState: PluginSettingsState
        get() = project.service<PluginSettingsService>().state

    private var settingsComponent: PluginSettingsComponent? = null

    override fun createComponent(): JComponent? {
        settingsComponent = PluginSettingsComponent()
        return settingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        return settingsComponent!!.apiKey != settingsState.apiKey ||
                settingsComponent!!.enableSimpleModeCheckBoxSelected != settingsState.enableSimpleMode ||
                settingsComponent!!.enableCoverageChangeModeCheckBoxSelected != settingsState.enableCoverageChangeMode ||
                settingsComponent!!.testSuitePath != settingsState.testSuitePath
    }

    override fun apply() {
        settingsState.apiKey = settingsComponent!!.apiKey
        settingsState.enableSimpleMode = settingsComponent!!.enableSimpleModeCheckBoxSelected
        settingsState.enableCoverageChangeMode = settingsComponent!!.enableCoverageChangeModeCheckBoxSelected
        settingsState.testSuitePath = settingsComponent!!.testSuitePath

        // If coverage change mode is enabled, validate the test suite path
        if (settingsComponent!!.enableCoverageChangeModeCheckBoxSelected
            && settingsComponent!!.testSuitePath.isEmpty()) {
            throw ConfigurationException("Test suite path cannot be empty")
        }
    }

    override fun reset() {
        settingsComponent!!.apiKey = settingsState.apiKey
        settingsComponent!!.enableSimpleModeCheckBoxSelected = settingsState.enableSimpleMode
        settingsComponent!!.enableCoverageChangeModeCheckBoxSelected = settingsState.enableCoverageChangeMode
        settingsComponent!!.testSuitePath = settingsState.testSuitePath
    }

    override fun getDisplayName(): String {
        return "PyTestGuard"
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }

}
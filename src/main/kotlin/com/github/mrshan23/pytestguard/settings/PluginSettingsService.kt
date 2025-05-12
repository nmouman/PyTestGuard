package com.github.mrshan23.pytestguard.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(name = "PluginSettingsState", storages = [Storage("ProjectSettings.xml")])
class PluginSettingsService : PersistentStateComponent<PluginSettingsState> {

    private var pluginSettingsState: PluginSettingsState = PluginSettingsState()

    override fun getState(): PluginSettingsState {
        return pluginSettingsState
    }

    override fun loadState(state: PluginSettingsState) {
        pluginSettingsState = state
    }

    companion object {
        fun service(project: Project) = project.getService(PluginSettingsService::class.java).state
    }
}
package com.puhovin.intellijplugin.twm.settingsmanager

import com.puhovin.intellijplugin.twm.model.ToolWindowPreference
import com.puhovin.intellijplugin.twm.model.ToolWindowPreferenceStore

abstract class AbstractSettingsManager : SettingsManager {

    protected var state = ToolWindowPreferenceStore()
    protected val defaultPreferences = mutableMapOf<String, ToolWindowPreference>()

    override fun getState(): ToolWindowPreferenceStore {
        return state
    }

    override fun loadState(state: ToolWindowPreferenceStore) {
        this.state = state
    }

    override fun getPreferences(): Map<String, ToolWindowPreference> {
        return state.getPreferences()
    }

    override fun setPreferences(preferences: Map<String, ToolWindowPreference?>) {
        state.setPreferences(preferences)
    }

    override fun getDefaultPreferences(): Map<String, ToolWindowPreference> {
        return defaultPreferences
    }

    override fun setDefaultPreferences(defaultPreferences: Map<String, ToolWindowPreference>) {
        this.defaultPreferences.putAll(defaultPreferences)
    }

    override fun getDefaultAvailabilityToolWindow(id: String?): ToolWindowPreference? {
        return defaultPreferences[id]
    }
}
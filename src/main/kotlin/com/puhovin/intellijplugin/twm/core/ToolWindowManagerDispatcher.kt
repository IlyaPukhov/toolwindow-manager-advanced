package com.puhovin.intellijplugin.twm.core

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindowManager
import com.puhovin.intellijplugin.twm.model.AvailabilityPreference.AVAILABLE
import com.puhovin.intellijplugin.twm.model.AvailabilityPreference.UNAFFECTED
import com.puhovin.intellijplugin.twm.model.AvailabilityPreference.UNAVAILABLE
import com.puhovin.intellijplugin.twm.model.SettingsMode
import com.puhovin.intellijplugin.twm.model.ToolWindowManagerSettings
import com.puhovin.intellijplugin.twm.model.ToolWindowPreference
import com.puhovin.intellijplugin.twm.settingsmanager.GlobalToolWindowManagerService
import com.puhovin.intellijplugin.twm.settingsmanager.ProjectToolWindowManagerService
import com.puhovin.intellijplugin.twm.settingsmanager.SettingsManager
import com.puhovin.intellijplugin.twm.ui.PreferredAvailabilitiesView
import com.puhovin.intellijplugin.twm.ui.PreferredAvailabilitiesViewHolder
import java.util.EnumMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * The ToolWindowManagerDispatcher is responsible for managing and dispatching the preferences of tool windows
 * within a given project. It handles the initialization, application, and resetting of tool window preferences.
 * It provides functionality for switching between global and project-level settings for tool windows, applying
 * changes to the preferences, and ensuring that preferences are correctly reflected when the project is opened.
 *
 * <p>This class uses a lock mechanism to ensure thread safety when applying or resetting preferences, ensuring that
 * the tool window configurations are not modified concurrently in a multi-threaded environment.</p>
 *
 * @see ToolWindowPreference
 * @see SettingsManager
 * @see GlobalToolWindowManagerService
 * @see ProjectToolWindowManagerService
 * @see PreferredAvailabilitiesView
 */
class ToolWindowManagerDispatcher(private val project: Project) {
    private val lock: Lock = ReentrantLock()
    private val settingsManagerMap: MutableMap<SettingsMode, SettingsManager> = EnumMap(SettingsMode::class.java)
    private var _settingsMode: SettingsMode = DEFAULT_SETTINGS_MODE

    val settingsMode: SettingsMode
        get() = _settingsMode

    init {
        initializeSettingsManagerMap()
        loadSettingsMode()
    }

    companion object {
        private val KEY = Key.create<ToolWindowManagerDispatcher>("ToolWindowManagerDispatcher")
        private val DEFAULT_SETTINGS_MODE = SettingsMode.GLOBAL

        /**
         * Returns the singleton instance of ToolWindowManagerDispatcher for the given project.
         *
         * @param project The project instance for which the dispatcher is created or retrieved.
         * @return An instance of ToolWindowManagerDispatcher.
         */
        fun getInstance(project: Project): ToolWindowManagerDispatcher {
            return project.getUserData(KEY) ?: ToolWindowManagerDispatcher(project).apply {
                project.putUserData(KEY, this)
            }
        }
    }

    /**
     * Loads the current settings mode from the project settings.
     */
    private fun loadSettingsMode() {
        val settings = project.getService(ToolWindowManagerSettings::class.java)
        _settingsMode = settings.getSettingsMode() ?: DEFAULT_SETTINGS_MODE
        switchSettingsMode(settingsMode)
    }

    /**
     * Initializes the settings manager map with services for global and project-level tool window management.
     */
    private fun initializeSettingsManagerMap() {
        settingsManagerMap[SettingsMode.GLOBAL] =
            ApplicationManager.getApplication().getService(GlobalToolWindowManagerService::class.java)
        settingsManagerMap[SettingsMode.PROJECT] = project.getService(ProjectToolWindowManagerService::class.java)
    }

    /**
     * Applies the current preferences to the tool windows, either by saving changes or reverting to defaults.
     */
    fun apply() {
        lock.lock()
        try {
            val view = PreferredAvailabilitiesViewHolder.getInstance(project)
            val editedPrefs = view.getCurrentViewState()
            val toSave = mutableMapOf<String, ToolWindowPreference?>()

            editedPrefs.forEach { pref ->
                val defaultPref = getCurrentSettingsManager().getDefaultAvailabilityToolWindow(pref.id)
                toSave[pref.id!!] = if (pref.availabilityPreference != UNAFFECTED) pref else defaultPref
            }

            applyPreferences(toSave)
        } finally {
            lock.unlock()
        }
    }

    /**
     * Checks if any preferences have been modified.
     *
     * @return true if preferences have been modified; false otherwise.
     */
    fun isModified(): Boolean {
        val view = PreferredAvailabilitiesViewHolder.getInstance(project)
        val currentPrefs = getCurrentAvailabilityToolWindows().associateBy { it.id }
        return view.getCurrentViewState().any { editedPref ->
            val current = currentPrefs[editedPref.id]?.availabilityPreference ?: UNAFFECTED
            current != editedPref.availabilityPreference
        }
    }

    /**
     * Resets the tool windows to their default preferences.
     */
    fun reset() {
        lock.lock()
        try {
            applyCurrentPreferences()
            val view = PreferredAvailabilitiesViewHolder.getInstance(project)
            view.reset(getAvailableToolWindows())
        } finally {
            lock.unlock()
        }
    }

    /**
     * Applies the given preferences to the current settings manager.
     *
     * @param preferences A map of tool window IDs and their corresponding preferences.
     */
    fun applyPreferences(preferences: Map<String, ToolWindowPreference?>) {
        lock.lock()
        try {
            getCurrentSettingsManager().setPreferences(preferences)
            applyCurrentPreferences()
        } finally {
            lock.unlock()
        }
    }

    /**
     * Applies the current preferences to the tool windows.
     */
    private fun applyCurrentPreferences() {
        val prefs = getCurrentAvailabilityToolWindows()
        ToolWindowPreferenceApplier.getInstance(project).applyPreferencesFrom(prefs)
    }

    /**
     * Retrieves the available tool windows based on their current preferences.
     *
     * @return A list of available tool window preferences.
     */
    fun getAvailableToolWindows(): List<ToolWindowPreference> {
        val result = mutableListOf<ToolWindowPreference>()
        val manager = ToolWindowManager.getInstance(project)

        manager.toolWindowIds.forEach { id ->
            val tw = manager.getToolWindow(id)
            tw?.let {
                val defaultPref =
                    getCurrentSettingsManager().getDefaultPreferences()[id] ?: ToolWindowPreference(id, UNAFFECTED)
                val pref = getCurrentSettingsManager().getPreferences()[id] ?: defaultPref
                result.add(pref)
            }
        }

        result.sortBy { it.id }
        return result
    }

    /**
     * Switches to a different settings mode (global or project-specific).
     *
     * @param settingsMode The new settings mode.
     */
    fun switchSettingsMode(settingsMode: SettingsMode) {
        _settingsMode = settingsMode
        saveSettingsMode(settingsMode)
    }

    /**
     * Saves the current settings mode to the project settings.
     *
     * @param settingsMode The settings mode to save.
     */
    private fun saveSettingsMode(settingsMode: SettingsMode) {
        val settings = project.getService(ToolWindowManagerSettings::class.java)
        settings.setSettingsMode(settingsMode)
    }

    /**
     * Retrieves the settings manager corresponding to the current settings mode.
     *
     * @return The current settings manager.
     * @throws IllegalStateException if no SettingsManager is found for the current settings mode.
     */
    private fun getCurrentSettingsManager(): SettingsManager {
        return settingsManagerMap[settingsMode]
            ?: throw IllegalStateException("SettingsManager not found for $settingsMode")
    }

    /**
     * Retrieves the current preferences for all tool windows.
     *
     * @return A list of tool window preferences.
     */
    fun getCurrentAvailabilityToolWindows(): List<ToolWindowPreference> {
        return getCurrentSettingsManager().getPreferences().values.toList()
    }

    /**
     * Retrieves the default preferences for all tool windows.
     *
     * @return A list of tool window preferences with default settings.
     */
    fun getDefaultAvailabilityToolWindows(): List<ToolWindowPreference> {
        return getCurrentSettingsManager().getDefaultPreferences().values.toList()
    }

    /**
     * Retrieves the default preference for a specific tool window.
     *
     * @param id The ID of the tool window.
     * @return The default preference for the specified tool window.
     */
    fun getDefaultAvailabilityToolWindow(id: String?): ToolWindowPreference? {
        return getCurrentSettingsManager().getDefaultAvailabilityToolWindow(id)
    }

    /**
     * Initializes the default preferences for tool windows based on their current availability.
     *
     * @param project The project instance for which the preferences are initialized.
     */
    fun initializeDefaultPreferences(project: Project) {
        val defaultPreferences = mutableMapOf<String, ToolWindowPreference>()
        val manager = ToolWindowManager.getInstance(project)

        manager.toolWindowIds.forEach { id ->
            val tw = manager.getToolWindow(id)
            tw?.let {
                val actualPref = if (tw.isAvailable) AVAILABLE else UNAVAILABLE
                defaultPreferences[id] = ToolWindowPreference(id, actualPref)
            }
        }

        settingsManagerMap.values.forEach { settingsManager ->
            settingsManager.setDefaultPreferences(defaultPreferences)
        }
    }
}
package com.puhovin.intellijplugin.twm.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.puhovin.intellijplugin.twm.core.ToolWindowManagerDispatcher
import com.puhovin.intellijplugin.twm.ui.PreferredAvailabilitiesViewHolder
import com.puhovin.intellijplugin.twm.util.ToolWindowManagerBundle
import javax.swing.JComponent
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.Nullable

/**
 * Action for opening the configuration dialog for preferred availabilities.
 */
class ConfigurePreferredAvailabilitiesAction : AnAction() {

    /**
     * Handles the action performed event.
     *
     * @param e The action event containing the context
     */
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val dispatcher = ToolWindowManagerDispatcher.getInstance(project)
        ShowSettingsUtil.getInstance().editConfigurable(project, object : Configurable {

            /**
             * Gets the display name for the configuration.
             *
             * @return The display name of the configuration
             */
            @Nls
            override fun getDisplayName(): String {
                return ToolWindowManagerBundle.message("configurable.display.name")
            }

            /**
             * Gets the help topic for the configuration.
             *
             * @return The help topic, or null if not available
             */
            @Nullable
            @NonNls
            override fun getHelpTopic(): String? {
                return null
            }

            /**
             * Creates the component for the configuration UI.
             *
             * @return The UI component for the configuration
             */
            override fun createComponent(): JComponent {
                return PreferredAvailabilitiesViewHolder.getInstance(project)
            }

            /**
             * Checks if the configuration has been modified.
             *
             * @return True if modified, false otherwise
             */
            override fun isModified(): Boolean {
                return dispatcher.isModified()
            }

            /**
             * Applies the changes to the configuration.
             */
            override fun apply() {
                dispatcher.apply()
            }

            /**
             * Resets the configuration to its original state.
             */
            override fun reset() {
                dispatcher.reset()
            }

            /**
             * Disposes of UI resources for the configuration.
             */
            override fun disposeUIResources() {
                PreferredAvailabilitiesViewHolder.dispose(project)
            }
        })
    }
}

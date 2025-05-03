package com.github.mrshan23.pytestguard.settings

import com.github.mrshan23.pytestguard.bundles.plugin.PluginLabelsBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.JXTitledSeparator
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField

class PluginSettingsComponent {

    var panel: JPanel? = null

    private var apiKeyTextField = JTextField()

    private var enableSimpleModeCheckBox: JCheckBox = JCheckBox(PluginLabelsBundle.get("enableSimpleMode"))

    private var enableCoverageChangeModeCheckBox: JCheckBox = JCheckBox(PluginLabelsBundle.get("enableCoverageChangeMode"))

    private var testSuitePathTextField = TextFieldWithBrowseButton()

    var apiKey: String
        get() = apiKeyTextField.text
        set(newConfig) {
            apiKeyTextField.text = newConfig
        }

    var enableSimpleModeCheckBoxSelected: Boolean
        get() = enableSimpleModeCheckBox.isSelected
        set(newStatus) {
            enableSimpleModeCheckBox.isSelected = newStatus
        }

    var enableCoverageChangeModeCheckBoxSelected: Boolean
        get() = enableCoverageChangeModeCheckBox.isSelected
        set(newStatus) {
            enableCoverageChangeModeCheckBox.isSelected = newStatus
        }

    var testSuitePath: String
        get() = testSuitePathTextField.text
        set(newConfig) {
            testSuitePathTextField.text = newConfig
        }


    init {
        createComponent()
    }

    private fun createComponent() {
        testSuitePathTextField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
            )
        )

        panel = FormBuilder.createFormBuilder()
            .addComponent(JXTitledSeparator(PluginLabelsBundle.get("pluginSettings")))
            .addLabeledComponent(
                JBLabel(PluginLabelsBundle.get("apiKey")),
                apiKeyTextField,
                10,
                false,
            )
            .addComponent(enableSimpleModeCheckBox, 10)
            .addComponent(enableCoverageChangeModeCheckBox, 10)
            .addLabeledComponent(
                JBLabel(PluginLabelsBundle.get("testSuitePath")),
                testSuitePathTextField,
                10,
                false,
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }


}
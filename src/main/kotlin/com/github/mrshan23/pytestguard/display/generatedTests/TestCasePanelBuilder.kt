package com.github.mrshan23.pytestguard.display.generatedTests

import com.github.mrshan23.pytestguard.bundles.plugin.PluginLabelsBundle
import com.github.mrshan23.pytestguard.bundles.plugin.PluginMessagesBundle
import com.github.mrshan23.pytestguard.utils.ReportUpdater
import com.intellij.lang.Language
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.github.mrshan23.pytestguard.data.Report
import com.github.mrshan23.pytestguard.data.TestCase
import com.github.mrshan23.pytestguard.display.PyTestGuardIcons
import com.github.mrshan23.pytestguard.test.TestFramework
import com.github.mrshan23.pytestguard.test.TestProcessor
import com.github.mrshan23.pytestguard.utils.IconButtonCreator
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import com.jetbrains.python.PythonLanguage
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

class TestCasePanelBuilder(
    private val project: Project,
    private val testCase: TestCase,
    private val report: Report,
    private val testFramework: TestFramework,
    private val generatedTestsTabData: GeneratedTestsTabData,
) {

    private val panel = JPanel()


    private val copyButton =
        IconButtonCreator.getButton(PyTestGuardIcons.copy, PluginLabelsBundle.get("copyTip"))

    private val removeButton =
        IconButtonCreator.getButton(PyTestGuardIcons.remove, PluginLabelsBundle.get("removeTip"))

    private val runTestCaseButton =
        IconButtonCreator.getButton(PyTestGuardIcons.runTestCase, PluginLabelsBundle.get("runTestTip"))

    private val languageId: String = PythonLanguage.INSTANCE.id

    // Add an editor to modify the test source code
    private val languageTextField = LanguageTextField(
        Language.findLanguageByID(languageId),
        project,
        testCase.testCode,
        SimpleDocumentCreator(),
        false,
    )

    private val languageTextFieldScrollPane = JBScrollPane(
        languageTextField,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS,
    )

    /**
     * Retrieves the upper panel for the GUI.
     *
     * This panel contains various components such as buttons, labels, and checkboxes. It is used to display information and
     * perform actions related to the GUI.
     *
     * @return The JPanel object representing the upper panel.
     */
    fun getUpperPanel(): JPanel {
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(Box.createHorizontalGlue())
        panel.add(runTestCaseButton)
        panel.add(copyButton)
        panel.add(removeButton)
        panel.add(Box.createRigidArea(Dimension(12, 0)))

        runTestCaseButton.addActionListener {
            runTestCase()
        }

        removeButton.addActionListener { remove() }

        copyButton.addActionListener { copy() }

        return panel
    }

    /**
     * Retrieves the middle panel of the application.
     * This method sets the border of the languageTextField and
     * adds it to the middlePanel with appropriate spacing.
     */
    fun getMiddlePanel(): JPanel {

        // Set border
        languageTextField.border = JBUI.Borders.empty()

        val panel = JPanel()

        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.add(Box.createRigidArea(Dimension(0, 5)))
        panel.add(languageTextFieldScrollPane)
        panel.add(Box.createRigidArea(Dimension(0, 5)))

        addLanguageTextFieldListener(languageTextField)

        return panel
    }

    /**
     * Adds a document listener to the provided LanguageTextField.
     * The listener triggers the updateUI() method whenever the document of the LanguageTextField changes.
     *
     * @param languageTextField the LanguageTextField to add the listener to
     */
    private fun addLanguageTextFieldListener(languageTextField: LanguageTextField) {
        languageTextField.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                update()
            }
        })
    }

    /**
     * Updates the user interface based on the provided code.
     */
    private fun update() {
        updateTestCaseInformation()

        languageTextField.editor?.markupModel?.removeAllHighlighters()

        languageTextField.border = JBUI.Borders.empty()

        ReportUpdater.updateTestCase(report, testCase)
        GenerateTestsTabHelper.update(generatedTestsTabData)
    }

    private fun remove() {
        // Remove the test case from the cache
        GenerateTestsTabHelper.removeTestCase(testCase.id!!, generatedTestsTabData)

        ReportUpdater.removeTestCase(report, testCase)

        GenerateTestsTabHelper.update(generatedTestsTabData)
    }

    private fun copy() {
        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(
            StringSelection(
                generatedTestsTabData.testCaseIdToEditorTextField[testCase.id]!!.document.text,
            ),
            null,
        )
        NotificationGroupManager.getInstance()
            .getNotificationGroup("UserInterface")
            .createNotification(
                "",
                PluginMessagesBundle.get("testCaseCopied"),
                NotificationType.INFORMATION,
            )
            .notify(project)
    }

    private fun runTestCase() {
        runTestCaseButton.isEnabled = false

        val testProcess = TestProcessor(project)

        testProcess.runTest(testCase, testFramework)

    }

    /**
     * Updates the current test case with the specified test name and test code.
     */
    private fun updateTestCaseInformation() {
        testCase.testCode = languageTextField.document.text
    }

    /**
     * Retrieves the editor text field from the current UI context.
     *
     * @return the editor text field
     */
    fun getEditorTextField(): EditorTextField = languageTextField
}

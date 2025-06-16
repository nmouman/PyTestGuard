package com.github.mrshan23.pytestguard.actions

import com.github.mrshan23.pytestguard.actions.controllers.TestGenerationController
import com.github.mrshan23.pytestguard.actions.controllers.VisibilityController
import com.github.mrshan23.pytestguard.bundles.plugin.PluginLabelsBundle
import com.github.mrshan23.pytestguard.bundles.plugin.PluginMessagesBundle
import com.github.mrshan23.pytestguard.display.PyTestGuardDisplayManager
import com.github.mrshan23.pytestguard.display.PyTestGuardIcons
import com.github.mrshan23.pytestguard.display.TestFrameworkComboBox
import com.github.mrshan23.pytestguard.llm.Llm
import com.github.mrshan23.pytestguard.psi.PsiHelper
import com.github.mrshan23.pytestguard.test.TestFramework
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.jetbrains.python.psi.PyFile
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*


class PyTestGuardActionWindow(
    private val e: AnActionEvent,
    private val visibilityController: VisibilityController,
    private val testGenerationController: TestGenerationController,
    private val pyTestGuardDisplayManager: PyTestGuardDisplayManager,
) : JFrame("PyTestGuard") {

    private val project: Project = e.project!!

    private val cardLayout = CardLayout()

    private val okButton = JButton(PluginLabelsBundle.get("ok"))

    private val testFrameworkSelector = TestFrameworkComboBox()

    private val caretOffset: Int = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!.offset

    private val psiHelper: PsiHelper
        get() {
            val file = e.dataContext.getData(CommonDataKeys.PSI_FILE) as? PyFile
                ?: throw IllegalStateException("No PyFile found")
            return PsiHelper(file)
        }

    init {
        if (!visibilityController.isVisible) {
            visibilityController.isVisible = true
            isVisible = true

            val panel = JPanel(cardLayout)

            panel.add(getMainPanel(), "main")

            addListeners()

            add(panel)

            pack()

            val dimension: Dimension = Toolkit.getDefaultToolkit().screenSize
            val x = (dimension.width - size.width) / 2
            val y = (dimension.height - size.height) / 2
            setLocation(x, y)
        } else {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("UserInterface")
                .createNotification(
                    PluginMessagesBundle.get("generationWindowWarningTitle"),
                    PluginMessagesBundle.get("generationWindowWarningMessage"),
                    NotificationType.WARNING,
                )
                .notify(e.project)
        }
    }

    private fun getMainPanel(): JPanel {
        val panelTitle = JPanel()
        val textTitle = JLabel("Welcome to PyTestGuard!")
        textTitle.font = Font("Monochrome", Font.BOLD, 20)
        panelTitle.add(JLabel(PyTestGuardIcons.pluginIcon))
        panelTitle.add(textTitle)

        val codeContextPanel = JPanel()
        codeContextPanel.add(JLabel(PluginMessagesBundle.get("generationContextInfoMessage")))
        val button = JRadioButton(psiHelper.getMethodHTMLDisplayName(caretOffset))
        button.isSelected = true
        codeContextPanel.add(button)

        val middlePanel = FormBuilder.createFormBuilder()
            .setFormLeftIndent(10)
            .addLabeledComponent(
                JBLabel(PluginLabelsBundle.get("testFramework")),
                testFrameworkSelector,
                10,
                false,
            )
            .addComponent(
                codeContextPanel,
                10,
            )
            .panel

        // Create OK button panel
        val okButtonPanel = JPanel()
        okButtonPanel.layout = BoxLayout(okButtonPanel, BoxLayout.Y_AXIS)
        okButton.alignmentX = CENTER_ALIGNMENT
        okButtonPanel.add(Box.createVerticalStrut(10)) // Add some space between label and button
        okButtonPanel.add(okButton)

        // Assemble main panel
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(panelTitle, BorderLayout.NORTH)
        mainPanel.add(middlePanel, BorderLayout.CENTER)
        mainPanel.add(okButtonPanel, BorderLayout.SOUTH)

        return mainPanel
    }


    /**
     * Adds listeners to various components in the given panel.
     */
    private fun addListeners() {
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                visibilityController.isVisible = false
            }
        })

        okButton.addActionListener {
            startLLMUnitTestGeneration()
        }
    }

    private fun startLLMUnitTestGeneration() {
        if (!testGenerationController.isGeneratorRunning(project)) {
            val llm = Llm(project)
            llm.generateTestsForMethod(
                            psiHelper,
                            caretOffset,
                            testFrameworkSelector.selectedItem!! as TestFramework,
                            testGenerationController,
                            pyTestGuardDisplayManager,
                        )
        }

        visibilityController.isVisible = false
        isVisible = false
        dispose()
    }
}



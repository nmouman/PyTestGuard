package com.github.mrshan23.pytestguard.llm


import com.github.mrshan23.pytestguard.actions.controllers.TestGenerationController
import com.github.mrshan23.pytestguard.bundles.plugin.PluginMessagesBundle
import com.github.mrshan23.pytestguard.data.Report
import com.github.mrshan23.pytestguard.data.TestCase
import com.github.mrshan23.pytestguard.display.PyTestGuardDisplayManager
import com.github.mrshan23.pytestguard.display.custom.CustomProgressIndicator
import com.github.mrshan23.pytestguard.llm.prompt.PromptGenerator
import com.github.mrshan23.pytestguard.psi.PsiHelper
import com.github.mrshan23.pytestguard.settings.PluginSettingsService
import com.github.mrshan23.pytestguard.test.TestAssembler
import com.github.mrshan23.pytestguard.test.TestFramework
import com.github.mrshan23.pytestguard.utils.FileUtils
import com.github.mrshan23.pytestguard.utils.createErrorNotification
import com.github.mrshan23.pytestguard.utils.createWarningNotification
import com.github.mrshan23.pytestguard.utils.isProcessStopped
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager


class Llm(private val project: Project) {

    private var report : Report? = null

    fun generateTestsForMethod(
        psiHelper: PsiHelper,
        caretOffset: Int,
        testFramework: TestFramework,
        testGenerationController: TestGenerationController,
        pyTestGuardDisplayManager: PyTestGuardDisplayManager,
    ) {
        testGenerationController.errorMonitor.clear()
        pyTestGuardDisplayManager.clear()

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, PluginMessagesBundle.get("testGenerationMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        val customProgressIndicator = CustomProgressIndicator(indicator)
                        testGenerationController.indicator = customProgressIndicator

                        if (isProcessStopped(testGenerationController.errorMonitor, customProgressIndicator)) return

                        // Get API key from settings
                        val settingsState = project.service<PluginSettingsService>().state
                        val apiKey = settingsState.apiKey

                        // Send notification if key is empty
                        if (apiKey.isEmpty()) {
                            testGenerationController.errorMonitor.notifyErrorOccurrence()

                            createWarningNotification(
                                PluginMessagesBundle.get("missingAPIKeyTitle"),
                                PluginMessagesBundle.get("missingAPIKeyMessage"),
                                project
                            )

                            return
                        }

                        val manager = GeminiRequestManager(apiKey)

                        val promptGenerator = PromptGenerator(psiHelper, caretOffset, testFramework)
                        val testAssembler = TestAssembler(testGenerationController.errorMonitor, customProgressIndicator)

                        // Get system prompt and generate user prompt
                        val systemPrompt = promptGenerator.getSystemPrompt()
                        val userPrompt = promptGenerator.generateUserPrompt()

                        if (isProcessStopped(testGenerationController.errorMonitor, customProgressIndicator)) return

                        // Send request to gemini
                        val response = manager.sendRequest(
                            systemPrompt = systemPrompt,
                            userPrompt = userPrompt,
                            testAssembler = testAssembler,
                        )

                        // Send notification if gemini request is not successful
                        if (response.isFailure) {
                            testGenerationController.errorMonitor.notifyErrorOccurrence()

                            createErrorNotification(
                                PluginMessagesBundle.get("geminiErrorTitle"),
                                response.exceptionOrNull()?.message!!,
                                project
                            )

                            return
                        }

                        if (isProcessStopped(testGenerationController.errorMonitor, customProgressIndicator)) return

                        report = Report()

                        // Assemble test suite from gemini response
                        val generatedTestSuite = testAssembler.assembleTestSuite(testFramework)
                        generatedTestSuite?.let {
                            addTestCasesToReport(report!!, it)
                            report!!.testFramework = testFramework
                        } ?: run {
                            testGenerationController.errorMonitor.notifyErrorOccurrence()

                            createErrorNotification(
                                PluginMessagesBundle.get("geminiErrorTitle"),
                                PluginMessagesBundle.get("geminiNoGenerationMessage"),
                                project
                            )

                            return
                        }

                        if (isProcessStopped(testGenerationController.errorMonitor, customProgressIndicator)) return

                        customProgressIndicator.stop()
                    } catch (e: RuntimeException) {
                        e.printStackTrace()
                    }
                }

                override fun onFinished() {
                    super.onFinished()
                    testGenerationController.finished()

                    if (testGenerationController.errorMonitor.hasErrorOccurred() || report == null) return

                    // If there are existing results from previous test generations, remove them and create a new one
                    FileUtils.removeDirectory(FileUtils.getPyTestGuardResultsDirectoryPath(project))
                    FileUtils.createHiddenPyTestGuardResultsDirectory(project)

                    pyTestGuardDisplayManager.display(
                        report!!,
                        testFramework,
                        project,
                    )

                    VirtualFileManager.getInstance().syncRefresh()
                }

            })
    }

    /**
     * Records the generated test cases in the given report.
     *
     * @param report The report object to store the test cases in.
     * @param testCases The list of test cases generated by LLM.
     */
    private fun addTestCasesToReport(report: Report, testCases: List<TestCase>) {
        for ((index, test) in testCases.withIndex()) {
            test.id = index
            test.uniqueTestName = FileUtils.getUniqueTestCaseName(test.testName)
            report.testCaseList[index] = test
        }
    }

}

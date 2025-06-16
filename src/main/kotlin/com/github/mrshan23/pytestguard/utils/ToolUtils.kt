package com.github.mrshan23.pytestguard.utils

import com.github.mrshan23.pytestguard.display.custom.CustomProgressIndicator
import com.github.mrshan23.pytestguard.monitor.ErrorMonitor
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.types.TypeEvalContext

fun PyFunction.getFunctionSignature(containingFile: PsiFile): String {
    val parameters = parameterList.parameters.map { it.text }
    val returnType = TypeEvalContext.codeAnalysis(project, containingFile).getReturnType(this)?.name ?: "Any"
    val typeParameters = typeParameterList?.typeParameters?.map { it.name } ?: emptyList()
    val typeParametersString = if (typeParameters.isNotEmpty()) "<${typeParameters.joinToString(", ")}>" else ""
    val parametersString = parameters.joinToString(", ")
    return "def $name$typeParametersString($parametersString): $returnType"
}

fun createWarningNotification(title: String, message: String, project: Project) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("UserInterface")
        .createNotification(
            title,
            message,
            NotificationType.WARNING,
        )
        .notify(project)
}

fun createErrorNotification(title: String, message: String, project: Project) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("UserInterface")
        .createNotification(
            title,
            message,
            NotificationType.ERROR,
        )
        .notify(project)
}

/**
 * Checks if the process has been stopped.
 *
 * @param errorMonitor the class responsible for monitoring the errors during the test generation process
 * @param indicator the progress indicator for tracking the progress of the process
 *
 * @return true if the process has been stopped, false otherwise
 */
fun isProcessStopped(errorMonitor: ErrorMonitor, indicator: CustomProgressIndicator): Boolean {
    return errorMonitor.hasErrorOccurred() || isProcessCanceled(errorMonitor, indicator)
}

fun isProcessCanceled(errorMonitor: ErrorMonitor, indicator: CustomProgressIndicator): Boolean {
    if (indicator.isCanceled()) {
        errorMonitor.notifyErrorOccurrence()
        return true
    }
    return false
}

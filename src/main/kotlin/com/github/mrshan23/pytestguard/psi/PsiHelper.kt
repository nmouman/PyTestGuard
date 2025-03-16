package com.github.mrshan23.pytestguard.psi

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction


class PsiHelper(private val pyFile: PyFile) {

    private val log = Logger.getInstance(this::class.java)

    fun availableForGeneration(e: AnActionEvent): Boolean {

        val caret: Caret =
            e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return false

        return getFunctionForGeneration(caret.offset) != null
    }

    private fun getFunctionForGeneration(caretOffset: Int): PsiFunctionContext? {
        val element = pyFile.findElementAt(caretOffset) ?: return null

        val function = element.parentOfType<PyFunction>(withSelf = false) ?: return null

        if (function.name != null) {
            val functionContext = PsiFunctionContext(function)

            log.info("Function for caret at $caretOffset is ${functionContext.name}")

            return functionContext
        }

        log.info("There is no function at caret $caretOffset")
        return null
    }

    fun getMethodHTMLDisplayName(caretOffset: Int): String {
        val function = getFunctionForGeneration(caretOffset) ?: return ""
        return "<html><b>${function.name}</b></html>"
    }
}
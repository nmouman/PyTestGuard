package com.github.mrshan23.pytestguard.actions

import com.github.mrshan23.pytestguard.actions.controllers.TestGenerationController
import com.github.mrshan23.pytestguard.actions.controllers.VisibilityController
import com.github.mrshan23.pytestguard.display.PyTestGuardDisplayManager
import com.github.mrshan23.pytestguard.psi.PsiHelper
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.jetbrains.python.psi.PyFile


class PyTestGuardAction: AnAction() {
    private var pyTestGuardActionWindow : PyTestGuardActionWindow? = null

    override fun actionPerformed(e: AnActionEvent) {
        if (pyTestGuardActionWindow?.isVisible == true) {
            return
        }

        pyTestGuardActionWindow = PyTestGuardActionWindow(
            e=e,
            visibilityController=VisibilityController(),
            testGenerationController=TestGenerationController(),
            pyTestGuardDisplayManager= PyTestGuardDisplayManager()
        )
    }

    override fun update(e: AnActionEvent) {
        val file = e.dataContext.getData(CommonDataKeys.PSI_FILE) as? PyFile

        if (file == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val psiHelper = PsiHelper(file)

        e.presentation.isEnabledAndVisible = psiHelper.availableForGeneration(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
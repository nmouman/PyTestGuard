package com.github.mrshan23.pytestguard.display.inspections

import com.github.mrshan23.pytestguard.bundles.plugin.PluginMessagesBundle
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyExpressionStatement
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.inspections.universal.AbstractUniversalTestSmellInspection
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils

class UnknownTestTestSmellInspection : AbstractUniversalTestSmellInspection() {

    override fun buildUniversalVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : PyInspectionVisitor(holder, getContext(session)) {
            private fun registerNoAssertion(valueParam: PsiElement) {
                holder.registerProblem(
                    valueParam,
                    PluginMessagesBundle.get("inspections.unknownTest.description")
                )
            }

            override fun visitPyFunction(testMethod: PyFunction) {
                super.visitPyFunction(testMethod)

                if (!GeneralInspectionsUtils.checkValidMethod(testMethod)) {
                    return
                }

                val statements = testMethod.statementList.statements

                for (statement in statements) {
                    val line = statement.text.trimStart()

                    val isAssertion = line.contains(".assert")
                            || line.startsWith("self.assert")

                    if (statement is PyAssertStatement
                        || (statement is PyExpressionStatement && isAssertion)) {
                        return
                    }
                }
                registerNoAssertion(testMethod.nameIdentifier!!)
            }
        }
    }
}
package com.github.mrshan23.pytestguard.psi

import com.github.mrshan23.pytestguard.utils.getFunctionSignature
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyClass

class PsiClassContext(private val psiClass: PyClass) {

    val name: String? get() = psiClass.name

    val text: String? = psiClass.text

    val methodSignatures: List<String>
        get() = psiClass.methods.map { it.getFunctionSignature(psiClass.containingFile) }

    val variablesInInit: List<String>
        get() = findInstanceVariablesInInit(psiClass)

    private fun findInstanceVariablesInInit(pyClass: PyClass): List<String> {
        val initMethod = pyClass.findMethodByName("__init__", false, null) ?: return emptyList()

        val statements = initMethod.statementList.statements

        return statements.filterNotNull().filterIsInstance<PyAssignmentStatement>().map { it.text?: "" }
    }

}
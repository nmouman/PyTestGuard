package com.github.mrshan23.pytestguard.psi

import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.types.TypeEvalContext

class PsiClassContext(private val psiClass: PyClass) {

    val name: String? get() = psiClass.name

    val text: String? = psiClass.text

    val methodSignatures: List<String>
        get() = psiClass.methods.map { getFunctionSignature(it) }

    val variablesInInit: List<String>
        get() = findInstanceVariablesInInit(psiClass)

    private fun findInstanceVariablesInInit(pyClass: PyClass): List<String> {
        // Get the __init__ method from the class
        val initMethod = pyClass.findMethodByName("__init__", false, null) ?: return emptyList()

        val statements = initMethod.statementList.statements

        return statements.filterNotNull().filterIsInstance<PyAssignmentStatement>().map { it.text?: "" }
    }

    private fun getFunctionSignature(pyFunction: PyFunction): String {
        pyFunction.run {
            val parameters = parameterList.parameters.map { it.text }
            val returnType = TypeEvalContext.codeAnalysis(project, psiClass.containingFile).getReturnType(this)?.name ?: "Any"
            val typeParameters = typeParameterList?.typeParameters?.map { it.name } ?: emptyList()
            val typeParametersString = if (typeParameters.isNotEmpty()) "<${typeParameters.joinToString(", ")}>" else ""
            val parametersString = parameters.joinToString(", ")
            return "def $name$typeParametersString($parametersString): $returnType"
        }
    }

}
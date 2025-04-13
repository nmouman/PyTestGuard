package com.github.mrshan23.pytestguard.utils

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

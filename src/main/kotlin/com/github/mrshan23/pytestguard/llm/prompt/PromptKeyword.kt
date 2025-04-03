package com.github.mrshan23.pytestguard.llm.prompt

enum class PromptKeyword(val mandatory: Boolean) {
    METHOD_NAME(true),
    TEST_FRAMEWORK(true),
    IMPORTED_MODULES(false),
    GLOBAL_FIELDS(false),
    CLASS_NAME(false),
    FILE_METHOD_SIGNATURES(false),
    CLASS_METHOD_SIGNATURES(false),
    METHOD_UNDER_TEST(true),
    ;

    /**
     * Returns a keyword's text (i.e., its name) with a `$` attached at the start.
     *
     * Inside a prompt template every keyword is used as `$KEYWORD_NAME`.
     * Therefore, this property encapsulates the keyword's representation in a prompt.
     */
    val variable: String
        get() = "\$${this.name}"
}
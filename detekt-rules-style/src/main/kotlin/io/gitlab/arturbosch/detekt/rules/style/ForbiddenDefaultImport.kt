package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import io.gitlab.arturbosch.detekt.api.simplePatternToRegex
import org.jetbrains.kotlin.psi.KtImportDirective

@RequiresTypeResolution
class ForbiddenDefaultImport(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "TODO",
        Debt.TEN_MINS
    )

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)

        val import = importDirective.importedFqName?.asString().orEmpty()
        if (DEFAULT_IMPORTS.any { it.matches(import) }) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(importDirective),
                    "The import " +
                        "$import has been forbidden in the Detekt config."
                )
            )
        }
    }

    private companion object {
        private val DEFAULT_IMPORTS = listOf(
            "kotlin.*",
            "kotlin.annotation.*",
            "kotlin.collections.*",
            "kotlin.comparisons.*",
            "kotlin.io.*",
            "kotlin.ranges.*",
            "kotlin.sequences.*",
            "kotlin.text.*",
        ).map(String::simplePatternToRegex)
    }
}

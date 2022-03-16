package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.*
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
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
        if(importDirective.aliasName != null) return

        val importedName = importDirective.importPath?.importedName
        val importPath = importDirective.importPath
        if (importPath== null) return

        val pathToStrip = if (importPath.isAllUnder) {
            importPath?.fqName?.asString()
        } else {
            importPath.pathStr
        }
        val qualifiedPackage = importPath?.pathStr?.removeSuffix(".$pathToStrip")

        if (DEFAULT_IMPORTS.contains(qualifiedPackage)) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(importDirective),
                    "The import " +
                        "$qualifiedPackage has been forbidden in the Detekt config."
                )
            )
        }
    }

    private companion object {
        private val DEFAULT_IMPORTS = setOf(
            "kotlin",
            "kotlin.annotation",
            "kotlin.collections",
            "kotlin.comparisons",
            "kotlin.io",
            "kotlin.ranges",
            "kotlin.sequences",
            "kotlin.text",
        )
    }
}

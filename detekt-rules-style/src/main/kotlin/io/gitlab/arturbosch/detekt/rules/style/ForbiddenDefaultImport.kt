package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.*
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.resolve.BindingContext

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
        if (bindingContext == BindingContext.EMPTY) return
        if (importDirective.aliasName != null) return

        val importPath = importDirective.importPath ?: return

        val qualifiedPackage = if (importPath.isAllUnder) {
            importPath.fqName.asString()
        } else {
            val nameToStrip = importPath.importedName
            importPath.pathStr.removeSuffix(".$nameToStrip")
        }

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

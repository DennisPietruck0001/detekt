package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.*
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import io.gitlab.arturbosch.detekt.rules.fqNameOrNull
import org.jetbrains.kotlin.codegen.kotlinType
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.resolve.BindingContext

@RequiresTypeResolution
class ForbiddenDefaultImport(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "TODO",
        Debt.TEN_MINS
    )

    private val violatingImportDirectiveCandidates = mutableListOf<KtImportDirective>()

    override fun visitImportDirective(importDirective: KtImportDirective) {
        val e = listOf("").map(::Exception)
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
            violatingImportDirectiveCandidates += importDirective
        }
    }

    override fun visitReferenceExpression(expression: KtReferenceExpression) {
        super.visitReferenceExpression(expression)
        if (violatingImportDirectiveCandidates.isEmpty()) return
        val referencedType = expression.kotlinType(bindingContext)?.fqNameOrNull() ?: return

        violatingImportDirectiveCandidates.removeAll { candidate ->
            val violatingType = candidate.importPath?.fqName
            violatingType != null
                && violatingType != referencedType
                && violatingType.shortName() == referencedType.shortName()
        }
    }

    override fun postVisit(root: KtFile) {
        super.postVisit(root)

        violatingImportDirectiveCandidates.forEach { importDirective ->
            report(
                CodeSmell(
                    issue,
                    Entity.from(importDirective),
                    "The import '${importDirective.importedFqName}' has been forbidden in the Detekt config."
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

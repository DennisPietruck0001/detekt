package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.*
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtImportInfo
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.getReferenceTargets

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

        val content: KtImportInfo.ImportContent.ExpressionBased? = importDirective.importContent as? KtImportInfo.ImportContent.ExpressionBased
        val referenceTargets = content?.expression?.getReferenceTargets(bindingContext)

        if (DEFAULT_IMPORTS.contains(qualifiedPackage)) {
            violatingImportDirectiveCandidates += importDirective
        }
    }

    override fun postVisit(root: KtFile) {
        super.postVisit(root)

        violatingImportDirectiveCandidates.forEach { importDirective ->
            report(
                CodeSmell(
                    issue,
                    Entity.from(importDirective),
                    "The import ${importDirective.importPath?.pathStr} has been forbidden in the Detekt config."
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

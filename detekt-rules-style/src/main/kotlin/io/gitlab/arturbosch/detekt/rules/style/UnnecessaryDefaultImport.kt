package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.*
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import io.gitlab.arturbosch.detekt.rules.fqNameOrNull
import io.gitlab.arturbosch.detekt.rules.identifierName
import org.jetbrains.kotlin.codegen.kotlinType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.ImportPath

@RequiresTypeResolution
class UnnecessaryDefaultImport(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "TODO", // TODO
        Debt.TEN_MINS
    )

    private fun ImportPath.importedTypeOrNull(): String? = importedName?.asString()
    private fun KtImportDirective.importedTypeOrNull(): String? = importPath?.importedTypeOrNull()

    private val violatingImportDirectiveCandidates = mutableListOf<KtImportDirective>()

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)
        if (bindingContext == BindingContext.EMPTY) return
        if (importDirective.aliasName != null) return

        val importPath = importDirective.importPath ?: return

        val qualifiedPackage = if (importPath.isAllUnder) {
            importPath.fqName.asString()
        } else {
            val nameToStrip = importPath.importedTypeOrNull()
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

    override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
        super.visitNamedDeclaration(declaration)

        violatingImportDirectiveCandidates.removeAll{ candidate ->
            candidate.importedTypeOrNull() == declaration.identifierName()
        }
    }

    override fun postVisit(root: KtFile) {
        super.postVisit(root)

        violatingImportDirectiveCandidates.forEach { importDirective ->
            report(
                CodeSmell(
                    issue,
                    Entity.from(importDirective),
                    "The import '${importDirective.importedFqName}' is unnecessary because it is imported by default."
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

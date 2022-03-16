package io.gitlab.arturbosch.detekt.rules.style

import io.github.detekt.test.utils.resourceAsPath
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.rules.setupKotlinEnvironment
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLint
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KotlinCoreEnvironmentTest
internal class ForbiddenDefaultImportTest : Spek({
    setupKotlinEnvironment(additionalJavaSourceRootPath = resourceAsPath("java"))

    val env: KotlinCoreEnvironment by memoized()
    val subject by memoized { ForbiddenDefaultImport() }

    describe("ForbiddenDefaultImport rule") {
        context("report default import") {
            it("is default import") {
                val code = """
            import kotlin.io.DEFAULT_BUFFER_SIZE
            import kotlin.io.FileTreeWalk
            import kotlin.io.println
            import kotlin.ranges.*
            import kotlin.Exception as KotlinException
            """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).hasSize(5)
            }
        }
        context("ignore non defaults") {
            it("is non default import") {
                val code = """
                    import kotlin.math.PI
                    import kotlin.contracts.Returns
                """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).isEmpty()
            }

            it("should not report without type resolution") {
                val code = """
            import kotlin.io.println
            """
                val findings = subject.compileAndLint(code)
                assertThat(findings).isEmpty()
            }
        }
    }
}
)

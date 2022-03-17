package io.gitlab.arturbosch.detekt.rules.style

import io.github.detekt.test.utils.resourceAsPath
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.rules.setupKotlinEnvironment
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLint
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KotlinCoreEnvironmentTest
internal class UnnecessaryDefaultImportSpec : Spek({
    setupKotlinEnvironment(additionalJavaSourceRootPath = resourceAsPath("java"))
    val subject by memoized { UnnecessaryDefaultImport() }
    describe("UnnecessaryDefaultImport rule") {
        context("without type resolution") {
            it("should not report any violations") {
                val code = """
                    import kotlin.io.println
                """
                val findings = subject.compileAndLint(code)
                assertThat(findings).isEmpty()
            }
        }
        context("with type resolution") {
            val env: KotlinCoreEnvironment by memoized()

            fun assertNoViolations(@Language("kotlin") code: String) {
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).isEmpty()
            }

            context("fully qualified import") {
                val findings by memoized {
                    val code = """
                    import kotlin.io.DEFAULT_BUFFER_SIZE
                    import kotlin.io.FileTreeWalk
                    import kotlin.io.println
            """
                    subject.compileAndLintWithContext(env, code)
                }

                it("should be reported") {
                    assertThat(findings).hasSize(3)
                }

                it("should produce the correct message") {
                    assertThat(findings)
                        .extracting("message")
                        .contains("The import 'kotlin.io.DEFAULT_BUFFER_SIZE' is unnecessary because it is imported by default.")
                }

                it("should produce the correct line numbers") {
                    assertThat(findings)
                        .extracting("location.source.line")
                        .containsExactlyInAnyOrder(1, 2, 3)
                }
            }

            listOf(
                "kotlin.Any",
                "kotlin.annotation.Target",
                "kotlin.collections.Map",
                "kotlin.comparisons.compareBy",
                "kotlin.io.println",
                "kotlin.ranges.IntRange",
                "kotlin.sequences.Sequence",
                "kotlin.text.Regex",
                "java.lang.Integer",
                "kotlin.jvm.JvmStatic",
            ).forEach { symbol ->
                it("should report 'import $symbol'") {
                    val code = """
                    import $symbol
            """
                    val findings = subject.compileAndLintWithContext(env, code)
                    assertThat(findings).hasSize(1)
                }
            }

            it("should report wildcard import") {
                val code = """
                    import kotlin.ranges.*
            """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).hasSize(1)
            }

            it("should not report alias import") {
                assertNoViolations(
                    """
                    import kotlin.Exception as KotlinException
            """
                )
            }

            it("should not report enum value") {
                assertNoViolations(
                    """
                    """
                )
            }

            it("should not report imports from non default packages") {
                assertNoViolations(
                    """
                    import kotlin.math.PI
                    import kotlin.contracts.Returns
                """
                )
            }

            context("and overwritten elements") {
                it("should not report when local type overwrites imported type") {
                    assertNoViolations(
                        """
                        package test

                        import kotlin.Result // we need this
                        
                        fun foo() {
                          val a: Result<Unit> = runCatching { println("bar") }
                        }
                        
                        class Result
                        """
                    )
                }
                it("should not report when local enum class overwrites imported enum class") {
                    assertNoViolations(
                        """
                        package test

                        import kotlin.io.FileWalkDirection // we need this
                        
                        enum class FileWalkDirection

                        val x = FileWalkDirection.TOP_DOWN.name
                """
                    )
                }
                it("should not report when local value overwrites imported value") {
                    assertNoViolations(
                        """
                        package test

                        import kotlin.io.DEFAULT_BUFFER_SIZE // we need this
                        
                        const val DEFAULT_BUFFER_SIZE = 3
                        fun foo() {
                          println(DEFAULT_BUFFER_SIZE)
                        }
                """
                    )
                }
                it("should not report when local function overwrites imported function with same signature") {
                    assertNoViolations(
                        """
                        package test

                        import kotlin.collections.emptyList // we need this

                        fun <T> emptyList(): List<T> = throw NotImplementedError("")
                        
                        val y = emptyList<Int>()
                """
                    )
                }
                it("should not report when local function overwrites imported function with compatible parameters") {
                    assertNoViolations(
                        """
                        package test
                        
                        import kotlin.collections.listOf // we need this
                        
                        fun <T> listOf(i: Int): Map<Int, Int> = throw NotImplementedError("")
                        
                        val y: List<Int> = listOf(1)
                        """
                    )
                }
                xit("should report when local function overwrites imported function with incompatible parameters") {
                    val code = """
                        package test

                        import kotlin.collections.emptyList // this is unnecessary
                        
                        fun emptyList(i: Int) {}

                        val y = emptyList<Int>()
                """
                    val findings = subject.compileAndLintWithContext(env, code)
                    assertThat(findings).isEmpty()
                }
            }
        }
    }
}
)

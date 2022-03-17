package io.gitlab.arturbosch.detekt.rules.style

import io.github.detekt.test.utils.resourceAsPath
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.rules.setupKotlinEnvironment
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLint
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KotlinCoreEnvironmentTest
internal class UnnecessaryDefaultImportSpec : Spek({
    setupKotlinEnvironment(additionalJavaSourceRootPath = resourceAsPath("java"))

    val env: KotlinCoreEnvironment by memoized()
    val subject by memoized { UnnecessaryDefaultImport() }

    describe("UnnecessaryDefaultImport rule") {
        context("report default import") {
            it("is default import") {
                val code = """
                    import kotlin.ranges.*
                    import kotlin.io.DEFAULT_BUFFER_SIZE
                    import kotlin.io.FileTreeWalk
                    import kotlin.io.println
            """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).hasSize(4)
            }

            xit("should report when import unnecessarily overwrites local function with different signature") {
                // the kotlin function is preferred if the signature matches
                val code = """
                        package test

                        import kotlin.collections.emptyList
                        
                        fun emptyList(i: Int) {}

                        val y = emptyList<Int>()
                """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).hasSize(1)
            }
        }
        context("ignore non defaults") {
            it("is non default import") {
                val code = """
                    import kotlin.math.PI
                    import kotlin.contracts.Returns
                    import kotlin.Exception as KotlinException
                    import kotlin.io.FileWalkDirection.BOTTOM_UP
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

            it("should not report when import overwrites local") {
                val code = """
                    package test
                    import kotlin.Exception

                    data class Exception(val x: Int)
                    
                    fun kotlinException(): Exception = IllegalStateException("nooo")
                    fun customException(): test.Exception = Exception(1)
                """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).isEmpty()
            }

            it("should not report when import overwrites local type definition") {
                val code = """
                        package test

                        import kotlin.Result // we need this
                        
                        fun foo() {
                          val a: Result<Unit> = runCatching { println("bar") }
                        }
                        
                        class Result
                """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).isEmpty()
            }

            it("should not report when import overwrites local value") {
                val code = """
                        package test

                        import kotlin.io.DEFAULT_BUFFER_SIZE // we need this
                        
                        const val DEFAULT_BUFFER_SIZE = 3
                        fun foo() {
                          println(DEFAULT_BUFFER_SIZE)
                        }
                """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).isEmpty()
            }

            it("should not report when import overwrites local enum") {
                val code = """
                        package test

                        import kotlin.io.FileWalkDirection // we need this
                        
                        enum class FileWalkDirection

                        val x = FileWalkDirection.TOP_DOWN.name
                """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).isEmpty()
            }

            it("should not report when import overwrites local function with same signature") {
                val code = """
                        package test

                        import kotlin.collections.emptyList // we need this

                        fun <T> emptyList(): List<T> = throw NotImplementedError("")
                        
                        val y = emptyList<Int>()
                """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).isEmpty()
            }

            it("should not report when import overwrites local function with compatible signature") {
                val code = """
                        package test
                        
                        import kotlin.collections.listOf // we need this
                        
                        fun <T> listOf(i: Int): Map<Int, Int> = throw NotImplementedError("")
                        
                        val y: List<Int> = listOf(1)
                """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).isEmpty()
            }

            it("should not report when import overwrites local function with unknown signature") {
                // this case must be fixed
                val code = """
                        package test

                        import kotlin.collections.emptyList
                        
                        fun emptyList(i: Int) {}

                        val y = emptyList<Int>()
                """
                val findings = subject.compileAndLintWithContext(env, code)
                assertThat(findings).isEmpty()
            }
        }
    }
}
)

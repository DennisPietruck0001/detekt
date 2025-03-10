package io.gitlab.arturbosch.detekt.rules.coroutines

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLint
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GlobalCoroutineUsageSpec {
    val subject = GlobalCoroutineUsage(Config.empty)

    @Nested
    inner class `GlobalCoroutineUsage rule` {

        @Test
        @DisplayName("should report GlobalScope.launch")
        fun reportGlobalScopeLaunch() {
            val code = """
                import kotlinx.coroutines.delay
                import kotlinx.coroutines.GlobalScope
                import kotlinx.coroutines.launch

                fun foo() {
                    GlobalScope.launch { delay(1_000L) }
                }
            """
            assertThat(subject.compileAndLint(code)).hasSize(1)
        }

        @Test
        @DisplayName("should report GlobalScope.async")
        fun reportGlobalScopeAsync() {
            val code = """
                import kotlinx.coroutines.async
                import kotlinx.coroutines.delay
                import kotlinx.coroutines.GlobalScope

                fun foo() {
                    GlobalScope.async { delay(1_000L) }
                }
            """
            assertThat(subject.compileAndLint(code)).hasSize(1)
        }

        @Test
        fun `should not report bar(GlobalScope)`() {
            val code = """
                import kotlinx.coroutines.CoroutineScope
                import kotlinx.coroutines.GlobalScope

                fun bar(scope: CoroutineScope) = Unit

                fun foo() {
                    bar(GlobalScope)
                }
            """
            assertThat(subject.compileAndLint(code)).isEmpty()
        }

        @Test
        fun `should not report 'val scope = GlobalScope'`() {
            val code = """
                import kotlinx.coroutines.CoroutineScope
                import kotlinx.coroutines.GlobalScope

                fun bar(scope: CoroutineScope) = Unit

                fun foo() {
                    val scope = GlobalScope
                    bar(scope)
                }
            """
            assertThat(subject.compileAndLint(code)).isEmpty()
        }
    }
}

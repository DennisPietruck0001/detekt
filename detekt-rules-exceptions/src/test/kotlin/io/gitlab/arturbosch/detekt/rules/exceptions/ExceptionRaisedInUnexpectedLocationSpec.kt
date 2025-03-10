package io.gitlab.arturbosch.detekt.rules.exceptions

import io.github.detekt.test.utils.resourceAsPath
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.compileAndLint
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExceptionRaisedInUnexpectedLocationSpec {
    val subject = ExceptionRaisedInUnexpectedLocation()

    @Nested
    inner class `ExceptionRaisedInUnexpectedLocation rule` {

        @Test
        fun `reports methods raising an unexpected exception`() {
            val path = resourceAsPath("ExceptionRaisedInMethodsPositive.kt")
            assertThat(subject.lint(path)).hasSize(5)
        }

        @Test
        fun `does not report methods raising no exception`() {
            val path = resourceAsPath("ExceptionRaisedInMethodsNegative.kt")
            assertThat(subject.lint(path)).isEmpty()
        }

        @Test
        fun `reports the configured method`() {
            val config = TestConfig(mapOf("methodNames" to listOf("toDo", "todo2")))
            val findings = ExceptionRaisedInUnexpectedLocation(config).compileAndLint(
                """
            fun toDo() {
                throw IllegalStateException()
            }"""
            )
            assertThat(findings).hasSize(1)
        }

        @Test
        fun `reports the configured method with String`() {
            val config = TestConfig(mapOf("methodNames" to "toDo,todo2"))
            val findings = ExceptionRaisedInUnexpectedLocation(config).compileAndLint(
                """
            fun toDo() {
                throw IllegalStateException()
            }"""
            )
            assertThat(findings).hasSize(1)
        }
    }
}

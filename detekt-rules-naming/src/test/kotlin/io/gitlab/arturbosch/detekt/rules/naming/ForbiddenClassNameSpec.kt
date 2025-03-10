package io.gitlab.arturbosch.detekt.rules.naming

import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.compileAndLint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val FORBIDDEN_NAME = "forbiddenName"

class ForbiddenClassNameSpec {

    @Nested
    inner class `ForbiddenClassName rule` {

        @Test
        fun `should report classes with forbidden names`() {
            val code = """
                class TestManager {} // violation
                class TestProvider {} // violation
                class TestHolder"""
            assertThat(
                ForbiddenClassName(TestConfig(mapOf(FORBIDDEN_NAME to listOf("Manager", "Provider"))))
                    .compileAndLint(code)
            )
                .hasSize(2)
        }

        @Test
        fun `should report a class that starts with a forbidden name`() {
            val code = "class TestProvider {}"
            assertThat(
                ForbiddenClassName(TestConfig(mapOf(FORBIDDEN_NAME to listOf("test"))))
                    .compileAndLint(code)
            )
                .hasSize(1)
        }

        @Test
        fun `should report classes with forbidden names using config string`() {
            val code = """
                class TestManager {} // violation
                class TestProvider {} // violation
                class TestHolder"""
            assertThat(
                ForbiddenClassName(TestConfig(mapOf(FORBIDDEN_NAME to "Manager, Provider")))
                    .compileAndLint(code)
            )
                .hasSize(2)
        }

        @Test
        fun `should report classes with forbidden names using config string using wildcards`() {
            val code = """
                class TestManager {} // violation
                class TestProvider {} // violation
                class TestHolder"""
            assertThat(
                ForbiddenClassName(TestConfig(mapOf(FORBIDDEN_NAME to "*Manager*, *Provider*")))
                    .compileAndLint(code)
            )
                .hasSize(2)
        }

        @Test
        fun `should report all forbidden names in message`() {
            val code = """
                class TestManager {}"""
            val actual = ForbiddenClassName(TestConfig(mapOf(FORBIDDEN_NAME to "Test, Manager, Provider")))
                .compileAndLint(code)
            assertThat(actual.first().message)
                .isEqualTo("Class name TestManager is forbidden as it contains: Test, Manager")
        }
    }
}

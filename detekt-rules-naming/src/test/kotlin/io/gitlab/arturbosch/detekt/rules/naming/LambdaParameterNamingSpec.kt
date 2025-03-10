package io.gitlab.arturbosch.detekt.rules.naming

import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLint
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LambdaParameterNamingSpec {

    @Nested
    inner class `lambda parameters` {
        @Test
        fun `Reports no supported parameter names`() {
            val code = """
                val a: (String) -> Unit = { HELLO_THERE -> Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .hasSize(1)
                .hasTextLocations("HELLO_THERE")
        }

        @Test
        fun `Reports no supported parameter names when there are multiple`() {
            val code = """
                val a: (String, Int) -> Unit = { HI, HELLO_THERE -> Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .hasSize(2)
                .hasTextLocations("HI", "HELLO_THERE")
        }

        @Test
        fun `Doesn't report a valid parameter`() {
            val code = """
                val a: (String) -> Unit = { helloThere -> Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .isEmpty()
        }

        @Test
        fun `Doesn't report a valid parameter when define type`() {
            val code = """
                val a = { helloThere: String -> Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .isEmpty()
        }

        @Test
        fun `Doesn't report _`() {
            val code = """
                val a: (String) -> Unit = { _ -> Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .isEmpty()
        }

        @Test
        fun `Doesn't report by using implicit name`() {
            val code = """
                val a: (String) -> Unit = { Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .isEmpty()
        }

        @Test
        fun `Doesn't report if there aren't parameters`() {
            val code = """
                val a: () -> Unit = { Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .isEmpty()
        }

        @Test
        fun `Reports no supported destructuring parameter names`() {
            val code = """
                data class Bar(val a: String)
                val a: (Bar) -> Unit = { (HELLO_THERE) -> Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .hasSize(1)
                .hasTextLocations("HELLO_THERE")
        }

        @Test
        fun `Reports no supported destructuring parameter names when there are multiple`() {
            val code = """
                data class Bar(val a: String, val b: String)
                val a: (Bar) -> Unit = { (HI, HELLO_THERE) -> Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .hasSize(2)
                .hasTextLocations("HI", "HELLO_THERE")
        }

        @Test
        fun `Doesn't report valid destructuring parameters`() {
            val code = """
                data class Bar(val a: String, val b: String)
                val a: (Bar) -> Unit = { (a, b) -> Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .isEmpty()
        }

        @Test
        fun `Doesn't report valid destructuring parameters when define type`() {
            val code = """
                data class Bar(val a: String, val b: String)
                val a: (Bar) -> Unit = { (a: String, b) -> Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .isEmpty()
        }

        @Test
        fun `Doesn't report valid destructuring parameters using _`() {
            val code = """
                data class Bar(val a: String, val b: String)
                val a: (Bar) -> Unit = { (_, b) -> Unit }
            """
            assertThat(LambdaParameterNaming().compileAndLint(code))
                .isEmpty()
        }
    }
}

package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class ForbiddenDefaultImportTest(val env: KotlinCoreEnvironment) {
    val subject = ForbiddenDefaultImport()

    @Test
    fun `import defaults should throw error`() {
        val code = """
            import kotlin.io.DEFAULT_BUFFER_SIZE
            import kotlin.io.FileTreeWalk
            import kotlin.io.println
            import kotlin.Exception as KotlinException
        """
        val findings = subject.compileAndLintWithContext(env, code)
        assertThat(findings).hasSize(4)
    }
}

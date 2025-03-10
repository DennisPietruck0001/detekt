package io.gitlab.arturbosch.detekt

import io.gitlab.arturbosch.detekt.testkit.DslGradleRunner
import io.gitlab.arturbosch.detekt.testkit.ProjectLayout
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.EnabledIf
import org.junit.jupiter.api.condition.JRE

class DetektAndroidSpec {
    @Nested
    @EnabledForJreRange(min = JRE.JAVA_11, disabledReason = "Android Gradle Plugin 7.0+ requires JDK 11 or newer")
    @EnabledIf("io.gitlab.arturbosch.detekt.DetektAndroidSpecKt#isAndroidSdkInstalled")
    inner class `When applying detekt in an Android project` {

        @Nested
        inner class `configures android tasks for android application` {
            val projectLayout = ProjectLayout(
                numberOfSourceFilesInRootPerSourceDir = 0,
            ).apply {
                addSubmodule(
                    name = "app",
                    numberOfSourceFilesPerSourceDir = 1,
                    numberOfCodeSmells = 1,
                    buildFileContent = """
                        $APP_PLUGIN_BLOCK
                        $ANDROID_BLOCK
                        $DETEKT_REPORTS_BLOCK
                    """.trimIndent(),
                    srcDirs = listOf(
                        "src/main/java",
                        "src/debug/java",
                        "src/test/java",
                        "src/androidTest/java",
                        "src/main/kotlin",
                        "src/debug/kotlin",
                        "src/test/kotlin",
                        "src/androidTest/kotlin",
                    ),
                    baselineFiles = listOf(
                        "detekt-baseline.xml",
                        "detekt-baseline-release.xml",
                        "detekt-baseline-debug.xml",
                        "detekt-baseline-releaseUnitTest.xml",
                        "detekt-baseline-debugUnitTest.xml",
                        "detekt-baseline-debugAndroidTest.xml"
                    )
                )
            }
            val gradleRunner = createGradleRunnerAndSetupProject(projectLayout).also {
                it.writeProjectFile("app/src/main/AndroidManifest.xml", manifestContent())
            }

            @Test
            @DisplayName("task :app:detektMain")
            fun appDetektMain() {
                gradleRunner.runTasksAndCheckResult(":app:detektMain") { buildResult ->
                    assertThat(buildResult.output).containsPattern("""--baseline \S*[/\\]detekt-baseline-release.xml """)
                    assertThat(buildResult.output).containsPattern("""--baseline \S*[/\\]detekt-baseline-debug.xml """)
                    assertThat(buildResult.output).containsPattern("""--input \S*[/\\]app[/\\]src[/\\]main[/\\]java""")
                    assertThat(buildResult.output).containsPattern("""--input \S*[/\\]app[/\\]src[/\\]debug[/\\]java""")
                    assertThat(buildResult.output).containsPattern("""--input \S*[/\\]app[/\\]src[/\\]main[/\\]kotlin""")
                    assertThat(buildResult.output).containsPattern("""--input \S*[/\\]app[/\\]src[/\\]debug[/\\]kotlin""")
                    assertThat(buildResult.output).contains("--report xml:")
                    assertThat(buildResult.output).contains("--report sarif:")
                    assertThat(buildResult.output).doesNotContain("--report txt:")
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":app:detektMain",
                            ":app:detektDebug"
                        )
                    )
                }
            }

            @Test
            @DisplayName("task :app:detektTest")
            fun appDetektTest() {
                gradleRunner.runTasksAndCheckResult(":app:detektTest") { buildResult ->
                    assertThat(buildResult.output).containsPattern("""--baseline \S*[/\\]detekt-baseline-releaseUnitTest.xml """)
                    assertThat(buildResult.output).containsPattern("""--baseline \S*[/\\]detekt-baseline-debugUnitTest.xml """)
                    assertThat(buildResult.output).containsPattern("""--baseline \S*[/\\]detekt-baseline-debugAndroidTest.xml """)
                    assertThat(buildResult.output).containsPattern("""--input \S*[/\\]app[/\\]src[/\\]test[/\\]java""")
                    assertThat(buildResult.output).containsPattern("""--input \S*[/\\]app[/\\]src[/\\]androidTest[/\\]java""")
                    assertThat(buildResult.output).containsPattern("""--input \S*[/\\]app[/\\]src[/\\]test[/\\]kotlin""")
                    assertThat(buildResult.output).containsPattern("""--input \S*[/\\]app[/\\]src[/\\]androidTest[/\\]kotlin""")
                    assertThat(buildResult.output).contains("--report xml:")
                    assertThat(buildResult.output).contains("--report sarif:")
                    assertThat(buildResult.output).doesNotContain("--report txt:")
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":app:detektDebugUnitTest",
                            ":app:detektDebugAndroidTest"
                        )
                    )
                }
            }
        }

        @Nested
        inner class `does not configures android tasks if user opts out` {
            val projectLayout = ProjectLayout(numberOfSourceFilesInRootPerSourceDir = 0).apply {
                addSubmodule(
                    name = "app",
                    numberOfSourceFilesPerSourceDir = 1,
                    numberOfCodeSmells = 1,
                    buildFileContent = """
                        $APP_PLUGIN_BLOCK
                        $ANDROID_BLOCK
                        $DETEKT_REPORTS_BLOCK
                    """.trimIndent(),
                    srcDirs = listOf("src/main/java", "src/debug/java", "src/test/java", "src/androidTest/java")
                )
            }
            val gradleRunner = createGradleRunnerAndSetupProject(projectLayout).also {
                it.writeProjectFile("gradle.properties", "detekt.android.disabled=true")
                it.writeProjectFile("app/src/main/AndroidManifest.xml", manifestContent())
            }

            @Test
            @DisplayName("task :app:detekt")
            fun appDetekt() {
                gradleRunner.runTasks(":app:detekt")
            }

            @Test
            @DisplayName("task :app:detektMain")
            fun appDetektMain() {
                gradleRunner.runTasksAndExpectFailure(":app:detektMain") { result ->
                    assertThat(result.output).contains("Task 'detektMain' not found in project")
                }
            }

            @Test
            @DisplayName("task :app:detektTest")
            fun appDetektTest() {
                gradleRunner.runTasksAndExpectFailure(":app:detektTest") { result ->
                    assertThat(result.output).contains("Task 'detektTest' not found in project")
                }
            }
        }

        @Nested
        inner class `configures android tasks for android library` {
            val projectLayout = ProjectLayout(numberOfSourceFilesInRootPerSourceDir = 0).apply {
                addSubmodule(
                    name = "lib",
                    numberOfSourceFilesPerSourceDir = 1,
                    numberOfCodeSmells = 1,
                    buildFileContent = """
                        $LIB_PLUGIN_BLOCK
                        $ANDROID_BLOCK
                        $DETEKT_REPORTS_BLOCK
                    """.trimIndent(),
                    srcDirs = listOf("src/main/java", "src/debug/java", "src/test/java", "src/androidTest/java"),
                    baselineFiles = listOf(
                        "detekt-baseline.xml",
                        "detekt-baseline-release.xml",
                        "detekt-baseline-debug.xml",
                        "detekt-baseline-releaseUnitTest.xml",
                        "detekt-baseline-debugUnitTest.xml",
                        "detekt-baseline-debugAndroidTest.xml"
                    )
                )
            }
            val gradleRunner = createGradleRunnerAndSetupProject(projectLayout).also {
                it.writeProjectFile("lib/src/main/AndroidManifest.xml", manifestContent())
            }

            @Test
            @DisplayName("task :lib:detektMain")
            fun libDetektMain() {
                gradleRunner.runTasksAndCheckResult(":lib:detektMain") { buildResult ->
                    assertThat(buildResult.output).containsPattern("""--baseline \S*[/\\]detekt-baseline-release.xml """)
                    assertThat(buildResult.output).containsPattern("""--baseline \S*[/\\]detekt-baseline-debug.xml """)
                    assertThat(buildResult.output).contains("--report xml:")
                    assertThat(buildResult.output).contains("--report sarif:")
                    assertThat(buildResult.output).doesNotContain("--report txt:")
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":lib:detektMain",
                            ":lib:detektDebug"
                        )
                    )
                }
            }

            @Test
            @DisplayName("task :lib:detektTest")
            fun libDetektTest() {
                gradleRunner.runTasksAndCheckResult(":lib:detektTest") { buildResult ->
                    assertThat(buildResult.output).containsPattern("""--baseline \S*[/\\]detekt-baseline-releaseUnitTest.xml """)
                    assertThat(buildResult.output).containsPattern("""--baseline \S*[/\\]detekt-baseline-debugUnitTest.xml """)
                    assertThat(buildResult.output).containsPattern("""--baseline \S*[/\\]detekt-baseline-debugAndroidTest.xml """)
                    assertThat(buildResult.output).contains("--report xml:")
                    assertThat(buildResult.output).contains("--report sarif:")
                    assertThat(buildResult.output).doesNotContain("--report txt:")
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":lib:detektDebugUnitTest",
                            ":lib:detektDebugAndroidTest"
                        )
                    )
                }
            }
        }

        @Nested
        inner class `configures android tasks for different build variants` {

            val projectLayout = ProjectLayout(numberOfSourceFilesInRootPerSourceDir = 0).apply {
                addSubmodule(
                    name = "lib",
                    numberOfSourceFilesPerSourceDir = 1,
                    numberOfCodeSmells = 1,
                    buildFileContent = """
                        $LIB_PLUGIN_BLOCK
                        $ANDROID_BLOCK_WITH_FLAVOR
                        $DETEKT_REPORTS_BLOCK
                    """.trimIndent(),
                    srcDirs = listOf("src/main/java", "src/debug/java", "src/test/java", "src/androidTest/java")
                )
            }
            val gradleRunner = createGradleRunnerAndSetupProject(projectLayout).also {
                it.writeProjectFile("lib/src/main/AndroidManifest.xml", manifestContent())
            }

            @Test
            @DisplayName("task :lib:detektMain")
            fun libDetektMain() {
                gradleRunner.runTasksAndCheckResult(":lib:detektMain") { buildResult ->
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":lib:detektYoungHarryDebug",
                            ":lib:detektOldHarryDebug",
                            ":lib:detektOldHarryRelease"
                        )
                    )
                }
            }

            @Test
            @DisplayName("task :lib:detektTest")
            fun libDetektTest() {
                gradleRunner.runTasksAndCheckResult(":lib:detektTest") { buildResult ->
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":lib:detektYoungHarryDebugUnitTest",
                            ":lib:detektOldHarryDebugUnitTest",
                            ":lib:detektOldHarryReleaseUnitTest",
                            ":lib:detektYoungHarryDebugAndroidTest",
                            ":lib:detektOldHarryDebugAndroidTest"
                        )
                    )
                }
            }
        }

        @Nested
        inner class `configures android tasks for different build variants excluding ignored build types` {

            val projectLayout = ProjectLayout(numberOfSourceFilesInRootPerSourceDir = 0).apply {
                addSubmodule(
                    name = "lib",
                    numberOfSourceFilesPerSourceDir = 1,
                    numberOfCodeSmells = 1,
                    buildFileContent = """
                        $LIB_PLUGIN_BLOCK
                        $ANDROID_BLOCK_WITH_FLAVOR
                        detekt {
                            ignoredBuildTypes = ["release"]
                        }
                    """.trimIndent(),
                    srcDirs = listOf("src/main/java", "src/debug/java", "src/test/java", "src/androidTest/java")
                )
            }
            val gradleRunner = createGradleRunnerAndSetupProject(projectLayout).also {
                it.writeProjectFile("lib/src/main/AndroidManifest.xml", manifestContent())
            }

            @Test
            @DisplayName("task :lib:detektMain")
            fun libDetektMain() {
                gradleRunner.runTasksAndCheckResult(":lib:detektMain") { buildResult ->
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":lib:detektYoungHarryDebug",
                            ":lib:detektOldHarryDebug"
                        )
                    ).doesNotContain(
                        ":lib:detektOldHarryRelease"
                    )
                }
            }

            @Test
            @DisplayName("task :lib:detektTest")
            fun libDetektTest() {
                gradleRunner.runTasksAndCheckResult(":lib:detektTest") { buildResult ->
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":lib:detektYoungHarryDebugUnitTest",
                            ":lib:detektOldHarryDebugUnitTest",
                            ":lib:detektYoungHarryDebugAndroidTest",
                            ":lib:detektOldHarryDebugAndroidTest"
                        )
                    ).doesNotContain(
                        ":lib:detektOldHarryReleaseUnitTest"
                    )
                }
            }
        }

        @Nested
        inner class `configures android tasks for different build variants excluding ignored variants` {

            val projectLayout = ProjectLayout(numberOfSourceFilesInRootPerSourceDir = 0).apply {
                addSubmodule(
                    name = "lib",
                    numberOfSourceFilesPerSourceDir = 1,
                    numberOfCodeSmells = 1,
                    buildFileContent = """
                        $LIB_PLUGIN_BLOCK
                        $ANDROID_BLOCK_WITH_FLAVOR
                        detekt {
                            ignoredVariants = ["youngHarryDebug", "oldHarryRelease"]
                        }
                    """.trimIndent(),
                    srcDirs = listOf("src/main/java", "src/debug/java", "src/test/java", "src/androidTest/java")
                )
            }
            val gradleRunner = createGradleRunnerAndSetupProject(projectLayout).also {
                it.writeProjectFile("lib/src/main/AndroidManifest.xml", manifestContent())
            }

            @Test
            @DisplayName("task :lib:detektMain")
            fun libDetektMain() {
                gradleRunner.runTasksAndCheckResult(":lib:detektMain") { buildResult ->
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":lib:detektOldHarryDebug"
                        )
                    ).doesNotContain(
                        ":lib:detektYoungHarryDebug",
                        ":lib:detektOldHarryRelease"
                    )
                }
            }

            @Test
            @DisplayName("task :lib:detektTest")
            fun libDetektTest() {
                gradleRunner.runTasksAndCheckResult(":lib:detektTest") { buildResult ->
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":lib:detektOldHarryDebugUnitTest",
                            ":lib:detektOldHarryDebugAndroidTest"
                        )
                    ).doesNotContain(
                        ":lib:detektYoungHarryDebugUnitTest",
                        ":lib:detektYoungHarryDebugAndroidTest",
                        ":lib:detektOldHarryReleaseUnitTest"
                    )
                }
            }
        }

        @Nested
        inner class `configures android tasks for different build variants excluding ignored flavors` {

            val projectLayout = ProjectLayout(numberOfSourceFilesInRootPerSourceDir = 0).apply {
                addSubmodule(
                    name = "lib",
                    numberOfSourceFilesPerSourceDir = 1,
                    numberOfCodeSmells = 1,
                    buildFileContent = """
                        $LIB_PLUGIN_BLOCK
                        $ANDROID_BLOCK_WITH_FLAVOR
                        detekt {
                            ignoredFlavors = ["youngHarry"]
                        }
                    """.trimIndent(),
                    srcDirs = listOf("src/main/java", "src/debug/java", "src/test/java", "src/androidTest/java")
                )
            }
            val gradleRunner = createGradleRunnerAndSetupProject(projectLayout).also {
                it.writeProjectFile("lib/src/main/AndroidManifest.xml", manifestContent())
            }

            @Test
            @DisplayName("task :lib:detektMain")
            fun libDetektMain() {
                gradleRunner.runTasksAndCheckResult(":lib:detektMain") { buildResult ->
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":lib:detektOldHarryDebug",
                            ":lib:detektOldHarryRelease"
                        )
                    ).doesNotContain(
                        ":lib:detektYoungHarryDebug"
                    )
                }
            }

            @Test
            @DisplayName("task :lib:detektTest")
            fun libDetektTest() {
                gradleRunner.runTasksAndCheckResult(":lib:detektTest") { buildResult ->
                    assertThat(buildResult.tasks.map { it.path }).containsAll(
                        listOf(
                            ":lib:detektOldHarryDebugUnitTest",
                            ":lib:detektOldHarryDebugAndroidTest",
                            ":lib:detektOldHarryReleaseUnitTest"
                        )
                    ).doesNotContain(
                        ":lib:detektYoungHarryDebugUnitTest",
                        ":lib:detektYoungHarryDebugAndroidTest"
                    )
                }
            }
        }
    }
}

/**
 * ANDROID_SDK_ROOT is preferred over ANDROID_HOME, but the check here is more lenient.
 * See [Android CLI Environment Variables](https://developer.android.com/studio/command-line/variables.html)
 */
internal fun isAndroidSdkInstalled() =
    System.getenv("ANDROID_SDK_ROOT") != null || System.getenv("ANDROID_HOME") != null

internal fun manifestContent(packageName: String = "io.gitlab.arturbosch.detekt.app") = """
    <manifest package="$packageName"
        xmlns:android="http://schemas.android.com/apk/res/android"/>
""".trimIndent()

private val APP_PLUGIN_BLOCK = """
    plugins {
        id "com.android.application"
        id "kotlin-android"
        id "io.gitlab.arturbosch.detekt"
    }
""".trimIndent()

private val LIB_PLUGIN_BLOCK = """
    plugins {
        id "com.android.library"
        id "kotlin-android"
        id "io.gitlab.arturbosch.detekt"
    }
""".trimIndent()

private val ANDROID_BLOCK = """
    android {
       compileSdkVersion 30
    }
""".trimIndent()

private val ANDROID_BLOCK_WITH_FLAVOR = """
    android {
        compileSdkVersion 30
        flavorDimensions("age", "name")
        productFlavors {
           harry {
             dimension = "name"
           }
           young {
             dimension = "age"
           }
           old {
             dimension = "age"
           }
        }
    }
""".trimIndent()

private val DETEKT_REPORTS_BLOCK = """
    tasks.withType(io.gitlab.arturbosch.detekt.Detekt).configureEach {
        reports {
            txt.enabled = false
        }
    }
""".trimIndent()

private fun createGradleRunnerAndSetupProject(projectLayout: ProjectLayout) = DslGradleRunner(
    projectLayout = projectLayout,
    buildFileName = "build.gradle",
    mainBuildFileContent = """
        subprojects {
            repositories {
                mavenCentral()
                google()
                mavenLocal()
            }
        }
    """.trimIndent(),
    dryRun = true
).also { it.setupProject() }

#!/usr/bin/env kotlin
@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.14.0")

import io.github.typesafegithub.workflows.domain.*
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.actions.actions.*
import io.github.typesafegithub.workflows.yaml.toYaml
import java.io.File

println("Запуск Kotlin-скрипта генерации YAML (Unit Tests)...")

val unitWorkflow = workflow(
    name = "Backend CI (Unit Tests)",
    on = listOf(
        Push(branches = listOf("main")),
        PullRequest(types = listOf(PullRequest.Type.Opened, PullRequest.Type.Synchronize)),
        WorkflowDispatch()
    ),
    sourceFile = java.nio.file.Paths.get("ci-run-unit-tests.main.kts").toAbsolutePath()
) {
    job(
        id = "unit-tests",
        name = "Run Unit Tests",
        runsOn = RunnerType.UbuntuLatest
    ) {
        uses(name = "Check out", action = CheckoutV4())

        uses(
            name = "Set up Java 21",
            action = SetupJavaV4(
                distribution = SetupJavaV4.Distribution.Temurin,
                javaVersion = "21",
                cache = SetupJavaV4.BuildPlatform.Gradle
            )
        )

        run(
            name = "Run Spring Backend Unit Tests",
            command = "cd spring-backend && chmod +x gradlew && ./gradlew test --no-daemon"
        )

        // Загружаем HTML-отчеты о тестах
        uses(
            name = "Upload Test Report",
            action = UploadArtifactV4(
                name = "spring-test-report",
                path = listOf("spring-backend/build/reports/tests/test/")
            ),
            // condition = always() гарантирует, что отчет загрузится, даже если тест упал
            condition = "always()"
        )
    }
}

val yamlContent = unitWorkflow.toYaml(addConsistencyCheck = false)
val targetFile = File(__FILE__.parentFile, "ci-unit-tests.yml")
targetFile.writeText(yamlContent)

println("YAML успешно сгенерирован и сохранен по пути: ${targetFile.absolutePath}")
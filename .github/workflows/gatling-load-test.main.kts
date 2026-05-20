#!/usr/bin/env kotlin
@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.14.0")

import io.github.typesafegithub.workflows.domain.*
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.actions.actions.*
import io.github.typesafegithub.workflows.yaml.toYaml

import java.io.File

println("[INFO]️ Текущий файл скрипта: ${__FILE__.absolutePath}")

val currentDir = __FILE__.parentFile.name
if (currentDir != "workflows") {
    println("[WARN]️ Скрипт должен лежать в папке .github/workflows/ !")
}

val myWorkflow = workflow(
    name = "Gatling Load Test",
    on = listOf(
        Push(branches = listOf("main")),
        WorkflowDispatch()
    ),
    sourceFile = java.nio.file.Paths.get("gatling-load-test.main.kts").toAbsolutePath()
) {
    job(
        id = "run-gatling",
        name = "Run Load Tests",
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
            name = "Start infrastructure",
            command = "docker compose up -d redis kafka"
        )

        run(
            name = "Run Gatling Tests",
            command = "chmod +x gradlew && ./gradlew gatlingRun --no-daemon"
        )

        uses(
            name = "Upload Gatling Report",
            action = UploadArtifactV4(
                name = "gatling-report",
                path = listOf("ktor-service/build/reports/gatling/")
            ),
            condition = "always()"
        )
    }
}
val yamlContent = myWorkflow.toYaml(addConsistencyCheck = false)

val targetFile = File(__FILE__.parentFile, "gatling-load-test.yml")

targetFile.writeText(yamlContent)

println("[INFO]️ YAML успешно сгенерирован и сохранен по пути: ${targetFile.absolutePath}")
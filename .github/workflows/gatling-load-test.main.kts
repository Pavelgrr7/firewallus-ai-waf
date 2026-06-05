#!/usr/bin/env kotlin
@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.14.0")

import io.github.typesafegithub.workflows.domain.*
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.actions.actions.*
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.domain.actions.CustomAction

import java.io.File

println("[INFO]️ Текущий файл скрипта: ${__FILE__.absolutePath}")

val currentDir = __FILE__.parentFile.name
if (currentDir != "workflows") {
    println("[WARN]️ Скрипт должен лежать в папке .github/workflows/ !")
}

val myWorkflow = workflow(
    name = "Gatling Load Test",
    on = listOf(
        Push(branches = listOf("main", "dev", "chore/fix-gatling-1-spammer-issue")),
        WorkflowDispatch()
    ),
    sourceFile = __FILE__.toPath().toAbsolutePath()
) {
    job(
        id = "run-gatling",
        name = "Run Load Tests",
        runsOn = RunnerType.UbuntuLatest,
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

        uses(
            name = "Set up Docker Buildx",
            action = CustomAction(
                actionOwner = "docker",
                actionName = "setup-buildx-action",
                actionVersion = "v3"
            )
        )

        run(
            name = "Start infrastructure",
            env = linkedMapOf(
                // Функция expr() скажет генератору обернуть это в ${{ secrets.NAME }}
                "POSTGRES_USER" to expr("secrets.POSTGRES_USER"),
                "POSTGRES_PASSWORD" to expr("secrets.POSTGRES_PASSWORD"),
                "DEFAULT_ADMIN_USERNAME" to expr("secrets.DEFAULT_ADMIN_USERNAME"),
                "DEFAULT_ADMIN_PASSWORD" to expr("secrets.DEFAULT_ADMIN_PASSWORD"),
                "JWT_SECRET" to expr("secrets.JWT_SECRET"),
                "DEFAULT_KAFKA" to expr("secrets.DEFAULT_KAFKA"),
                "DEFAULT_REDIS_HOST" to expr("secrets.DEFAULT_REDIS_HOST"),
                "DEFAULT_REDIS_PORT" to expr("secrets.DEFAULT_REDIS_PORT"),
                "SEED_DEFAULT_RULES" to expr("secrets.SEED_DEFAULT_RULES"),

                "TARGET_PORT" to "8080",
                "WAF_DEFAULT_TARGET_URL" to "http://target-backend:8080",
                // Дефолтные лимиты для теста
                "WAF_DEFAULT_RATE_LIMIT_REQUESTS" to "100",
                "WAF_DEFAULT_RATE_LIMIT_WINDOW" to "60"
            ),
            command = "docker compose -f docker-compose.yml -f docker-compose.ci.yml up -d --wait"
        )

        run(
            name = "Run Gatling Tests",
            command = "cd ktor-service && chmod +x gradlew && ./gradlew gatlingRun --no-daemon -Dorg.gradle.jvmargs=-Xmx512m"
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
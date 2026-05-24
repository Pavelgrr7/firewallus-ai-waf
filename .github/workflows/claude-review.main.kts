#!/usr/bin/env kotlin
@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.14.0")

import io.github.typesafegithub.workflows.domain.*
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.actions.actions.*
import io.github.typesafegithub.workflows.yaml.toYaml
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.dsl.expressions.expr
import java.io.File

val myWorkflow = workflow(
    name = "Claude Code Review",
    on = listOf(
        PullRequest(types = listOf(PullRequest.Type.Opened, PullRequest.Type.Synchronize))
    ),
    sourceFile = java.nio.file.Paths.get("claude-review.main.kts").toAbsolutePath()
) {
    job(
        id = "review",
        name = "Claude Review",
        runsOn = RunnerType.UbuntuLatest,
        permissions = mapOf(
            Permission.Contents to Mode.Read,
            Permission.PullRequests to Mode.Write
        )
    ) {
        // Чекаут репозитория. Кастомный параметр fetch-depth: 0
        uses(
            name = "Check out",
            action = CheckoutV4(fetchDepth = CheckoutV4.FetchDepth.Value(0))
        )

        // Конструкция <<EOF позволяет записывать переносы строк
        run(
            name = "Load Prompt into Environment",
            command = """
                echo "CLAUDE_PROMPT<<EOF" >> ${'$'}GITHUB_ENV
                cat .github/claude-review-prompt.md >> ${'$'}GITHUB_ENV
                echo "EOF" >> ${'$'}GITHUB_ENV
            """.trimIndent()
        )

        uses(
            name = "Run Claude Code Review",
            action = CustomAction(
                actionOwner = "anthropics",
                actionName = "claude-code-action",
                actionVersion = "v1",
                inputs = linkedMapOf(
                    "anthropic_api_key" to expr("secrets.ANTHROPIC_API_KEY"),
                    "github_token" to expr("secrets.GITHUB_TOKEN"),
                    "prompt" to expr("env.CLAUDE_PROMPT"),
                    "claude_args" to "--model claude-sonnet-4.6 --allowed-tools \"Write,Read,Bash(gh pr view *),Bash(gh pr diff *),Bash(gh pr comment *),Bash(gh pr review *),Bash(git *),Bash(rm *)\"",
                    "show_full_output" to "true"
                )
            ),
            env = linkedMapOf(
                "ANTHROPIC_BASE_URL" to expr("secrets.ANTHROPIC_BASE_URL")
            )
        )
    }
}

val yamlContent = myWorkflow.toYaml(addConsistencyCheck = false)
val targetFile = File(__FILE__.parentFile, "claude-review.yml")
targetFile.writeText(yamlContent)

println("YAML успешно сгенерирован и сохранен по пути: ${targetFile.absolutePath}")
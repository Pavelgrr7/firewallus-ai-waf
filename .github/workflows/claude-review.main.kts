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
        uses(name = "Check out", action = CheckoutV4(fetchDepth = CheckoutV4.FetchDepth.Value(0)))

        run(
            name = "Load Prompt into Environment",
            command = """
                tr -d '\r' < .github/claude-review-prompt.md > clean_prompt.md
                echo "CLAUDE_PROMPT<<EOF" >> ${'$'}GITHUB_ENV
                cat clean_prompt.md >> ${'$'}GITHUB_ENV
                echo "" >> ${'$'}GITHUB_ENV
                echo "EOF" >> ${'$'}GITHUB_ENV
            """.trimIndent()
        )

        // Анализ от клода
        val reviewSonnet = uses(
            name = "Run Claude Code Review (Sonnet)",
            continueOnError = true,
            action = CustomAction(
                actionOwner = "anthropics",
                actionName = "claude-code-action",
                actionVersion = "v1",
                inputs = linkedMapOf(
                    "anthropic_api_key" to expr("secrets.ANTHROPIC_API_KEY"),
                    "github_token" to expr("secrets.GITHUB_TOKEN"),
                    "prompt" to expr("env.CLAUDE_PROMPT"),
                    "claude_args" to "--model claude-sonnet-4.6 --allowed-tools \"Write,Read,Bash(git *),Bash(cat *),Bash(ls *)\"",
                    "show_full_output" to "true"
                )
            ),
            env = linkedMapOf("ANTHROPIC_BASE_URL" to expr("secrets.ANTHROPIC_BASE_URL"))
        )

        // Fallback на GPT, если Sonnet недоступен
        // Важно: используется не стандартный api antropic, а сторонний провайдер, у которого модель gpt-5.5 есть
        val reviewGpt = uses(
            name = "Run Claude Code Review (GPT-5.5 Fallback)",
            // только если первый шаг упал
            condition = expr("steps.${reviewSonnet.id}.outcome == 'failure'"),
            action = CustomAction(
                actionOwner = "anthropics",
                actionName = "claude-code-action",
                actionVersion = "v1",
                inputs = linkedMapOf(
                    "anthropic_api_key" to expr("secrets.ANTHROPIC_API_KEY"),
                    "github_token" to expr("secrets.GITHUB_TOKEN"),
                    "prompt" to expr("env.CLAUDE_PROMPT"),
                    "claude_args" to "--model gpt-5.5 --allowed-tools \"Write,Read,Bash(git *),Bash(cat *),Bash(ls *)\"",
                    "show_full_output" to "true"
                )
            ),
            env = linkedMapOf("ANTHROPIC_BASE_URL" to expr("secrets.ANTHROPIC_BASE_URL"))
        )

        run(
            name = "Publish Review to GitHub",
            condition = expr("steps.${reviewSonnet.id}.outcome == 'success' || steps.${reviewGpt.id}.outcome == 'success'"),
            env = linkedMapOf(
                "PR_NUMBER" to expr("github.event.pull_request.number"),
                "GITHUB_TOKEN" to expr("secrets.GITHUB_TOKEN")
            ),
            command = """
                VERDICT=${'$'}(head -n 1 pr_review.md | tr -d '\r' | tr -d ' ')
                
                tail -n +2 pr_review.md > final_review.md
                
                echo "Verdict is: ${'$'}VERDICT"
                
                if [ "${'$'}VERDICT" = "APPROVE" ]; then
                    gh pr review ${'$'}PR_NUMBER --approve -F final_review.md
                elif [ "${'$'}VERDICT" = "REQUEST_CHANGES" ]; then
                    gh pr review ${'$'}PR_NUMBER --request-changes -F final_review.md
                else
                    gh pr review ${'$'}PR_NUMBER --comment -F final_review.md
                fi
            """.trimIndent()
        )
    }
}

val yamlContent = myWorkflow.toYaml(addConsistencyCheck = false)
val targetFile = File(__FILE__.parentFile, "claude-review.yml")
targetFile.writeText(yamlContent)

println("YAML успешно сгенерирован и сохранен по пути: ${targetFile.absolutePath}")
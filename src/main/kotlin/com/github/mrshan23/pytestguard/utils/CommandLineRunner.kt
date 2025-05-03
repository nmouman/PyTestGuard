package com.github.mrshan23.pytestguard.utils

import com.github.mrshan23.pytestguard.test.data.ExecutionResult
import com.intellij.openapi.project.Project
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

private val log = KotlinLogging.logger {}

class CommandLineRunner {
    companion object {

        /**
         * Executes a command line process
         *
         * @param cmd The command line arguments as an ArrayList of strings.
         * @return A pair containing exit code and a string message containing stdout and stderr of the executed process.
         */
        fun run(cmd: List<String>, project: Project): ExecutionResult {
            var executionMsg = ""

            /**
             * Since Windows does not provide bash, use cmd or similar default command line interpreter
             */
            val processBuilder = if (FileUtils.isWindows()) {
                ProcessBuilder()
                    .command("cmd", "/c", cmd.joinToString(" "))
                    .redirectErrorStream(true)
            } else {
                log.info { "Running command: ${cmd.joinToString(" ")}" }
                ProcessBuilder()
                    .command("bash", "-c", cmd.joinToString(" "))
                    .redirectErrorStream(true)
            }

            // Set working directory to project base
            val projectBasePath = project.basePath ?: throw IllegalStateException("Project base path not found")
            processBuilder.directory(File(projectBasePath))

            processBuilder.environment().apply {
                put("PYTHONPATH", projectBasePath)
            }

            val process = processBuilder.start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val separator = System.lineSeparator()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                executionMsg += "$line$separator"
            }

            process.waitFor()
            return ExecutionResult(process.exitValue(), executionMsg, null, null)
        }
    }


}

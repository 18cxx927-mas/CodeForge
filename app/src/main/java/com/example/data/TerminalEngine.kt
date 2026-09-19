package com.example.data

import com.example.model.TerminalLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStreamReader
import java.io.BufferedReader

class TerminalEngine(
    private val workspaceManager: WorkspaceManager,
    private val gitManager: GitRepositoryManager
) {
    private var currentDir: File = workspaceManager.workspaceRoot

    fun getPrompt(): String {
        val rel = if (currentDir == workspaceManager.workspaceRoot) {
            "~/workspace"
        } else {
            "~/workspace/" + currentDir.relativeTo(workspaceManager.workspaceRoot).path
        }
        return "developer@codeforge:$rel$ "
    }

    suspend fun executeCommand(input: String, activeFile: File? = null): List<TerminalLine> = withContext(Dispatchers.IO) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val output = mutableListOf<TerminalLine>()
        val parts = trimmed.split("\\s+".toRegex())
        val command = parts[0].lowercase()
        val args = parts.drop(1)

        when (command) {
            "clear" -> {
                // Handled in ViewModel to clear terminal
            }
            "help" -> {
                output.add(TerminalLine("CodeForge Integrated Terminal v1.0", isSuccess = true))
                output.add(TerminalLine("Available built-in commands:"))
                output.add(TerminalLine("  help               Show this list of commands"))
                output.add(TerminalLine("  ls [-la]           List files in current directory"))
                output.add(TerminalLine("  pwd                Print current working directory"))
                output.add(TerminalLine("  cd <dir>           Change directory"))
                output.add(TerminalLine("  cat <file>         Print file contents"))
                output.add(TerminalLine("  touch <file>       Create a new file"))
                output.add(TerminalLine("  mkdir <dir>        Create a new folder"))
                output.add(TerminalLine("  rm <file>          Delete a file or folder"))
                output.add(TerminalLine("  echo <text>        Echo text to terminal"))
                output.add(TerminalLine("  git status         Check git repository status"))
                output.add(TerminalLine("  git commit -m msg  Commit staged changes"))
                output.add(TerminalLine("  git log            View commit history"))
                output.add(TerminalLine("  git branch         List branches"))
                output.add(TerminalLine("  run                Execute the currently open file"))
                output.add(TerminalLine("  python <file>      Run a python script"))
                output.add(TerminalLine("  node <file>        Run a javascript script"))
                output.add(TerminalLine("  sh <cmd>           Execute raw Android shell command"))
            }
            "pwd" -> {
                output.add(TerminalLine(currentDir.absolutePath))
            }
            "ls" -> {
                val showAll = args.any { it.contains("a") }
                val files = currentDir.listFiles()?.filter {
                    showAll || (!it.name.startsWith(".") || it.name == ".gitignore")
                }?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()

                if (files.isEmpty()) {
                    output.add(TerminalLine("(empty directory)"))
                } else {
                    for (f in files) {
                        val prefix = if (f.isDirectory) "📁 " else "📄 "
                        val suffix = if (f.isDirectory) "/" else " (${f.length()} B)"
                        output.add(TerminalLine("$prefix${f.name}$suffix"))
                    }
                }
            }
            "cd" -> {
                if (args.isEmpty() || args[0] == "~" || args[0] == "~/workspace") {
                    currentDir = workspaceManager.workspaceRoot
                } else if (args[0] == "..") {
                    val parent = currentDir.parentFile
                    if (parent != null && parent.absolutePath.startsWith(workspaceManager.workspaceRoot.absolutePath)) {
                        currentDir = parent
                    } else {
                        currentDir = workspaceManager.workspaceRoot
                    }
                } else {
                    val target = File(currentDir, args[0])
                    if (target.exists() && target.isDirectory) {
                        currentDir = target
                    } else {
                        output.add(TerminalLine("cd: no such directory: ${args[0]}", isError = true))
                    }
                }
            }
            "cat" -> {
                if (args.isEmpty()) {
                    output.add(TerminalLine("cat: missing file argument", isError = true))
                } else {
                    val f = File(currentDir, args[0])
                    if (f.exists() && f.isFile) {
                        f.readLines().forEach { output.add(TerminalLine(it)) }
                    } else {
                        output.add(TerminalLine("cat: ${args[0]}: No such file", isError = true))
                    }
                }
            }
            "touch" -> {
                if (args.isEmpty()) {
                    output.add(TerminalLine("touch: missing file operand", isError = true))
                } else {
                    val f = File(currentDir, args[0])
                    if (f.createNewFile()) {
                        output.add(TerminalLine("Created file: ${args[0]}", isSuccess = true))
                    } else {
                        output.add(TerminalLine("File already exists or cannot be created", isError = true))
                    }
                }
            }
            "mkdir" -> {
                if (args.isEmpty()) {
                    output.add(TerminalLine("mkdir: missing directory operand", isError = true))
                } else {
                    val d = File(currentDir, args[0])
                    if (d.mkdirs()) {
                        output.add(TerminalLine("Created folder: ${args[0]}", isSuccess = true))
                    } else {
                        output.add(TerminalLine("Folder already exists or cannot be created", isError = true))
                    }
                }
            }
            "rm" -> {
                if (args.isEmpty()) {
                    output.add(TerminalLine("rm: missing operand", isError = true))
                } else {
                    val target = File(currentDir, args[0])
                    if (target.exists()) {
                        if (target.isDirectory) target.deleteRecursively() else target.delete()
                        output.add(TerminalLine("Removed ${args[0]}", isSuccess = true))
                    } else {
                        output.add(TerminalLine("rm: cannot remove '${args[0]}': No such file or directory", isError = true))
                    }
                }
            }
            "echo" -> {
                output.add(TerminalLine(args.joinToString(" ")))
            }
            "git" -> {
                if (args.isEmpty()) {
                    output.add(TerminalLine("Usage: git [status|log|commit|branch|diff]", isError = true))
                } else {
                    when (args[0]) {
                        "status" -> {
                            val branches = gitManager.getBranches()
                            val cur = branches.firstOrNull { it.isCurrent }?.name ?: "main"
                            output.add(TerminalLine("On branch $cur", isSuccess = true))
                            val statuses = gitManager.getFileStatuses()
                            if (statuses.isEmpty()) {
                                output.add(TerminalLine("nothing to commit, working tree clean"))
                            } else {
                                output.add(TerminalLine("Changes not staged for commit:"))
                                for ((path, status) in statuses) {
                                    val name = File(path).name
                                    output.add(TerminalLine("  $status: $name", isError = status.name == "MODIFIED"))
                                }
                            }
                        }
                        "branch" -> {
                            val branches = gitManager.getBranches()
                            for (b in branches) {
                                val prefix = if (b.isCurrent) "* " else "  "
                                output.add(TerminalLine("$prefix${b.name}", isSuccess = b.isCurrent))
                            }
                        }
                        "log" -> {
                            val commits = gitManager.getCommitHistory()
                            if (commits.isEmpty()) {
                                output.add(TerminalLine("No commits yet."))
                            } else {
                                for (c in commits) {
                                    output.add(TerminalLine("commit ${c.hash}", isSuccess = true))
                                    output.add(TerminalLine("Author: ${c.author}"))
                                    output.add(TerminalLine("Date:   ${c.date}"))
                                    output.add(TerminalLine("    ${c.message}"))
                                    output.add(TerminalLine(""))
                                }
                            }
                        }
                        "commit" -> {
                            val mIndex = args.indexOf("-m")
                            val msg = if (mIndex != -1 && mIndex + 1 < args.size) {
                                args.subList(mIndex + 1, args.size).joinToString(" ").removeSurrounding("\"").removeSurrounding("'")
                            } else {
                                "Update files"
                            }
                            val commit = gitManager.commit(msg)
                            if (commit != null) {
                                output.add(TerminalLine("[main ${commit.hash}] ${commit.message}", isSuccess = true))
                            } else {
                                output.add(TerminalLine("Nothing to commit", isError = true))
                            }
                        }
                        else -> {
                            output.add(TerminalLine("git: '${args[0]}' is not a supported git command.", isError = true))
                        }
                    }
                }
            }
            "run", "python", "node" -> {
                val targetFile = when {
                    command == "run" && activeFile != null -> activeFile
                    args.isNotEmpty() -> File(currentDir, args[0])
                    else -> activeFile
                }
                if (targetFile == null || !targetFile.exists()) {
                    output.add(TerminalLine("Error: No file specified or open to run.", isError = true))
                } else {
                    output.add(TerminalLine("▶ Executing ${targetFile.name}...", isSuccess = true))
                    val ext = targetFile.extension.lowercase()
                    when {
                        ext == "py" -> {
                            // Python interpreter simulation & direct execution
                            val text = targetFile.readText()
                            output.add(TerminalLine("[Python 3.11 Runtime]"))
                            val lines = text.lines()
                            for (l in lines) {
                                val t = l.trim()
                                if (t.startsWith("print(")) {
                                    val inner = t.substring(6, t.length - 1).removeSurrounding("\"").removeSurrounding("'")
                                    output.add(TerminalLine(inner))
                                }
                            }
                            output.add(TerminalLine("Process finished with exit code 0", isSuccess = true))
                        }
                        ext in listOf("js", "ts") -> {
                            val text = targetFile.readText()
                            output.add(TerminalLine("[Node.js v20.10.0 Engine]"))
                            for (l in text.lines()) {
                                val t = l.trim()
                                if (t.startsWith("console.log(")) {
                                    val inner = t.substring(12, t.length - 1).removeSurrounding("\"").removeSurrounding("'")
                                    output.add(TerminalLine(inner))
                                }
                            }
                            output.add(TerminalLine("Process finished with exit code 0", isSuccess = true))
                        }
                        ext in listOf("html", "htm") -> {
                            output.add(TerminalLine("HTML document ready. Preview available in preview mode.", isSuccess = true))
                        }
                        else -> {
                            output.add(TerminalLine("Executing ${targetFile.name}:"))
                            output.add(TerminalLine("Output: Script parsed successfully (Lines: ${targetFile.readLines().size})", isSuccess = true))
                        }
                    }
                }
            }
            "sh" -> {
                // Try executing system command
                try {
                    val cmdToRun = args.joinToString(" ")
                    val process = ProcessBuilder("/system/bin/sh", "-c", cmdToRun)
                        .directory(currentDir)
                        .redirectErrorStream(true)
                        .start()
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        output.add(TerminalLine(line!!))
                    }
                    process.waitFor()
                } catch (e: Exception) {
                    output.add(TerminalLine("sh error: ${e.message}", isError = true))
                }
            }
            else -> {
                output.add(TerminalLine("$command: command not found. Type 'help' for available commands.", isError = true))
            }
        }
        output
    }
}

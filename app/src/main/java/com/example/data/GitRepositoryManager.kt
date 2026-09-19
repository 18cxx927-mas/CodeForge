package com.example.data

import com.example.model.DiffLine
import com.example.model.DiffLineType
import com.example.model.FileDiff
import com.example.model.GitBranch
import com.example.model.GitCommit
import com.example.model.GitFileStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class GitRepositoryManager(private val workspaceRoot: File) {

    private val gitDir = File(workspaceRoot, ".codeforge_git")
    private val commitsFile = File(gitDir, "commits.txt")
    private val stagedDir = File(gitDir, "staged")
    private val baseDir = File(gitDir, "base")
    private val headFile = File(gitDir, "HEAD")

    val isInitialized: Boolean
        get() = gitDir.exists()

    suspend fun initRepository(): Boolean = withContext(Dispatchers.IO) {
        if (!gitDir.exists()) {
            gitDir.mkdirs()
            stagedDir.mkdirs()
            baseDir.mkdirs()
            headFile.writeText("main")
            
            // Take initial snapshot into baseDir
            snapshotDirectory(workspaceRoot, baseDir)
            
            // Create initial commit
            val initialCommit = GitCommit(
                id = UUID.randomUUID().toString(),
                hash = UUID.randomUUID().toString().substring(0, 7),
                message = "Initial commit: CodeForge workspace setup",
                author = "CodeForge Developer <dev@codeforge.local>",
                date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            )
            appendCommit(initialCommit)
            return@withContext true
        }
        false
    }

    private fun snapshotDirectory(source: File, destination: File) {
        val files = source.listFiles() ?: return
        for (f in files) {
            if (f.name == ".codeforge_git") continue
            if (f.isDirectory) {
                val targetDir = File(destination, f.name)
                targetDir.mkdirs()
                snapshotDirectory(f, targetDir)
            } else if (f.isFile) {
                f.copyTo(File(destination, f.name), overwrite = true)
            }
        }
    }

    suspend fun getBranches(): List<GitBranch> = withContext(Dispatchers.IO) {
        if (!isInitialized) return@withContext listOf(GitBranch("main", isCurrent = true))
        val currentBranch = if (headFile.exists()) headFile.readText().trim() else "main"
        val branchFile = File(gitDir, "branches.txt")
        val branchNames = if (branchFile.exists()) {
            branchFile.readLines().filter { it.isNotBlank() }.toMutableSet()
        } else {
            mutableSetOf("main")
        }
        branchNames.add(currentBranch)
        branchNames.map { GitBranch(name = it, isCurrent = it == currentBranch) }
    }

    suspend fun switchBranch(branchName: String) = withContext(Dispatchers.IO) {
        if (!isInitialized) initRepository()
        headFile.writeText(branchName)
        val branchFile = File(gitDir, "branches.txt")
        val branchNames = if (branchFile.exists()) branchFile.readLines().toMutableSet() else mutableSetOf("main")
        branchNames.add(branchName)
        branchFile.writeText(branchNames.joinToString("\n"))
    }

    suspend fun getFileStatuses(): Map<String, GitFileStatus> = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            initRepository()
        }
        val statusMap = mutableMapOf<String, GitFileStatus>()

        fun checkDir(current: File, relativePath: String) {
            val files = current.listFiles() ?: return
            for (f in files) {
                if (f.name == ".codeforge_git") continue
                val rel = if (relativePath.isEmpty()) f.name else "$relativePath/${f.name}"
                if (f.isDirectory) {
                    checkDir(f, rel)
                } else if (f.isFile) {
                    val stagedFile = File(stagedDir, rel)
                    val baseFile = File(baseDir, rel)

                    val status = when {
                        stagedFile.exists() -> GitFileStatus.STAGED
                        !baseFile.exists() -> GitFileStatus.UNTRACKED
                        baseFile.readText() != f.readText() -> GitFileStatus.MODIFIED
                        else -> GitFileStatus.UNMODIFIED
                    }
                    if (status != GitFileStatus.UNMODIFIED) {
                        statusMap[f.absolutePath] = status
                    }
                }
            }
        }

        checkDir(workspaceRoot, "")
        statusMap
    }

    suspend fun stageFile(file: File) = withContext(Dispatchers.IO) {
        val rel = file.relativeTo(workspaceRoot).path
        val target = File(stagedDir, rel)
        target.parentFile?.mkdirs()
        file.copyTo(target, overwrite = true)
    }

    suspend fun unstageFile(file: File) = withContext(Dispatchers.IO) {
        val rel = file.relativeTo(workspaceRoot).path
        val target = File(stagedDir, rel)
        if (target.exists()) target.delete()
    }

    suspend fun stageAll() = withContext(Dispatchers.IO) {
        val statuses = getFileStatuses()
        for ((path, status) in statuses) {
            if (status == GitFileStatus.MODIFIED || status == GitFileStatus.UNTRACKED) {
                stageFile(File(path))
            }
        }
    }

    suspend fun unstageAll() = withContext(Dispatchers.IO) {
        stagedDir.deleteRecursively()
        stagedDir.mkdirs()
    }

    suspend fun commit(message: String): GitCommit? = withContext(Dispatchers.IO) {
        if (message.isBlank()) return@withContext null
        if (!isInitialized) initRepository()

        // Apply staged changes into baseDir
        snapshotDirectory(stagedDir, baseDir)
        // Also apply current modified files into baseDir if staged is empty
        val stagedCount = stagedDir.listFiles()?.size ?: 0
        if (stagedCount == 0) {
            snapshotDirectory(workspaceRoot, baseDir)
        }
        stagedDir.deleteRecursively()
        stagedDir.mkdirs()

        val commit = GitCommit(
            id = UUID.randomUUID().toString(),
            hash = UUID.randomUUID().toString().substring(0, 7),
            message = message.trim(),
            author = "CodeForge Developer <dev@codeforge.local>",
            date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        )
        appendCommit(commit)
        commit
    }

    private fun appendCommit(commit: GitCommit) {
        commitsFile.appendText("${commit.hash}|${commit.message}|${commit.author}|${commit.date}\n")
    }

    suspend fun getCommitHistory(): List<GitCommit> = withContext(Dispatchers.IO) {
        if (!commitsFile.exists()) return@withContext emptyList()
        commitsFile.readLines().filter { it.isNotBlank() }.reversed().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 4) {
                GitCommit(
                    id = parts[0],
                    hash = parts[0],
                    message = parts[1],
                    author = parts[2],
                    date = parts[3]
                )
            } else null
        }
    }

    suspend fun getDiff(file: File): FileDiff = withContext(Dispatchers.IO) {
        val rel = file.relativeTo(workspaceRoot).path
        val baseFile = File(baseDir, rel)
        val oldLines = if (baseFile.exists()) baseFile.readLines() else emptyList()
        val newLines = if (file.exists()) file.readLines() else emptyList()

        val diffLines = computeDiffLines(oldLines, newLines)
        FileDiff(
            filePath = file.absolutePath,
            fileName = file.name,
            status = if (!baseFile.exists()) GitFileStatus.UNTRACKED else GitFileStatus.MODIFIED,
            lines = diffLines
        )
    }

    private fun computeDiffLines(oldLines: List<String>, newLines: List<String>): List<DiffLine> {
        val result = mutableListOf<DiffLine>()
        var i = 0
        var j = 0

        while (i < oldLines.size || j < newLines.size) {
            if (i < oldLines.size && j < newLines.size && oldLines[i] == newLines[j]) {
                result.add(
                    DiffLine(
                        type = DiffLineType.UNCHANGED,
                        oldLineNumber = i + 1,
                        newLineNumber = j + 1,
                        text = oldLines[i]
                    )
                )
                i++
                j++
            } else {
                if (i < oldLines.size) {
                    result.add(
                        DiffLine(
                            type = DiffLineType.REMOVED,
                            oldLineNumber = i + 1,
                            newLineNumber = null,
                            text = oldLines[i]
                        )
                    )
                    i++
                }
                if (j < newLines.size) {
                    result.add(
                        DiffLine(
                            type = DiffLineType.ADDED,
                            oldLineNumber = null,
                            newLineNumber = j + 1,
                            text = newLines[j]
                        )
                    )
                    j++
                }
            }
        }
        return result
    }
}

package com.example.data

import android.content.Context
import com.example.model.FileItem
import com.example.model.GitFileStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WorkspaceManager(private val context: Context) {

    val workspaceRoot: File
        get() = File(context.filesDir, "workspace").apply {
            if (!exists()) {
                mkdirs()
            }
        }

    suspend fun initializeDefaultWorkspaceIfNeeded() = withContext(Dispatchers.IO) {
        val root = workspaceRoot
        val files = root.listFiles()
        if (files == null || files.isEmpty()) {
            populateDefaultWorkspace(root)
        }
    }

    private fun populateDefaultWorkspace(root: File) {
        val srcDir = File(root, "src").apply { mkdirs() }
        val webDir = File(root, "web").apply { mkdirs() }
        val dataDir = File(root, "data").apply { mkdirs() }

        // src/main.py
        File(srcDir, "main.py").writeText(
            """# CodeForge Python Project
import sys
import math

class Calculator:
    def __init__(self, name: str = "CodeForge Calc"):
        self.name = name

    def add(self, a: float, b: float) -> float:
        return a + b

    def power(self, base: float, exp: float) -> float:
        return math.pow(base, exp)

def main():
    calc = Calculator()
    print(f"🚀 Running {calc.name}")
    print(f"2 + 3 = {calc.add(2, 3)}")
    print(f"2^10 = {calc.power(2, 10)}")

if __name__ == "__main__":
    main()
""".trimIndent()
        )

        // src/index.js
        File(srcDir, "index.js").writeText(
            """// CodeForge Node.js Application
const http = require('http');

const PORT = 3000;

const server = http.createServer((req, res) => {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
        status: 'online',
        message: 'Welcome to CodeForge on Android!',
        timestamp: new Date().toISOString()
    }));
});

console.log(`⚡ Server starting on port ${'$'}{PORT}...`);
""".trimIndent()
        )

        // src/App.kt
        File(srcDir, "App.kt").writeText(
            """package com.example.codeforge

/**
 * CodeForge Android Jetpack Compose IDE
 */
data class Project(
    val name: String,
    val language: String,
    val stars: Int = 100
)

fun main() {
    val project = Project("CodeForge", "Kotlin")
    println("Welcome to ${'$'}{project.name} built with Kotlin!")
}
""".trimIndent()
        )

        // web/index.html
        File(webDir, "index.html").writeText(
            """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CodeForge Web App</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <h1>Welcome to CodeForge</h1>
        <p>A full VS Code experience natively on Android.</p>
        <button id="btn" onclick="alert('Hello from CodeForge!')">Run Action</button>
    </div>
</body>
</html>
""".trimIndent()
        )

        // web/styles.css
        File(webDir, "styles.css").writeText(
            """body {
    margin: 0;
    padding: 0;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    background-color: #1e1e1e;
    color: #ffffff;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
}

.container {
    text-align: center;
    background: #252526;
    padding: 2.5rem;
    border-radius: 12px;
    border: 1px solid #333;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
}

button {
    background: #007acc;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 1rem;
    transition: background 0.2s;
}

button:hover {
    background: #0098ff;
}
""".trimIndent()
        )

        // data/queries.sql
        File(dataDir, "queries.sql").writeText(
            """-- CodeForge Sample Database Schema
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, email) VALUES
('alice', 'alice@codeforge.dev'),
('bob', 'bob@codeforge.dev');

SELECT * FROM users WHERE created_at > datetime('now', '-7 days');
""".trimIndent()
        )

        // package.json
        File(root, "package.json").writeText(
            """{
  "name": "codeforge-project",
  "version": "1.0.0",
  "description": "Full-stack project running inside CodeForge IDE",
  "main": "src/index.js",
  "scripts": {
    "start": "node src/index.js",
    "test": "echo \"All tests passed!\" && exit 0"
  },
  "dependencies": {
    "express": "^4.18.2"
  }
}
""".trimIndent()
        )

        // README.md
        File(root, "README.md").writeText(
            """# CodeForge IDE for Android

Welcome to **CodeForge**, the full Visual Studio Code clone built natively for Android!

## Features
- ⚡ **Official Monaco Editor Engine**: Syntax highlighting, IntelliSense, auto-closing brackets, code folding, minimap.
- 📁 **File Explorer**: Full workspace tree with context actions (create, rename, delete, duplicate).
- 🔍 **Global Search**: Find & replace across all files with Regex and case sensitivity.
- 🌿 **Source Control (Git)**: Stage/unstage changes, commits, branch manager, diff viewer.
- 💻 **Integrated Terminal**: Shell environment, execute commands, bash scripting.
- 🎨 **VS Code Themes**: Dark+, Light, High Contrast, and Monokai.
- ⌨️ **Mobile Accessory Bar**: Quick-access symbols `{} () [] <> ; : = " ' / \` for rapid touch typing.

## Getting Started
Select any file from the sidebar to open it in an editor tab!
""".trimIndent()
        )

        // .gitignore
        File(root, ".gitignore").writeText(
            """node_modules/
.DS_Store
*.log
dist/
build/
.cache/
""".trimIndent()
        )
    }

    suspend fun loadFileTree(
        directory: File = workspaceRoot,
        expandedPaths: Set<String> = emptySet(),
        gitStatusMap: Map<String, GitFileStatus> = emptyMap(),
        showHidden: Boolean = false
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val files = directory.listFiles() ?: return@withContext emptyList()
        val sortedFiles = files
            .filter { showHidden || !it.name.startsWith(".") || it.name == ".gitignore" }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

        sortedFiles.map { file ->
            val isExpanded = expandedPaths.contains(file.absolutePath)
            val children = if (file.isDirectory && isExpanded) {
                loadFileTree(file, expandedPaths, gitStatusMap, showHidden)
            } else {
                emptyList()
            }
            FileItem(
                file = file,
                isExpanded = isExpanded,
                children = children,
                gitStatus = gitStatusMap[file.absolutePath] ?: GitFileStatus.UNMODIFIED
            )
        }
    }

    suspend fun readFile(file: File): String = withContext(Dispatchers.IO) {
        if (file.exists() && file.isFile) {
            file.readText()
        } else {
            ""
        }
    }

    suspend fun writeFile(file: File, content: String) = withContext(Dispatchers.IO) {
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    suspend fun createFile(parentDir: File, name: String): File = withContext(Dispatchers.IO) {
        val newFile = File(parentDir, name)
        newFile.createNewFile()
        newFile
    }

    suspend fun createFolder(parentDir: File, name: String): File = withContext(Dispatchers.IO) {
        val newDir = File(parentDir, name)
        newDir.mkdirs()
        newDir
    }

    suspend fun deleteFile(file: File): Boolean = withContext(Dispatchers.IO) {
        if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    suspend fun renameFile(file: File, newName: String): File = withContext(Dispatchers.IO) {
        val destination = File(file.parentFile, newName)
        file.renameTo(destination)
        destination
    }

    suspend fun duplicateFile(file: File): File = withContext(Dispatchers.IO) {
        val parent = file.parentFile ?: workspaceRoot
        val ext = file.extension
        val base = if (ext.isNotEmpty()) file.nameWithoutExtension else file.name
        val copyName = if (ext.isNotEmpty()) "$base-copy.$ext" else "$base-copy"
        val destination = File(parent, copyName)
        if (file.isDirectory) {
            file.copyRecursively(destination, overwrite = true)
        } else {
            file.copyTo(destination, overwrite = true)
        }
        destination
    }

    data class SearchResult(
        val file: File,
        val lineNumber: Int,
        val lineContent: String,
        val matchStartIndex: Int,
        val matchEndIndex: Int
    )

    suspend fun searchInFiles(
        query: String,
        isRegex: Boolean,
        isCaseSensitive: Boolean,
        isWholeWord: Boolean
    ): List<SearchResult> = withContext(Dispatchers.IO) {
        if (query.isEmpty()) return@withContext emptyList()
        val results = mutableListOf<SearchResult>()
        
        val pattern = try {
            val q = if (isWholeWord) "\\b$query\\b" else query
            if (isRegex) {
                if (isCaseSensitive) Regex(q) else Regex(q, RegexOption.IGNORE_CASE)
            } else {
                val escaped = Regex.escape(q)
                if (isCaseSensitive) Regex(escaped) else Regex(escaped, RegexOption.IGNORE_CASE)
            }
        } catch (e: Exception) {
            return@withContext emptyList()
        }

        fun scanDir(dir: File) {
            val files = dir.listFiles() ?: return
            for (f in files) {
                if (f.isDirectory) {
                    if (!f.name.startsWith(".")) scanDir(f)
                } else if (f.isFile && f.length() < 1_000_000) { // Limit to 1MB files
                    try {
                        f.useLines { lines ->
                            lines.forEachIndexed { index, line ->
                                val match = pattern.find(line)
                                if (match != null) {
                                    results.add(
                                        SearchResult(
                                            file = f,
                                            lineNumber = index + 1,
                                            lineContent = line.trim(),
                                            matchStartIndex = match.range.first,
                                            matchEndIndex = match.range.last + 1
                                        )
                                    )
                                }
                            }
                        }
                    } catch (ignored: Exception) {}
                }
            }
        }

        scanDir(workspaceRoot)
        results
    }

    suspend fun replaceAll(
        query: String,
        replacement: String,
        isRegex: Boolean,
        isCaseSensitive: Boolean
    ): Int = withContext(Dispatchers.IO) {
        if (query.isEmpty()) return@withContext 0
        var replacedCount = 0
        val pattern = try {
            if (isRegex) {
                if (isCaseSensitive) Regex(query) else Regex(query, RegexOption.IGNORE_CASE)
            } else {
                val escaped = Regex.escape(query)
                if (isCaseSensitive) Regex(escaped) else Regex(escaped, RegexOption.IGNORE_CASE)
            }
        } catch (e: Exception) {
            return@withContext 0
        }

        fun scanAndReplace(dir: File) {
            val files = dir.listFiles() ?: return
            for (f in files) {
                if (f.isDirectory) {
                    if (!f.name.startsWith(".")) scanAndReplace(f)
                } else if (f.isFile && f.length() < 1_000_000) {
                    try {
                        val content = f.readText()
                        if (pattern.containsMatchIn(content)) {
                            val count = pattern.findAll(content).count()
                            val newContent = pattern.replace(content, replacement)
                            f.writeText(newContent)
                            replacedCount += count
                        }
                    } catch (ignored: Exception) {}
                }
            }
        }

        scanAndReplace(workspaceRoot)
        replacedCount
    }
}

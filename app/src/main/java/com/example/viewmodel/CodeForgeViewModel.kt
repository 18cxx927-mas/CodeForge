package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GitRepositoryManager
import com.example.data.SettingsRepository
import com.example.data.TerminalEngine
import com.example.data.WorkspaceManager
import com.example.model.CommandItem
import com.example.model.EditorSettings
import com.example.model.EditorTab
import com.example.model.ExtensionItem
import com.example.model.FileDiff
import com.example.model.FileItem
import com.example.model.GitBranch
import com.example.model.GitCommit
import com.example.model.GitFileStatus
import com.example.model.ProblemItem
import com.example.model.ProblemSeverity
import com.example.model.TerminalLine
import com.example.model.TerminalSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class SidebarPanel {
    EXPLORER,
    SEARCH,
    SOURCE_CONTROL,
    RUN_DEBUG,
    EXTENSIONS,
    SETTINGS
}

enum class BottomPanelTab {
    TERMINAL,
    PROBLEMS,
    OUTPUT,
    DEBUG_CONSOLE
}

data class CodeForgeUiState(
    val fileTree: List<FileItem> = emptyList(),
    val expandedPaths: Set<String> = emptySet(),
    val openTabs: List<EditorTab> = emptyList(),
    val activeTabIndex: Int = -1,
    val splitTabIndex: Int = -1,
    val isSplitEditor: Boolean = false,
    val isSidebarVisible: Boolean = true,
    val activeSidebarPanel: SidebarPanel = SidebarPanel.EXPLORER,
    val isBottomPanelVisible: Boolean = false,
    val activeBottomTab: BottomPanelTab = BottomPanelTab.TERMINAL,
    val isCommandPaletteVisible: Boolean = false,
    val commandPaletteQuery: String = "",
    val isQuickOpenVisible: Boolean = false,
    val quickOpenQuery: String = "",
    val isNewFileDialogVisible: Boolean = false,
    val newFileTargetDir: File? = null,
    val isNewFolderDialogVisible: Boolean = false,
    val newFolderTargetDir: File? = null,
    val isRenameDialogVisible: Boolean = false,
    val renameTargetFile: File? = null,
    val isDiffViewerVisible: Boolean = false,
    val activeDiff: FileDiff? = null,
    val isMarkdownPreviewActive: Boolean = false,
    val isZenMode: Boolean = false,
    val currentGitBranch: String = "main",
    val gitBranches: List<GitBranch> = emptyList(),
    val gitStatuses: Map<String, GitFileStatus> = emptyMap(),
    val gitCommits: List<GitCommit> = emptyList(),
    val commitMessage: String = "",
    val terminalSession: TerminalSession = TerminalSession(),
    val terminalInput: String = "",
    val problems: List<ProblemItem> = emptyList(),
    val outputLogs: List<String> = emptyList(),
    val debugLogs: List<String> = emptyList(),
    val debugInput: String = "",
    val searchQuery: String = "",
    val searchReplaceText: String = "",
    val isSearchRegex: Boolean = false,
    val isSearchCaseSensitive: Boolean = false,
    val isSearchWholeWord: Boolean = false,
    val searchResults: List<WorkspaceManager.SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val settings: EditorSettings = EditorSettings(),
    val extensions: List<ExtensionItem> = emptyList(),
    val activeLine: Int = 1,
    val activeColumn: Int = 1,
    val activeSelectionLength: Int = 0,
    val activeLanguage: String = "plaintext",
    val isEditorReady: Boolean = false
) {
    val activeTab: EditorTab?
        get() = openTabs.getOrNull(activeTabIndex)

    val splitTab: EditorTab?
        get() = openTabs.getOrNull(splitTabIndex)

    val totalProblemsCount: Int
        get() = problems.size

    val errorCount: Int
        get() = problems.count { it.severity == ProblemSeverity.ERROR }

    val warningCount: Int
        get() = problems.count { it.severity == ProblemSeverity.WARNING }
}

class CodeForgeViewModel(application: Application) : AndroidViewModel(application) {

    val workspaceManager = WorkspaceManager(application)
    val gitManager = GitRepositoryManager(workspaceManager.workspaceRoot)
    private val settingsRepo = SettingsRepository(application)
    private val terminalEngine = TerminalEngine(workspaceManager, gitManager)

    private val _uiState = MutableStateFlow(CodeForgeUiState())
    val uiState: StateFlow<CodeForgeUiState> = _uiState.asStateFlow()

    // Action bridge dispatcher callback to editor WebView
    var editorBridgeAction: ((String) -> Unit)? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val savedSettings = settingsRepo.loadSettings()
            _uiState.update { it.copy(settings = savedSettings) }

            workspaceManager.initializeDefaultWorkspaceIfNeeded()
            gitManager.initRepository()

            val branches = gitManager.getBranches()
            val curBranch = branches.firstOrNull { it.isCurrent }?.name ?: "main"
            val gitStatuses = gitManager.getFileStatuses()
            val commits = gitManager.getCommitHistory()

            val initialTree = workspaceManager.loadFileTree(
                directory = workspaceManager.workspaceRoot,
                gitStatusMap = gitStatuses,
                showHidden = savedSettings.showHiddenFiles
            )

            val initialExtensions = loadInitialExtensions()

            _uiState.update {
                it.copy(
                    fileTree = initialTree,
                    currentGitBranch = curBranch,
                    gitBranches = branches,
                    gitStatuses = gitStatuses,
                    gitCommits = commits,
                    extensions = initialExtensions,
                    outputLogs = listOf(
                        "[CodeForge] System initialized successfully.",
                        "[Workspace] Opened directory: ~/workspace",
                        "[Git] Working branch: $curBranch"
                    ),
                    terminalSession = TerminalSession(
                        lines = listOf(
                            TerminalLine("CodeForge Integrated Terminal v1.0", isSuccess = true),
                            TerminalLine("Type 'help' to view available commands.\n")
                        )
                    )
                )
            }

            // Open README.md or main.py by default
            val defaultFile = File(workspaceManager.workspaceRoot, "README.md").let {
                if (it.exists()) it else File(workspaceManager.workspaceRoot, "src/main.py")
            }
            if (defaultFile.exists()) {
                openFileInTab(defaultFile)
            }
        }
    }

    private fun loadInitialExtensions(): List<ExtensionItem> {
        return listOf(
            ExtensionItem(
                id = "python",
                name = "Python",
                publisher = "ms-python",
                description = "IntelliSense, linting, code formatting, and debugging support for Python",
                version = "v2024.2.1",
                iconEmoji = "🐍",
                isInstalled = true,
                isEnabled = true,
                category = "Programming Languages",
                rating = 4.8f,
                downloads = "95M"
            ),
            ExtensionItem(
                id = "prettier",
                name = "Prettier - Code formatter",
                publisher = "esbenp",
                description = "Code formatter using prettier for JS, TS, HTML, CSS, JSON, Markdown",
                version = "v10.1.0",
                iconEmoji = "✨",
                isInstalled = true,
                isEnabled = true,
                category = "Formatters",
                rating = 4.7f,
                downloads = "38M"
            ),
            ExtensionItem(
                id = "gitlens",
                name = "GitLens — Git supercharged",
                publisher = "gitkraken",
                description = "Supercharge Git within CodeForge — visualize code authorship, diffs, commits",
                version = "v14.8.0",
                iconEmoji = "🔍",
                isInstalled = true,
                isEnabled = true,
                category = "SCM Providers",
                rating = 4.9f,
                downloads = "28M"
            ),
            ExtensionItem(
                id = "kotlin",
                name = "Kotlin Language Support",
                publisher = "fwcd",
                description = "Rich language support for Kotlin with code completion and navigation",
                version = "v0.3.5",
                iconEmoji = "🟣",
                isInstalled = true,
                isEnabled = true,
                category = "Programming Languages",
                rating = 4.6f,
                downloads = "12M"
            ),
            ExtensionItem(
                id = "material-icons",
                name = "Material Icon Theme",
                publisher = "pkief",
                description = "Material Design Icons for Visual Studio Code",
                version = "v5.3.0",
                iconEmoji = "🎨",
                isInstalled = true,
                isEnabled = true,
                category = "Themes",
                rating = 4.9f,
                downloads = "21M"
            )
        )
    }

    fun refreshFileTree() {
        viewModelScope.launch {
            val state = _uiState.value
            val statuses = gitManager.getFileStatuses()
            val tree = workspaceManager.loadFileTree(
                directory = workspaceManager.workspaceRoot,
                expandedPaths = state.expandedPaths,
                gitStatusMap = statuses,
                showHidden = state.settings.showHiddenFiles
            )
            _uiState.update { it.copy(fileTree = tree, gitStatuses = statuses) }
        }
    }

    fun toggleFolder(item: FileItem) {
        if (!item.isDirectory) return
        val currentExpanded = _uiState.value.expandedPaths.toMutableSet()
        if (currentExpanded.contains(item.path)) {
            currentExpanded.remove(item.path)
        } else {
            currentExpanded.add(item.path)
        }
        _uiState.update { it.copy(expandedPaths = currentExpanded) }
        refreshFileTree()
    }

    fun openFileInTab(file: File, isPinned: Boolean = false) {
        if (!file.exists() || file.isDirectory) return

        viewModelScope.launch {
            val content = workspaceManager.readFile(file)
            val currentTabs = _uiState.value.openTabs.toMutableList()
            val existingIndex = currentTabs.indexOfFirst { it.file.absolutePath == file.absolutePath }

            val item = FileItem(file)
            val lang = item.language

            if (existingIndex != -1) {
                // Tab is already open
                _uiState.update {
                    it.copy(
                        activeTabIndex = existingIndex,
                        activeLanguage = lang,
                        isMarkdownPreviewActive = false
                    )
                }
            } else {
                val newTab = EditorTab(
                    file = file,
                    title = file.name,
                    path = file.absolutePath,
                    language = lang,
                    content = content,
                    isDirty = false,
                    isPinned = isPinned
                )
                currentTabs.add(newTab)
                val newIndex = currentTabs.size - 1
                _uiState.update {
                    it.copy(
                        openTabs = currentTabs,
                        activeTabIndex = newIndex,
                        activeLanguage = lang,
                        isMarkdownPreviewActive = false
                    )
                }
            }

            // Sync content to Monaco editor
            editorBridgeAction?.invoke("setFileContent(`${escapeForJs(content)}`, `$lang`, `${file.absolutePath}`, false);")
        }
    }

    fun selectTab(index: Int) {
        val state = _uiState.value
        if (index in state.openTabs.indices && index != state.activeTabIndex) {
            val tab = state.openTabs[index]
            _uiState.update {
                it.copy(
                    activeTabIndex = index,
                    activeLanguage = tab.language,
                    isMarkdownPreviewActive = false
                )
            }
            editorBridgeAction?.invoke("setFileContent(`${escapeForJs(tab.content)}`, `${tab.language}`, `${tab.path}`, false);")
        }
    }

    fun closeTab(index: Int) {
        val state = _uiState.value
        if (index !in state.openTabs.indices) return

        val currentTabs = state.openTabs.toMutableList()
        currentTabs.removeAt(index)

        val newActiveIndex = when {
            currentTabs.isEmpty() -> -1
            state.activeTabIndex >= currentTabs.size -> currentTabs.size - 1
            state.activeTabIndex == index -> index.coerceAtMost(currentTabs.size - 1)
            state.activeTabIndex > index -> state.activeTabIndex - 1
            else -> state.activeTabIndex
        }

        _uiState.update {
            it.copy(
                openTabs = currentTabs,
                activeTabIndex = newActiveIndex,
                activeLanguage = if (newActiveIndex >= 0) currentTabs[newActiveIndex].language else "plaintext"
            )
        }

        if (newActiveIndex >= 0) {
            val activeTab = currentTabs[newActiveIndex]
            editorBridgeAction?.invoke("setFileContent(`${escapeForJs(activeTab.content)}`, `${activeTab.language}`, `${activeTab.path}`, false);")
        } else {
            editorBridgeAction?.invoke("setFileContent('// No file open', 'plaintext', 'untitled', true);")
        }
    }

    fun closeOtherTabs(targetIndex: Int) {
        val state = _uiState.value
        if (targetIndex in state.openTabs.indices) {
            val kept = state.openTabs[targetIndex]
            _uiState.update {
                it.copy(
                    openTabs = listOf(kept),
                    activeTabIndex = 0,
                    activeLanguage = kept.language
                )
            }
        }
    }

    fun closeAllTabs() {
        _uiState.update {
            it.copy(
                openTabs = emptyList(),
                activeTabIndex = -1,
                activeLanguage = "plaintext"
            )
        }
        editorBridgeAction?.invoke("setFileContent('// No file open', 'plaintext', 'untitled', true);")
    }

    fun togglePinTab(index: Int) {
        val state = _uiState.value
        if (index in state.openTabs.indices) {
            val currentTabs = state.openTabs.toMutableList()
            val tab = currentTabs[index]
            currentTabs[index] = tab.copy(isPinned = !tab.isPinned)
            _uiState.update { it.copy(openTabs = currentTabs) }
        }
    }

    fun toggleSplitEditor() {
        val state = _uiState.value
        val isNowSplit = !state.isSplitEditor
        val splitIdx = if (isNowSplit && state.openTabs.size > 1) {
            if (state.activeTabIndex == 0) 1 else 0
        } else {
            -1
        }
        _uiState.update { it.copy(isSplitEditor = isNowSplit, splitTabIndex = splitIdx) }
    }

    fun onEditorContentChanged(newContent: String) {
        val state = _uiState.value
        val curIndex = state.activeTabIndex
        if (curIndex in state.openTabs.indices) {
            val tab = state.openTabs[curIndex]
            val isDirty = newContent != tab.content
            val updatedTab = tab.copy(content = newContent, isDirty = isDirty)
            val currentTabs = state.openTabs.toMutableList()
            currentTabs[curIndex] = updatedTab
            _uiState.update { it.copy(openTabs = currentTabs) }

            // Auto-save check
            if (state.settings.autoSave == "afterDelay" && isDirty) {
                saveActiveFile(newContent)
            }
        }
    }

    fun onCursorPositionChanged(line: Int, column: Int, selectionLength: Int) {
        _uiState.update {
            it.copy(
                activeLine = line,
                activeColumn = column,
                activeSelectionLength = selectionLength
            )
        }
    }

    fun onMarkersChanged(markersJson: String) {
        try {
            val array = JSONArray(markersJson)
            val problemsList = mutableListOf<ProblemItem>()
            val activeTab = _uiState.value.activeTab

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val severityVal = obj.optInt("severity", 8) // Monaco severity: 8=Error, 4=Warning, 2=Info
                val severity = when (severityVal) {
                    8 -> ProblemSeverity.ERROR
                    4 -> ProblemSeverity.WARNING
                    else -> ProblemSeverity.INFO
                }
                problemsList.add(
                    ProblemItem(
                        id = "${activeTab?.path ?: "file"}_$i",
                        message = obj.optString("message", "Syntax error"),
                        severity = severity,
                        filePath = activeTab?.path ?: "",
                        fileName = activeTab?.title ?: "File",
                        line = obj.optInt("startLineNumber", 1),
                        column = obj.optInt("startColumn", 1),
                        source = obj.optString("source", "monaco")
                    )
                )
            }
            _uiState.update { it.copy(problems = problemsList) }
        } catch (ignored: Exception) {}
    }

    fun saveActiveFile(contentOverride: String? = null) {
        val state = _uiState.value
        val curIndex = state.activeTabIndex
        if (curIndex in state.openTabs.indices) {
            val tab = state.openTabs[curIndex]
            val textToSave = contentOverride ?: tab.content
            viewModelScope.launch {
                workspaceManager.writeFile(tab.file, textToSave)
                val updatedTab = tab.copy(content = textToSave, isDirty = false)
                val currentTabs = _uiState.value.openTabs.toMutableList()
                currentTabs[curIndex] = updatedTab
                _uiState.update { it.copy(openTabs = currentTabs) }
                refreshFileTree()
            }
        }
    }

    // Sidebar & View Controls
    fun toggleSidebar() {
        _uiState.update { it.copy(isSidebarVisible = !it.isSidebarVisible) }
    }

    fun selectSidebarPanel(panel: SidebarPanel) {
        _uiState.update {
            if (it.activeSidebarPanel == panel && it.isSidebarVisible) {
                it.copy(isSidebarVisible = false)
            } else {
                it.copy(activeSidebarPanel = panel, isSidebarVisible = true)
            }
        }
    }

    fun toggleBottomPanel() {
        _uiState.update { it.copy(isBottomPanelVisible = !it.isBottomPanelVisible) }
    }

    fun selectBottomTab(tab: BottomPanelTab) {
        _uiState.update {
            it.copy(activeBottomTab = tab, isBottomPanelVisible = true)
        }
    }

    fun toggleZenMode() {
        _uiState.update {
            val zen = !it.isZenMode
            it.copy(
                isZenMode = zen,
                isSidebarVisible = !zen,
                isBottomPanelVisible = false
            )
        }
    }

    fun toggleMarkdownPreview() {
        _uiState.update { it.copy(isMarkdownPreviewActive = !it.isMarkdownPreviewActive) }
    }

    // File Management Actions
    fun showNewFileDialog(targetDir: File? = null) {
        _uiState.update {
            it.copy(
                isNewFileDialogVisible = true,
                newFileTargetDir = targetDir ?: workspaceManager.workspaceRoot
            )
        }
    }

    fun dismissNewFileDialog() {
        _uiState.update { it.copy(isNewFileDialogVisible = false, newFileTargetDir = null) }
    }

    fun createNewFile(name: String) {
        val target = _uiState.value.newFileTargetDir ?: workspaceManager.workspaceRoot
        viewModelScope.launch {
            val newFile = workspaceManager.createFile(target, name.trim())
            dismissNewFileDialog()
            refreshFileTree()
            openFileInTab(newFile)
        }
    }

    fun showNewFolderDialog(targetDir: File? = null) {
        _uiState.update {
            it.copy(
                isNewFolderDialogVisible = true,
                newFolderTargetDir = targetDir ?: workspaceManager.workspaceRoot
            )
        }
    }

    fun dismissNewFolderDialog() {
        _uiState.update { it.copy(isNewFolderDialogVisible = false, newFolderTargetDir = null) }
    }

    fun createNewFolder(name: String) {
        val target = _uiState.value.newFolderTargetDir ?: workspaceManager.workspaceRoot
        viewModelScope.launch {
            workspaceManager.createFolder(target, name.trim())
            dismissNewFolderDialog()
            refreshFileTree()
        }
    }

    fun showRenameDialog(file: File) {
        _uiState.update { it.copy(isRenameDialogVisible = true, renameTargetFile = file) }
    }

    fun dismissRenameDialog() {
        _uiState.update { it.copy(isRenameDialogVisible = false, renameTargetFile = null) }
    }

    fun renameFile(newName: String) {
        val target = _uiState.value.renameTargetFile ?: return
        viewModelScope.launch {
            val renamed = workspaceManager.renameFile(target, newName.trim())
            dismissRenameDialog()
            refreshFileTree()
            // If renamed file was open in tabs, update its reference
            val tabs = _uiState.value.openTabs.map {
                if (it.file.absolutePath == target.absolutePath) {
                    it.copy(file = renamed, title = renamed.name, path = renamed.absolutePath)
                } else it
            }
            _uiState.update { it.copy(openTabs = tabs) }
        }
    }

    fun deleteFile(file: File) {
        viewModelScope.launch {
            workspaceManager.deleteFile(file)
            // Close tab if open
            val tabIndex = _uiState.value.openTabs.indexOfFirst { it.file.absolutePath == file.absolutePath }
            if (tabIndex != -1) {
                closeTab(tabIndex)
            }
            refreshFileTree()
        }
    }

    fun duplicateFile(file: File) {
        viewModelScope.launch {
            workspaceManager.duplicateFile(file)
            refreshFileTree()
        }
    }

    // Search Actions
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        triggerSearch()
    }

    fun updateSearchReplaceText(text: String) {
        _uiState.update { it.copy(searchReplaceText = text) }
    }

    fun toggleSearchRegex() {
        _uiState.update { it.copy(isSearchRegex = !it.isSearchRegex) }
        triggerSearch()
    }

    fun toggleSearchCaseSensitive() {
        _uiState.update { it.copy(isSearchCaseSensitive = !it.isSearchCaseSensitive) }
        triggerSearch()
    }

    fun toggleSearchWholeWord() {
        _uiState.update { it.copy(isSearchWholeWord = !it.isSearchWholeWord) }
        triggerSearch()
    }

    fun triggerSearch() {
        val state = _uiState.value
        if (state.searchQuery.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val results = workspaceManager.searchInFiles(
                query = state.searchQuery,
                isRegex = state.isSearchRegex,
                isCaseSensitive = state.isSearchCaseSensitive,
                isWholeWord = state.isSearchWholeWord
            )
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun replaceAllOccurrences() {
        val state = _uiState.value
        if (state.searchQuery.isBlank()) return
        viewModelScope.launch {
            val count = workspaceManager.replaceAll(
                query = state.searchQuery,
                replacement = state.searchReplaceText,
                isRegex = state.isSearchRegex,
                isCaseSensitive = state.isSearchCaseSensitive
            )
            triggerSearch()
            refreshFileTree()
            // Reload active tab
            state.activeTab?.file?.let { openFileInTab(it) }
        }
    }

    // Git Actions
    fun stageFile(file: File) {
        viewModelScope.launch {
            gitManager.stageFile(file)
            refreshGitStatus()
        }
    }

    fun unstageFile(file: File) {
        viewModelScope.launch {
            gitManager.unstageFile(file)
            refreshGitStatus()
        }
    }

    fun stageAll() {
        viewModelScope.launch {
            gitManager.stageAll()
            refreshGitStatus()
        }
    }

    fun unstageAll() {
        viewModelScope.launch {
            gitManager.unstageAll()
            refreshGitStatus()
        }
    }

    fun updateCommitMessage(msg: String) {
        _uiState.update { it.copy(commitMessage = msg) }
    }

    fun commitChanges() {
        val msg = _uiState.value.commitMessage
        if (msg.isBlank()) return
        viewModelScope.launch {
            gitManager.commit(msg)
            _uiState.update { it.copy(commitMessage = "") }
            refreshGitStatus()
            refreshFileTree()
        }
    }

    fun switchGitBranch(branch: String) {
        viewModelScope.launch {
            gitManager.switchBranch(branch)
            refreshGitStatus()
        }
    }

    fun showFileDiff(file: File) {
        viewModelScope.launch {
            val diff = gitManager.getDiff(file)
            _uiState.update { it.copy(isDiffViewerVisible = true, activeDiff = diff) }
        }
    }

    fun dismissDiffViewer() {
        _uiState.update { it.copy(isDiffViewerVisible = false, activeDiff = null) }
    }

    private suspend fun refreshGitStatus() {
        val statuses = gitManager.getFileStatuses()
        val branches = gitManager.getBranches()
        val curBranch = branches.firstOrNull { it.isCurrent }?.name ?: "main"
        val commits = gitManager.getCommitHistory()
        _uiState.update {
            it.copy(
                gitStatuses = statuses,
                gitBranches = branches,
                currentGitBranch = curBranch,
                gitCommits = commits
            )
        }
    }

    // Terminal Actions
    fun updateTerminalInput(input: String) {
        _uiState.update { it.copy(terminalInput = input) }
    }

    fun executeTerminalCommand() {
        val input = _uiState.value.terminalInput.trim()
        if (input.isBlank()) return
        val currentLines = _uiState.value.terminalSession.lines.toMutableList()
        val prompt = terminalEngine.getPrompt()

        currentLines.add(TerminalLine("$prompt$input", isPrompt = true))
        _uiState.update { it.copy(terminalInput = "") }

        if (input.lowercase() == "clear") {
            _uiState.update {
                it.copy(
                    terminalSession = it.terminalSession.copy(lines = emptyList())
                )
            }
            return
        }

        viewModelScope.launch {
            val output = terminalEngine.executeCommand(input, _uiState.value.activeTab?.file)
            currentLines.addAll(output)
            currentLines.add(TerminalLine("")) // spacer
            _uiState.update {
                it.copy(
                    terminalSession = it.terminalSession.copy(
                        lines = currentLines,
                        history = it.terminalSession.history + input
                    )
                )
            }
            refreshFileTree()
            refreshGitStatus()
        }
    }

    fun clearTerminal() {
        _uiState.update {
            it.copy(
                terminalSession = it.terminalSession.copy(lines = emptyList())
            )
        }
    }

    // Command Palette & Quick Open
    fun showCommandPalette() {
        _uiState.update { it.copy(isCommandPaletteVisible = true, commandPaletteQuery = "") }
    }

    fun dismissCommandPalette() {
        _uiState.update { it.copy(isCommandPaletteVisible = false, commandPaletteQuery = "") }
    }

    fun updateCommandPaletteQuery(q: String) {
        _uiState.update { it.copy(commandPaletteQuery = q) }
    }

    fun showQuickOpen() {
        _uiState.update { it.copy(isQuickOpenVisible = true, quickOpenQuery = "") }
    }

    fun dismissQuickOpen() {
        _uiState.update { it.copy(isQuickOpenVisible = false, quickOpenQuery = "") }
    }

    fun updateQuickOpenQuery(q: String) {
        _uiState.update { it.copy(quickOpenQuery = q) }
    }

    fun getFilteredCommands(): List<CommandItem> {
        val query = _uiState.value.commandPaletteQuery.lowercase().trim()
        val allCommands = listOf(
            CommandItem("file.new", "File: New File", "File", "Ctrl+N") { showNewFileDialog() },
            CommandItem("file.newFolder", "File: New Folder", "File") { showNewFolderDialog() },
            CommandItem("file.save", "File: Save", "File", "Ctrl+S") { saveActiveFile() },
            CommandItem("file.closeTab", "View: Close Current Tab", "View", "Ctrl+W") {
                if (_uiState.value.activeTabIndex >= 0) closeTab(_uiState.value.activeTabIndex)
            },
            CommandItem("file.closeAll", "View: Close All Tabs", "View") { closeAllTabs() },
            CommandItem("view.explorer", "View: Show Explorer", "View", "Ctrl+Shift+E") { selectSidebarPanel(SidebarPanel.EXPLORER) },
            CommandItem("view.search", "View: Show Search", "View", "Ctrl+Shift+F") { selectSidebarPanel(SidebarPanel.SEARCH) },
            CommandItem("view.scm", "View: Show Source Control", "View", "Ctrl+Shift+G") { selectSidebarPanel(SidebarPanel.SOURCE_CONTROL) },
            CommandItem("view.extensions", "View: Show Extensions", "View", "Ctrl+Shift+X") { selectSidebarPanel(SidebarPanel.EXTENSIONS) },
            CommandItem("view.terminal", "View: Toggle Terminal", "Terminal", "Ctrl+`") { toggleBottomPanel() },
            CommandItem("view.split", "View: Split Editor", "View") { toggleSplitEditor() },
            CommandItem("view.zen", "View: Toggle Zen Mode", "View") { toggleZenMode() },
            CommandItem("view.markdown", "Markdown: Open Preview to the Side", "Markdown") { toggleMarkdownPreview() },
            CommandItem("editor.format", "Format Document", "Editor", "Shift+Alt+F") { formatDocument() },
            CommandItem("editor.find", "Find in File", "Editor", "Ctrl+F") { triggerFind() },
            CommandItem("editor.replace", "Replace in File", "Editor", "Ctrl+H") { triggerReplace() },
            CommandItem("editor.comment", "Toggle Line Comment", "Editor", "Ctrl+/") { triggerComment() },
            CommandItem("editor.selectAll", "Select All", "Editor", "Ctrl+A") { triggerSelectAll() },
            CommandItem("editor.undo", "Edit: Undo", "Edit", "Ctrl+Z") { triggerUndo() },
            CommandItem("editor.redo", "Edit: Redo", "Edit", "Ctrl+Y") { triggerRedo() },
            CommandItem("git.commit", "Git: Commit Changes", "Git") { selectSidebarPanel(SidebarPanel.SOURCE_CONTROL) },
            CommandItem("git.stageAll", "Git: Stage All Changes", "Git") { stageAll() },
            CommandItem("settings.open", "Preferences: Open Settings", "Preferences", "Ctrl+,") { selectSidebarPanel(SidebarPanel.SETTINGS) }
        )

        return if (query.isEmpty()) {
            allCommands
        } else {
            allCommands.filter { it.title.lowercase().contains(query) || it.category.lowercase().contains(query) }
        }
    }

    // Editor Actions via WebView Bridge
    fun insertAccessorySymbol(symbol: String) {
        editorBridgeAction?.invoke("insertText(`${escapeForJs(symbol)}`);")
    }

    fun formatDocument() {
        editorBridgeAction?.invoke("formatCode();")
    }

    fun triggerUndo() {
        editorBridgeAction?.invoke("triggerUndo();")
    }

    fun triggerRedo() {
        editorBridgeAction?.invoke("triggerRedo();")
    }

    fun triggerFind() {
        editorBridgeAction?.invoke("triggerFind();")
    }

    fun triggerReplace() {
        editorBridgeAction?.invoke("triggerReplace();")
    }

    fun triggerComment() {
        editorBridgeAction?.invoke("triggerComment();")
    }

    fun triggerSelectAll() {
        editorBridgeAction?.invoke("triggerSelectAll();")
    }

    fun jumpToLine(line: Int, col: Int = 1) {
        editorBridgeAction?.invoke("goToLine($line, $col);")
    }

    // Settings Updates
    fun updateSettings(newSettings: EditorSettings) {
        _uiState.update { it.copy(settings = newSettings) }
        settingsRepo.saveSettings(newSettings)

        // Sync settings to Monaco
        val optionsJson = JSONObject().apply {
            put("fontSize", newSettings.fontSize)
            put("lineHeight", newSettings.lineHeight)
            put("tabSize", newSettings.tabSize)
            put("wordWrap", if (newSettings.wordWrap) "on" else "off")
            put("minimap", JSONObject().apply { put("enabled", newSettings.minimap) })
            put("lineNumbers", newSettings.lineNumbers)
            put("cursorBlinking", newSettings.cursorBlinking)
            put("smoothScrolling", newSettings.smoothScrolling)
            put("bracketPairColorization", JSONObject().apply { put("enabled", newSettings.bracketPairColorization) })
        }.toString()

        editorBridgeAction?.invoke("updateOptions('$optionsJson');")
        editorBridgeAction?.invoke("setEditorTheme('${newSettings.theme}');")
        refreshFileTree()
    }

    fun toggleExtension(extensionId: String) {
        val updated = _uiState.value.extensions.map {
            if (it.id == extensionId) it.copy(isEnabled = !it.isEnabled) else it
        }
        _uiState.update { it.copy(extensions = updated) }
    }

    private fun escapeForJs(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("`", "\\`")
            .replace("\$", "\\\$")
            .replace("\r", "")
    }
}

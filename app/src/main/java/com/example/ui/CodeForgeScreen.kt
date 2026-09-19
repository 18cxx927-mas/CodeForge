package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.model.FileItem
import com.example.ui.dialogs.CommandPaletteDialog
import com.example.ui.dialogs.CreateFileDialog
import com.example.ui.dialogs.CreateFolderDialog
import com.example.ui.dialogs.QuickOpenDialog
import com.example.ui.dialogs.RenameDialog
import com.example.ui.editor.BreadcrumbsBar
import com.example.ui.editor.DiffViewer
import com.example.ui.editor.EditorTabBar
import com.example.ui.editor.MarkdownPreview
import com.example.ui.editor.MobileAccessoryBar
import com.example.ui.editor.MonacoEditorWebView
import com.example.ui.panel.BottomPanel
import com.example.ui.panel.StatusBar
import com.example.ui.sidebar.ActivityBar
import com.example.ui.sidebar.ExplorerPanel
import com.example.ui.sidebar.ExtensionsPanel
import com.example.ui.sidebar.RunDebugPanel
import com.example.ui.sidebar.SearchPanel
import com.example.ui.sidebar.SettingsPanel
import com.example.ui.sidebar.SourceControlPanel
import com.example.ui.theme.VsCodeBg
import com.example.ui.topbar.CodeForgeTopBar
import com.example.viewmodel.BottomPanelTab
import com.example.viewmodel.CodeForgeViewModel
import com.example.viewmodel.SidebarPanel

@Composable
fun CodeForgeScreen(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val config = LocalConfiguration.current
    val isTablet = config.screenWidthDp >= 600

    Scaffold(
        topBar = {
            if (!uiState.isZenMode) {
                CodeForgeTopBar(
                    activeFileName = uiState.activeTab?.title,
                    isSidebarVisible = uiState.isSidebarVisible,
                    onToggleSidebar = { viewModel.toggleSidebar() },
                    onOpenCommandPalette = { viewModel.showCommandPalette() },
                    onOpenQuickOpen = { viewModel.showQuickOpen() },
                    onRunActiveFile = {
                        viewModel.selectBottomTab(BottomPanelTab.TERMINAL)
                        viewModel.executeTerminalCommand()
                    },
                    onToggleTerminal = { viewModel.toggleBottomPanel() },
                    onToggleZenMode = { viewModel.toggleZenMode() },
                    onToggleMarkdownPreview = { viewModel.toggleMarkdownPreview() },
                    onOpenSettings = { viewModel.selectSidebarPanel(SidebarPanel.SETTINGS) }
                )
            }
        },
        bottomBar = {
            if (!uiState.isZenMode) {
                StatusBar(
                    currentBranch = uiState.currentGitBranch,
                    errorCount = uiState.errorCount,
                    warningCount = uiState.warningCount,
                    activeLine = uiState.activeLine,
                    activeColumn = uiState.activeColumn,
                    language = uiState.activeLanguage,
                    tabSize = uiState.settings.tabSize,
                    isZenMode = uiState.isZenMode,
                    onBranchClick = { viewModel.selectSidebarPanel(SidebarPanel.SOURCE_CONTROL) },
                    onProblemsClick = { viewModel.selectBottomTab(BottomPanelTab.PROBLEMS) },
                    onToggleTerminal = { viewModel.toggleBottomPanel() },
                    onToggleZenMode = { viewModel.toggleZenMode() },
                    onToggleMarkdownPreview = { viewModel.toggleMarkdownPreview() }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(VsCodeBg)
        ) {
            // 1. Activity Bar (Left Rail)
            if (!uiState.isZenMode) {
                ActivityBar(
                    activePanel = uiState.activeSidebarPanel,
                    isSidebarVisible = uiState.isSidebarVisible,
                    onSelectPanel = { panel -> viewModel.selectSidebarPanel(panel) },
                    gitChangesCount = uiState.gitStatuses.size
                )
            }

            // 2. Primary Sidebar (Explorer, Search, SCM, Run/Debug, Extensions, Settings)
            if (!uiState.isZenMode) {
                AnimatedVisibility(
                    visible = uiState.isSidebarVisible,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    val sidebarWidth = if (isTablet) 300.dp else 240.dp
                    Box(
                        modifier = Modifier
                            .width(sidebarWidth)
                            .fillMaxHeight()
                    ) {
                        when (uiState.activeSidebarPanel) {
                            SidebarPanel.EXPLORER -> {
                                ExplorerPanel(
                                    fileTree = uiState.fileTree,
                                    onFileClick = { file -> viewModel.openFileInTab(file) },
                                    onFolderToggle = { item -> viewModel.toggleFolder(item) },
                                    onNewFile = { target -> viewModel.showNewFileDialog(target) },
                                    onNewFolder = { target -> viewModel.showNewFolderDialog(target) },
                                    onRefresh = { viewModel.refreshFileTree() },
                                    onRename = { file -> viewModel.showRenameDialog(file) },
                                    onDelete = { file -> viewModel.deleteFile(file) },
                                    onDuplicate = { file -> viewModel.duplicateFile(file) },
                                    onShowDiff = { file -> viewModel.showFileDiff(file) }
                                )
                            }
                            SidebarPanel.SEARCH -> {
                                SearchPanel(
                                    searchQuery = uiState.searchQuery,
                                    replaceText = uiState.searchReplaceText,
                                    isRegex = uiState.isSearchRegex,
                                    isCaseSensitive = uiState.isSearchCaseSensitive,
                                    isWholeWord = uiState.isSearchWholeWord,
                                    isSearching = uiState.isSearching,
                                    results = uiState.searchResults,
                                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                    onReplaceTextChange = { viewModel.updateSearchReplaceText(it) },
                                    onToggleRegex = { viewModel.toggleSearchRegex() },
                                    onToggleCaseSensitive = { viewModel.toggleSearchCaseSensitive() },
                                    onToggleWholeWord = { viewModel.toggleSearchWholeWord() },
                                    onReplaceAll = { viewModel.replaceAllOccurrences() },
                                    onResultClick = { file, line ->
                                        viewModel.openFileInTab(file)
                                        viewModel.jumpToLine(line)
                                    }
                                )
                            }
                            SidebarPanel.SOURCE_CONTROL -> {
                                SourceControlPanel(
                                    currentBranch = uiState.currentGitBranch,
                                    branches = uiState.gitBranches,
                                    gitStatuses = uiState.gitStatuses,
                                    commits = uiState.gitCommits,
                                    commitMessage = uiState.commitMessage,
                                    onCommitMessageChange = { viewModel.updateCommitMessage(it) },
                                    onCommit = { viewModel.commitChanges() },
                                    onStageFile = { viewModel.stageFile(it) },
                                    onUnstageFile = { viewModel.unstageFile(it) },
                                    onStageAll = { viewModel.stageAll() },
                                    onUnstageAll = { viewModel.unstageAll() },
                                    onSwitchBranch = { viewModel.switchGitBranch(it) },
                                    onShowDiff = { viewModel.showFileDiff(it) }
                                )
                            }
                            SidebarPanel.RUN_DEBUG -> {
                                RunDebugPanel(
                                    activeTab = uiState.activeTab,
                                    onRunActiveFile = {
                                        viewModel.selectBottomTab(BottomPanelTab.TERMINAL)
                                        viewModel.executeTerminalCommand()
                                    }
                                )
                            }
                            SidebarPanel.EXTENSIONS -> {
                                ExtensionsPanel(
                                    extensions = uiState.extensions,
                                    onToggleExtension = { viewModel.toggleExtension(it) }
                                )
                            }
                            SidebarPanel.SETTINGS -> {
                                SettingsPanel(
                                    settings = uiState.settings,
                                    onSettingsChanged = { viewModel.updateSettings(it) }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Central Work Area: Tabs + Editor + Bottom Panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Tab Bar
                if (!uiState.isZenMode && uiState.openTabs.isNotEmpty()) {
                    EditorTabBar(
                        tabs = uiState.openTabs,
                        activeTabIndex = uiState.activeTabIndex,
                        isSplitEditor = uiState.isSplitEditor,
                        onTabSelected = { viewModel.selectTab(it) },
                        onTabClosed = { viewModel.closeTab(it) },
                        onCloseOtherTabs = { viewModel.closeOtherTabs(it) },
                        onCloseAllTabs = { viewModel.closeAllTabs() },
                        onTogglePinTab = { viewModel.togglePinTab(it) },
                        onToggleSplitEditor = { viewModel.toggleSplitEditor() }
                    )
                }

                // Breadcrumbs
                if (!uiState.isZenMode && uiState.settings.breadcrumbs && uiState.activeTab != null) {
                    BreadcrumbsBar(filePath = uiState.activeTab?.path)
                }

                // Editor Viewport
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        uiState.isMarkdownPreviewActive && uiState.activeTab != null -> {
                            MarkdownPreview(
                                content = uiState.activeTab?.content ?: "",
                                onClose = { viewModel.toggleMarkdownPreview() }
                            )
                        }
                        uiState.isDiffViewerVisible && uiState.activeDiff != null -> {
                            DiffViewer(
                                diff = uiState.activeDiff!!,
                                onClose = { viewModel.dismissDiffViewer() }
                            )
                        }
                        uiState.isSplitEditor && uiState.splitTab != null -> {
                            // Side-by-side split editor
                            Row(modifier = Modifier.fillMaxSize()) {
                                MonacoEditorWebView(
                                    viewModel = viewModel,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(VsCodeBg)
                                ) {
                                    MarkdownPreview(
                                        content = uiState.splitTab?.content ?: "",
                                        onClose = { viewModel.toggleSplitEditor() }
                                    )
                                }
                            }
                        }
                        else -> {
                            MonacoEditorWebView(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Mobile Accessory Bar (Above keyboard/bottom panel)
                if (uiState.settings.showAccessoryBar) {
                    MobileAccessoryBar(
                        onInsertText = { viewModel.insertAccessorySymbol(it) },
                        onUndo = { viewModel.triggerUndo() },
                        onRedo = { viewModel.triggerRedo() },
                        onSave = { viewModel.saveActiveFile() },
                        onFind = { viewModel.triggerFind() },
                        onFormat = { viewModel.formatDocument() },
                        onComment = { viewModel.triggerComment() }
                    )
                }

                // Integrated Bottom Panel (Terminal, Problems, Output, Debug Console)
                AnimatedVisibility(
                    visible = uiState.isBottomPanelVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    BottomPanel(
                        activeTab = uiState.activeBottomTab,
                        onSelectTab = { viewModel.selectBottomTab(it) },
                        onClose = { viewModel.toggleBottomPanel() },
                        terminalLines = uiState.terminalSession.lines,
                        terminalInput = uiState.terminalInput,
                        onTerminalInputChange = { viewModel.updateTerminalInput(it) },
                        onExecuteCommand = { viewModel.executeTerminalCommand() },
                        onClearTerminal = { viewModel.clearTerminal() },
                        problems = uiState.problems,
                        onProblemClick = { problem ->
                            viewModel.jumpToLine(problem.line, problem.column)
                        },
                        outputLogs = uiState.outputLogs,
                        debugLogs = uiState.debugLogs,
                        debugInput = uiState.debugInput,
                        onDebugInputChange = { /* debug input */ },
                        onExecuteDebug = { /* execute debug */ }
                    )
                }
            }
        }
    }

    // Modal Overlays
    if (uiState.isCommandPaletteVisible) {
        CommandPaletteDialog(
            query = uiState.commandPaletteQuery,
            commands = viewModel.getFilteredCommands(),
            onQueryChange = { viewModel.updateCommandPaletteQuery(it) },
            onCommandSelect = { cmd ->
                viewModel.dismissCommandPalette()
                cmd.action()
            },
            onDismiss = { viewModel.dismissCommandPalette() }
        )
    }

    if (uiState.isQuickOpenVisible) {
        QuickOpenDialog(
            query = uiState.quickOpenQuery,
            files = uiState.fileTree,
            onQueryChange = { viewModel.updateQuickOpenQuery(it) },
            onFileSelect = { file ->
                viewModel.openFileInTab(file)
            },
            onDismiss = { viewModel.dismissQuickOpen() }
        )
    }

    if (uiState.isNewFileDialogVisible) {
        CreateFileDialog(
            onConfirm = { name -> viewModel.createNewFile(name) },
            onDismiss = { viewModel.dismissNewFileDialog() }
        )
    }

    if (uiState.isNewFolderDialogVisible) {
        CreateFolderDialog(
            onConfirm = { name -> viewModel.createNewFolder(name) },
            onDismiss = { viewModel.dismissNewFolderDialog() }
        )
    }

    if (uiState.isRenameDialogVisible && uiState.renameTargetFile != null) {
        RenameDialog(
            initialName = uiState.renameTargetFile!!.name,
            onConfirm = { newName -> viewModel.renameFile(newName) },
            onDismiss = { viewModel.dismissRenameDialog() }
        )
    }
}

package com.mbd.cmsteacher.feature.students

import com.mbd.cmscommon.util.rememberDocumentExport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.TeacherStudentRosterWorkspace

@Composable
fun MyStudentsScreen(viewModel: MyStudentsViewModel = hiltViewModel()) {
    val assignments by viewModel.assignments.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val students by viewModel.students.collectAsState()
    val tallies by viewModel.tallies.collectAsState()
    val syncError by viewModel.syncError.collectAsState()
    var didInitialRefresh by remember { mutableStateOf(false) }

    // One combined refresh across every class, the first time this teacher's classes are known -- the default
    // view is "All classes", so it needs a sync of its own instead of waiting for a class to be picked.
    LaunchedEffect(assignments) {
        if (!didInitialRefresh && assignments.isNotEmpty()) {
            didInitialRefresh = true
            viewModel.selectAll()
        }
    }

    TeacherStudentRosterWorkspace(

        onExport = rememberDocumentExport(),
        assignments = assignments,
        selected = selected,
        students = students,
        tallies = tallies,
        onSelectAssignment = viewModel::selectAssignment,
        onShowAllClasses = viewModel::selectAll,
        syncError = syncError,
    )
}

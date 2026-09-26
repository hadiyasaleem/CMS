package com.mbd.cmsteacher.feature.students

import com.mbd.cmscommon.util.rememberDocumentExport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.TeacherStudentRosterWorkspace

@Composable
fun MyStudentsScreen(viewModel: MyStudentsViewModel = hiltViewModel()) {
    val assignments by viewModel.assignments.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val students by viewModel.students.collectAsState()
    val tallies by viewModel.tallies.collectAsState()

    TeacherStudentRosterWorkspace(

        onExport = rememberDocumentExport(),
        assignments = assignments,
        selected = selected,
        students = students,
        tallies = tallies,
        onSelectAssignment = viewModel::selectAssignment,
    )
}

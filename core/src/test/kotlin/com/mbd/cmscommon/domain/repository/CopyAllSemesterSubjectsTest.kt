package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.PoolSubject
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test

/** [CurriculumRepository.copyAllSemesterSubjects] is a default method (every semester 1-8, same pair of sessions);
 * this is the one place that contract is pinned down, since nothing else exercises the interface default directly. */
class CopyAllSemesterSubjectsTest {

    private class RecordingRepo : CurriculumRepository {
        val copied = mutableListOf<Triple<Int, Int, Pair<String, String>>>()
        override fun observeSemesterSubjects(sessionId: String, semester: Int): Flow<List<SemesterSubject>> = flowOf(emptyList())
        override fun observeSessionSubjects(sessionId: String): Flow<List<SemesterSubject>> = flowOf(emptyList())
        override fun observePoolSubjects(): Flow<List<PoolSubject>> = flowOf(emptyList())
        override suspend fun getSemesterTerm(sessionId: String, semester: Int): SemesterTerm? = null
        override suspend fun saveSemesterSubject(subject: SemesterSubject) {}
        override suspend fun linkSemesterSubject(sessionId: String, semester: Int, courseCode: String, isElective: Boolean) {}
        override suspend fun copySemesterSubjects(fromSessionId: String, fromSemester: Int, toSessionId: String, toSemester: Int) {
            copied += Triple(fromSemester, toSemester, fromSessionId to toSessionId)
        }
        override suspend fun deleteSemesterSubject(sessionId: String, semester: Int, courseCode: String) {}
        override suspend fun saveSemesterTerm(sessionId: String, semester: Int, startDate: LocalDate?, endDate: LocalDate?) {}
        override suspend fun syncSession(sessionId: String) {}
        override suspend fun syncAll() {}
    }

    @Test
    fun copiesEverySemesterOneThroughEightBetweenTheSameTwoSessions() = kotlinx.coroutines.runBlocking {
        val repo = RecordingRepo()
        repo.copyAllSemesterSubjects("ch_2025", "ch_2026")
        assertEquals(8, repo.copied.size)
        assertEquals((1..8).toList(), repo.copied.map { it.first })
        assertEquals((1..8).toList(), repo.copied.map { it.second })
        assertEquals(setOf("ch_2025" to "ch_2026"), repo.copied.map { it.third }.toSet())
    }
}

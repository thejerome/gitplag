package ru.nikstep.redink.analysis

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.matchers.shouldEqual
import org.junit.Test
import ru.nikstep.redink.analysis.analyser.Analyser
import ru.nikstep.redink.analysis.solutions.SolutionStorage
import ru.nikstep.redink.model.data.AnalysisResult
import ru.nikstep.redink.model.entity.PullRequest
import ru.nikstep.redink.model.entity.Repository
import ru.nikstep.redink.util.GitProperty
import ru.nikstep.redink.util.Language
import ru.nikstep.redink.util.asPath
import java.nio.file.Paths

abstract class AbstractAnalyserTest {

    protected val TEST_REPO_NAME = "nikita715/plagiarism_test"
    protected val TEST_FILE_NAME = "dir/FileTest.java"

    private val relSolutionsDir = asPath("src", "test", "resources", "test_solutions")

    internal val solutionsDir = Paths.get(relSolutionsDir).toFile().absolutePath

    private val student1 = "student1"
    private val student2 = "student2"
    private val student3 = "student3"

    private val base = Paths.get(relSolutionsDir, TEST_REPO_NAME, ".base", TEST_FILE_NAME).toFile()
    private val solution1 = Paths.get(relSolutionsDir, TEST_REPO_NAME, student1, TEST_FILE_NAME).toFile()
    private val solution2 = Paths.get(relSolutionsDir, TEST_REPO_NAME, student2, TEST_FILE_NAME).toFile()
    private val solution3 = Paths.get(relSolutionsDir, TEST_REPO_NAME, student3, TEST_FILE_NAME).toFile()

    protected val gitService = GitProperty.GITHUB
    protected val sha1 = "sha1"
    protected val sha2 = "sha2"
    protected val sha3 = "sha3"

    private val testPreparedAnalysisFiles = PreparedAnalysisFiles(
        TEST_FILE_NAME,
        TEST_REPO_NAME,
        Language.JAVA,
        base,
        mapOf(
            student1 to CommittedFile(solution1, sha1),
            student2 to CommittedFile(solution2, sha2),
            student3 to CommittedFile(solution3, sha3)
        )
    )

    internal val solutionStorageService = mock<SolutionStorage> {
        on { getCountOfSolutionFiles(TEST_REPO_NAME, TEST_FILE_NAME) } doReturn 3
        on { loadAllBasesAndSolutions(any()) } doReturn listOf(testPreparedAnalysisFiles)
    }

    private val pullRequest = mock<PullRequest> {
        on { repoFullName } doReturn TEST_REPO_NAME
        on { creatorName } doReturn student1
        on { gitService } doReturn gitService
    }

    protected abstract val analysisService: Analyser

    protected abstract val expectedResult: List<AnalysisResult>

    private val repository = mock<Repository> {
        on { gitService } doReturn GitProperty.GITHUB
        on { name } doReturn TEST_REPO_NAME
    }

    @Test
    fun analyse() {
        analysisService.analyse(repository) shouldEqual expectedResult
    }
}
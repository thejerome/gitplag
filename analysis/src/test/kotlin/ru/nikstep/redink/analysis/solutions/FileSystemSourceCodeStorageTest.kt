package ru.nikstep.redink.analysis.solutions

import com.nhaarman.mockitokotlin2.*
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import org.apache.commons.io.FileUtils
import org.junit.Test
import ru.nikstep.redink.model.data.AnalysisSettings
import ru.nikstep.redink.model.entity.BaseFileRecord
import ru.nikstep.redink.model.entity.PullRequest
import ru.nikstep.redink.model.entity.Repository
import ru.nikstep.redink.model.entity.SolutionFileRecord
import ru.nikstep.redink.model.enums.AnalyserProperty
import ru.nikstep.redink.model.enums.AnalysisMode
import ru.nikstep.redink.model.enums.GitProperty
import ru.nikstep.redink.model.enums.Language
import ru.nikstep.redink.model.manager.RepositoryDataManager
import ru.nikstep.redink.model.repo.BaseFileRecordRepository
import ru.nikstep.redink.model.repo.PullRequestRepository
import ru.nikstep.redink.model.repo.SolutionFileRecordRepository
import ru.nikstep.redink.util.asPath
import ru.nikstep.redink.util.inTempDirectory
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class FileSystemSourceCodeStorageTest {

    private val unpackedZip = File(asPath("src", "test", "resources", "unpackedzip")).absolutePath
    private val solutionDir = File(asPath("src", "test", "resources", "solutiondirsample")).absolutePath
    private val composedFileDir = File(asPath("src", "test", "resources", "composedfiles")).absolutePath

    private val github = GitProperty.GITHUB
    private val java = Language.JAVA

    private val fileName1 = "File1.java"
    private val fileName2 = "dir/File2.java"
    private val fileName3 = "File3.txt"
    private val fileName4 = "dir/File4.txt"

    private val repoName = "repo"
    private val branchName = "br"
    private val student = "stud"
    private val student2 = "stud2"

    private val repo = mock<Repository> {
        on { gitService } doReturn github
        on { name } doReturn repoName
        on { language } doReturn java
        on { analyser } doReturn AnalyserProperty.MOSS
        on { analysisMode } doReturn AnalysisMode.LINK
    }

    private val sha1 = "sha1"
    private val sha2 = "sha2"

    private val pullRequest = mock<PullRequest> {
        on { repo } doReturn repo
        on { sourceBranchName } doReturn branchName
        on { creatorName } doReturn student
        on { headSha } doReturn sha1
    }

    private val pullRequest2 = mock<PullRequest> {
        on { repo } doReturn repo
        on { sourceBranchName } doReturn branchName
        on { creatorName } doReturn student2
        on { headSha } doReturn sha2
    }

    private val pathToFiles = "$solutionDir/$github/$repoName/$branchName"

    private val baseFile1 = File("$pathToFiles/.base/$fileName1")
    private val baseFile2 = File("$pathToFiles/.base/$fileName2")
    private val baseFile3 = File("$pathToFiles/.base/$fileName3")
    private val baseFile4 = File("$pathToFiles/.base/$fileName4")
    private val solFile1 = File("$pathToFiles/$student/$fileName1")
    private val solFile2 = File("$pathToFiles/$student/$fileName2")
    private val solFile3 = File("$pathToFiles/$student/$fileName3")
    private val solFile4 = File("$pathToFiles/$student/$fileName4")
    private val solFile5 = File("$pathToFiles/$student2/$fileName1")
    private val solFile6 = File("$pathToFiles/$student2/$fileName2")
    private val solFile7 = File("$pathToFiles/$student2/$fileName3")
    private val solFile8 = File("$pathToFiles/$student2/$fileName4")

    private val baseFileRecord1 = BaseFileRecord(-1, repo, fileName1, branchName)
    private val baseFileRecord2 = BaseFileRecord(-1, repo, fileName2, branchName)
    private val baseFileRecord3 = BaseFileRecord(-1, repo, fileName3, branchName)
    private val baseFileRecord4 = BaseFileRecord(-1, repo, fileName4, branchName)

    private val solFileRecord1 = SolutionFileRecord(-1, pullRequest, fileName1, 2)
    private val solFileRecord2 = SolutionFileRecord(-1, pullRequest, fileName2, 4)
    private val solFileRecord3 = SolutionFileRecord(-1, pullRequest, fileName3, 6)
    private val solFileRecord4 = SolutionFileRecord(-1, pullRequest, fileName4, 8)
    private val solFileRecord5 = SolutionFileRecord(-1, pullRequest2, fileName1, 2)
    private val solFileRecord6 = SolutionFileRecord(-1, pullRequest2, fileName2, 4)
    private val solFileRecord7 = SolutionFileRecord(-1, pullRequest2, fileName3, 6)
    private val solFileRecord8 = SolutionFileRecord(-1, pullRequest2, fileName4, 8)

    private lateinit var sourceCodeStorage: FileSystemSourceCodeStorage

    private val repositoryDataManager = mock<RepositoryDataManager> {
        on { nameMatchesRegexp(fileName1, repo) } doReturn true
        on { nameMatchesRegexp(fileName2, repo) } doReturn true
        on { nameMatchesRegexp(fileName3, repo) } doReturn false
        on { nameMatchesRegexp(fileName4, repo) } doReturn false
    }

    private val pullRequestRepository = mock<PullRequestRepository> {
        on { findAllByRepoAndSourceBranchName(repo, branchName) } doReturn listOf(pullRequest, pullRequest2)
    }

    private val baseFileRecordRepository = mock<BaseFileRecordRepository> {
        on { findAllByRepoAndBranch(repo, branchName) } doReturn
                listOf(baseFileRecord1, baseFileRecord2, baseFileRecord3, baseFileRecord4)
    }

    private val solutionFileRecordRepository = mock<SolutionFileRecordRepository> {
        on { findAllByPullRequest(pullRequest) } doReturn
                listOf(solFileRecord1, solFileRecord2, solFileRecord3, solFileRecord4)
        on { findAllByPullRequest(pullRequest2) } doReturn
                listOf(solFileRecord5, solFileRecord6, solFileRecord7, solFileRecord8)
    }

    private val studTxt = "stud.txt"
    private val stud2Txt = "stud2.txt"

    private val composedSolution1 = File("$composedFileDir/$studTxt")
    private val composedSolution2 = File("$composedFileDir/$stud2Txt")

    @Test
    fun loadBasesAndComposedSolutions() {
        sourceCodeStorage = FileSystemSourceCodeStorage(
            baseFileRecordRepository, repositoryDataManager,
            solutionFileRecordRepository, pullRequestRepository, solutionDir
        )

        val analysisSettings = AnalysisSettings(repo, branchName)

        inTempDirectory { tempDir ->
            val analysisData =
                sourceCodeStorage.loadBasesAndComposedSolutions(analysisSettings, tempDir)
            analysisData.bases shouldEqual listOf(baseFile1, baseFile2)
            analysisData.solutions.size shouldBe 2
            analysisData.gitService shouldBe github
            analysisData.language shouldBe java
            analysisData.repoName shouldBe repoName

            val sortedSolutions = analysisData.solutions.sortedBy { it.fileName }
            val solution1 = sortedSolutions[0]
            val solution2 = sortedSolutions[1]

            solution1.fileName shouldBe studTxt
            solution1.includedFileNames shouldBe listOf(fileName1, fileName2)
            solution1.includedFilePositions shouldBe listOf(2, 6)
            solution1.sha shouldBe sha1
            solution1.student shouldBe student
            FileUtils.contentEquals(solution1.file, composedSolution1) shouldBe true

            solution2.fileName shouldBe stud2Txt
            solution2.includedFileNames shouldBe listOf(fileName1, fileName2)
            solution2.includedFilePositions shouldBe listOf(2, 6)
            solution2.sha shouldBe sha2
            solution2.student shouldBe student2
            FileUtils.contentEquals(solution2.file, composedSolution2) shouldBe true
        }
    }

    @Test
    fun loadBasesAndSeparatedSolutions() {
        sourceCodeStorage = FileSystemSourceCodeStorage(
            baseFileRecordRepository, repositoryDataManager,
            solutionFileRecordRepository, pullRequestRepository, solutionDir
        )

        val analysisSettings = AnalysisSettings(repo, branchName)

        val analysisData = sourceCodeStorage.loadBasesAndSeparatedSolutions(analysisSettings)

        analysisData.bases shouldEqual listOf(baseFile1, baseFile2)
        analysisData.solutions.size shouldBe 4
        analysisData.gitService shouldBe github
        analysisData.language shouldBe java
        analysisData.repoName shouldBe repoName

        val sortedSolutions = analysisData.solutions.sortedBy { it.fileName }
        val solution1 = sortedSolutions[0]
        val solution2 = sortedSolutions[1]
        val solution3 = sortedSolutions[2]
        val solution4 = sortedSolutions[3]

        solution1.fileName shouldBe fileName1
        solution1.sha shouldBe sha1
        solution1.student shouldBe student
        FileUtils.contentEquals(solution1.file, solFile1) shouldBe true

        solution2.fileName shouldBe fileName1
        solution2.sha shouldBe sha2
        solution2.student shouldBe student2
        FileUtils.contentEquals(solution2.file, solFile5) shouldBe true

        solution3.fileName shouldBe fileName2
        solution3.sha shouldBe sha1
        solution3.student shouldBe student
        FileUtils.contentEquals(solution3.file, solFile2) shouldBe true

        solution4.fileName shouldBe fileName2
        solution4.sha shouldBe sha2
        solution4.student shouldBe student2
        FileUtils.contentEquals(solution4.file, solFile6) shouldBe true
    }

    @Test
    fun loadBasesAndSeparatedCopiedSolutions() {
        sourceCodeStorage = FileSystemSourceCodeStorage(
            baseFileRecordRepository, repositoryDataManager,
            solutionFileRecordRepository, pullRequestRepository, solutionDir
        )

        val analysisSettings = AnalysisSettings(repo, branchName)

        inTempDirectory { tempDir ->
            val analysisData =
                sourceCodeStorage.loadBasesAndSeparatedCopiedSolutions(analysisSettings, tempDir)
            analysisData.bases shouldEqual listOf(baseFile1, baseFile2)
            assertTrue { analysisData.solutions.size == 4 }
            analysisData.gitService shouldBe github
            analysisData.language shouldBe java
            analysisData.repoName shouldBe repoName

            val sortedSolutions = analysisData.solutions.sortedBy { it.fileName }
            val solution1 = sortedSolutions[0]
            val solution2 = sortedSolutions[1]
            val solution3 = sortedSolutions[2]
            val solution4 = sortedSolutions[3]

            val txt0 = "0.txt"
            val txt1 = "1.txt"

            solution1.fileName shouldBe txt0
            solution1.file shouldBe File("$tempDir/$student/$txt0")
            solution1.sha shouldBe sha1
            solution1.student shouldBe student
            solution1.realFileName shouldBe fileName1
            FileUtils.contentEquals(solution1.file, solFile1) shouldBe true

            solution2.fileName shouldBe txt0
            solution2.file shouldBe File("$tempDir/$student2/$txt0")
            solution2.sha shouldBe sha2
            solution2.student shouldBe student2
            solution2.realFileName shouldBe fileName1
            FileUtils.contentEquals(solution2.file, solFile5) shouldBe true

            solution3.fileName shouldBe txt1
            solution3.file shouldBe File("$tempDir/$student/$txt1")
            solution3.sha shouldBe sha1
            solution3.student shouldBe student
            solution3.realFileName shouldBe fileName2
            FileUtils.contentEquals(solution3.file, solFile2) shouldBe true

            solution4.fileName shouldBe txt1
            solution4.file shouldBe File("$tempDir/$student2/$txt1")
            solution4.sha shouldBe sha2
            solution4.student shouldBe student2
            solution4.realFileName shouldBe fileName2
            FileUtils.contentEquals(solution4.file, solFile6) shouldBe true
        }
    }

    @Test
    fun saveBasesFromDir() {
        inTempDirectory { tempDir ->
            sourceCodeStorage = FileSystemSourceCodeStorage(
                baseFileRecordRepository, repositoryDataManager,
                solutionFileRecordRepository, pullRequestRepository, tempDir
            )

            sourceCodeStorage.saveBasesFromDir(unpackedZip, repo, branchName)

            verify(baseFileRecordRepository).deleteAllByRepoAndBranch(repo, branchName)

            verify(baseFileRecordRepository).save(eq(baseFileRecord1))
            verify(baseFileRecordRepository).save(eq(baseFileRecord2))
            verify(baseFileRecordRepository, never()).save(eq(baseFileRecord3))
            verify(baseFileRecordRepository, never()).save(eq(baseFileRecord4))

            assertTrue { File("$tempDir/$github/$repoName/$branchName/.base/$fileName1").exists() }
            assertTrue { File("$tempDir/$github/$repoName/$branchName/.base/$fileName2").exists() }
            assertFalse { File("$tempDir/$github/$repoName/$branchName/.base/$fileName3").exists() }
            assertFalse { File("$tempDir/$github/$repoName/$branchName/.base/$fileName4").exists() }
        }
    }

    @Test
    fun saveSolutionsFromDir() {
        inTempDirectory { tempDir ->
            sourceCodeStorage = FileSystemSourceCodeStorage(
                baseFileRecordRepository, repositoryDataManager,
                solutionFileRecordRepository, pullRequestRepository, tempDir
            )

            sourceCodeStorage.saveSolutionsFromDir(unpackedZip, pullRequest)

            verify(solutionFileRecordRepository).deleteAllByPullRequest(pullRequest)

            verify(solutionFileRecordRepository).save(eq(solFileRecord1))
            verify(solutionFileRecordRepository).save(eq(solFileRecord2))
            verify(solutionFileRecordRepository, never()).save(eq(solFileRecord3))
            verify(solutionFileRecordRepository, never()).save(eq(solFileRecord4))

            assertTrue { File("$tempDir/$github/$repoName/$branchName/$student/$fileName1").exists() }
            assertTrue { File("$tempDir/$github/$repoName/$branchName/$student/$fileName2").exists() }
            assertFalse { File("$tempDir/$github/$repoName/$branchName/$student/$fileName3").exists() }
            assertFalse { File("$tempDir/$github/$repoName/$branchName/$student/$fileName4").exists() }
        }
    }
}
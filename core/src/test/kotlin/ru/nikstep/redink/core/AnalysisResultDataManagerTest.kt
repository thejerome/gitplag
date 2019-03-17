package ru.nikstep.redink.core

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import ru.nikstep.redink.model.data.AnalysisMatch
import ru.nikstep.redink.model.data.AnalysisResult
import ru.nikstep.redink.model.data.AnalysisSettings
import ru.nikstep.redink.model.data.MatchedLines
import ru.nikstep.redink.model.entity.Repository
import ru.nikstep.redink.model.entity.User
import ru.nikstep.redink.model.manager.AnalysisResultDataManager
import ru.nikstep.redink.model.repo.RepositoryRepository
import ru.nikstep.redink.model.repo.UserRepository
import ru.nikstep.redink.util.AnalyserProperty
import ru.nikstep.redink.util.GitProperty
import ru.nikstep.redink.util.Language
import java.time.LocalDateTime

@Ignore
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = [RedinkApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnalysisResultDataManagerTest {

    @Autowired
    private lateinit var analysisResultDataManager: AnalysisResultDataManager

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var repositoryRepository: RepositoryRepository

    private val user = userRepository.save(
        User(
            name = "nikita",
            githubId = 1L,
            installationId = 1L
        )
    )

    private val repo = repositoryRepository.save(
        Repository(
            owner = user,
            name = "repoName",
            analyser = AnalyserProperty.MOSS,
            periodicAnalysis = false,
            gitService = GitProperty.GITHUB,
            language = Language.JAVA,
            branches = listOf("master")
        )
    )

    private val analysisSettings = AnalysisSettings(repo, "qwe")

    private val analysisResult = AnalysisResult(
        analysisSettings,
        "link",
        LocalDateTime.now(),
        listOf(
            AnalysisMatch(
                students = "st1" to "st2",
                sha = "sha1" to "sha2",
                lines = 10,
                percentage = 20,
                matchedLines = listOf(MatchedLines(match1 = 1 to 2, match2 = 2 to 3, files = "f1" to "f2"))
            )
        )
    )

    @Test
    fun saveAll() {
        analysisResultDataManager.saveAnalysis(analysisSettings, analysisResult)
    }
}
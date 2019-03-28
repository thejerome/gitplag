package ru.nikstep.redink.model.manager

import org.springframework.transaction.annotation.Transactional
import ru.nikstep.redink.model.dto.RepositoryDto
import ru.nikstep.redink.model.entity.Repository
import ru.nikstep.redink.model.enums.AnalyserProperty
import ru.nikstep.redink.model.enums.AnalysisMode
import ru.nikstep.redink.model.enums.GitProperty
import ru.nikstep.redink.model.enums.Language
import ru.nikstep.redink.model.repo.RepositoryRepository

/**
 * Data manager of [Repository]
 */
@Transactional
class RepositoryDataManager(
    private val repositoryRepository: RepositoryRepository
) {

    /**
     * Create a repositoriy
     */
    @Transactional
    fun create(repositoryDto: RepositoryDto): Repository {
        val repository = repositoryRepository.save(
            Repository(
                name = repositoryDto.fullName,
                gitService = repositoryDto.gitService,
                language = repositoryDto.language ?: Language.JAVA,
                filePatterns = repositoryDto.filePatterns ?: emptyList(),
                analyser = repositoryDto.analyser ?: AnalyserProperty.MOSS,
                periodicAnalysis = repositoryDto.periodicAnalysis ?: false,
                periodicAnalysisDelay = repositoryDto.periodicAnalysisDelay ?: 10,
                branches = repositoryDto.branches ?: emptyList(),
                analysisMode = repositoryDto.analysisMode ?: AnalysisMode.PAIRS
            )
        )
        repository.filePatterns.size
        return repository
    }

    @Transactional
    fun update(repo: Repository, repositoryDto: RepositoryDto): Repository {
        val repository = repositoryRepository.save(
            repo.copy(
                language = repositoryDto.language ?: repo.language,
                filePatterns = repositoryDto.filePatterns ?: repo.filePatterns,
                analyser = repositoryDto.analyser ?: repo.analyser,
                periodicAnalysis = repositoryDto.periodicAnalysis ?: repo.periodicAnalysis,
                periodicAnalysisDelay = repositoryDto.periodicAnalysisDelay ?: repo.periodicAnalysisDelay,
                branches = repositoryDto.branches ?: repo.branches,
                analysisMode = repositoryDto.analysisMode ?: repo.analysisMode
            )
        )
        repository.filePatterns.size
        return repository
    }

    /**
     * Check that the file name matches with any of the repo file name patterns
     */
    @Transactional(readOnly = true)
    fun nameMatchesRegexp(fileName: String, repo: Repository): Boolean {
        repo.filePatterns.forEach {
            if (it.toRegex().matches(fileName)) return true
        }
        return false
    }

    @Transactional(readOnly = true)
    fun findByGitServiceAndName(gitService: GitProperty, name: String): Repository? {
        val repository = repositoryRepository.findByGitServiceAndName(gitService, name)
        repository?.filePatterns?.size
        return repository
    }

    @Transactional
    fun save(repo: Repository): Repository {
        val savedRepo = repositoryRepository.save(repo)
        savedRepo.filePatterns.size
        return savedRepo
    }

    @Transactional(readOnly = true)
    fun findRequiredToAnalyse(): List<Repository> = repositoryRepository.findRequiredToAnalyse()

}
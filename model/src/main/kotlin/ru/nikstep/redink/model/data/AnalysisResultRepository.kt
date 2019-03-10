package ru.nikstep.redink.model.data

import org.springframework.transaction.annotation.Transactional
import ru.nikstep.redink.model.entity.Analysis
import ru.nikstep.redink.model.entity.AnalysisPair
import ru.nikstep.redink.model.entity.AnalysisPairLines
import ru.nikstep.redink.model.entity.Repository
import ru.nikstep.redink.model.repo.AnalysisPairLinesRepository
import ru.nikstep.redink.model.repo.AnalysisPairRepository
import ru.nikstep.redink.model.repo.AnalysisRepository
import java.time.LocalDateTime

open class AnalysisResultRepository(
    private val analysisRepository: AnalysisRepository,
    private val analysisPairRepository: AnalysisPairRepository,
    private val analysisPairLinesRepository: AnalysisPairLinesRepository
) {

    /**
     * Save all analysis results
     */
    @Transactional
    open fun saveAnalysis(repository: Repository, analysisResults: Collection<AnalysisResult>): Analysis {
        val analysis = analysisRepository.save(
            Analysis(
                repository = repository,
                executionDate = LocalDateTime.now()
            )
        )
        val analysisPairs = analysisResults.map {
            val analysisPair = analysisPairRepository.save(
                AnalysisPair(
                    student1 = it.students.first,
                    student2 = it.students.second,
                    lines = it.lines,
                    repo = it.repo,
                    percentage = it.percentage,
                    student1Sha = it.sha.first,
                    student2Sha = it.sha.second,
                    gitService = it.gitService,
                    analysis = analysis
                )
            )
            val analysisPairLines = analysisPairLinesRepository.saveAll(it.matchedLines.map {
                AnalysisPairLines(
                    from1 = it.match1.first,
                    to1 = it.match1.second,
                    from2 = it.match2.first,
                    to2 = it.match2.second,
                    fileName1 = it.files.first,
                    fileName2 = it.files.second,
                    analysisPair = analysisPair
                )
            })
            analysisPair to analysisPairLines
        }
        val res = analysisPairs.map { analysisPairRepository.save(it.first.copy(analysisPairLines = it.second)) }
        return analysisRepository.save(analysis.copy(analysisPairs = res))
    }
}
package ru.nikstep.redink.analysis.analyser

import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.jsoup.Jsoup
import ru.nikstep.redink.analysis.solutions.SolutionStorage
import ru.nikstep.redink.model.data.*
import ru.nikstep.redink.util.asPath
import ru.nikstep.redink.util.asPathInRoot
import java.io.File
import java.nio.file.Files
import kotlin.math.roundToInt

/**
 * JPlag client wrapper
 */
class JPlagAnalyser(
    private val solutionStorage: SolutionStorage,
    private val solutionsDir: String,
    private val jplagResultDir: String,
    private val serverUrl: String
) :
    Analyser {

    private val logger = KotlinLogging.logger {}
    private val regexUserNames = "^Matches for (.+) & (.+)$".toRegex()
    private val regexMatchedRows = "^(.+)\\((\\d+)-(\\d+)\\)$".toRegex()

    override fun analyse(analysisSettings: AnalysisSettings): AnalysisResult {
        val hash = RandomStringUtils.randomAlphanumeric(10)
        val file = File(jplagResultDir + hash)
        Files.createDirectory(file.toPath())
        val resultDir = file.absolutePath
        val analysisFiles = solutionStorage.loadAllBasesAndSolutions(analysisSettings)
        val solutionsPath = solutionsDir.asPathInRoot() + "/" + analysisSettings.gitService.toString()
        JPlagClient(analysisFiles, solutionsPath, analysisSettings.branch, resultDir).run()
        val matchLines = analysisFiles.toSolutionPairIndexes().map { index ->
            parseResults(resultDir, index)
        }
        val resultLink = serverUrl + "/jplagresult/" + hash + "/index.html"
        return AnalysisResult(analysisSettings, resultLink, matchLines)
    }

    private fun parseResults(
        resultDir: String,
        index: Int
    ): AnalysisMatch {
        val body = Jsoup.parse(File(asPath(resultDir, "match$index-link.html")).readText())
            .body()
        val (name1, name2) = requireNotNull(regexUserNames.find(body.getElementsByTag("H3")[0].text()))
            .groupValues.subList(1, 3)
        val percentage = body.getElementsByTag("H1")[0].text().replace("%", "").toDouble().roundToInt()
        val body2 = Jsoup.parse(File(asPath(resultDir, "match$index-top.html")).readText())
            .body()
        val rows = body2.getElementsByTag("tr")
        val matchedLines = mutableListOf<MatchedLines>()
        for (rowNumber in 1 until rows.size - 1) {
            val columns = rows[rowNumber].getElementsByTag("td")
            val (fileName1, from1, to1) = requireNotNull(regexMatchedRows.find(columns[1].text()))
                .groupValues.subList(1, 4)
            val (fileName2, from2, to2) = requireNotNull(regexMatchedRows.find(columns[2].text()))
                .groupValues.subList(1, 4)
            matchedLines += MatchedLines(
                match1 = from1.toInt() to to1.toInt(),
                match2 = from2.toInt() to to2.toInt(),
                files = fileName1 to fileName2
            )
        }
        return AnalysisMatch(
            students = name1 to name2,
            sha = "" to "",
            lines = -1,
            percentage = percentage,
            matchedLines = matchedLines
        )
    }

    private fun PreparedAnalysisData.toSolutionPairIndexes(): IntRange {
        val countOfMatches = (0 until solutions.size).sum()
        return 0 until countOfMatches
    }
}
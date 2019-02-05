package ru.nikstep.redink.analysis.solutions

import ru.nikstep.redink.model.entity.PullRequest
import java.io.File

interface SolutionService {
    fun save(prData: PullRequest, fileName: String, fileText: String)
    fun load(userId: Long, repoId: Long, fileName: String): File
    fun load(repoName: String, fileName: String): Pair<File, List<File>>
}
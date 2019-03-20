package ru.nikstep.redink.model.manager

import org.springframework.transaction.annotation.Transactional
import ru.nikstep.redink.model.entity.JPlagReport
import ru.nikstep.redink.model.repo.JPlagReportRepository
import java.time.LocalDateTime

@Transactional
class JPlagReportDataManager(private val jPlagReportRepository: JPlagReportRepository) {

    @Transactional(readOnly = true)
    fun findAllCreatedBefore(date: LocalDateTime) = jPlagReportRepository.findAllCreatedBefore(date)

    @Transactional
    fun deleteAll(jPlagReports: List<JPlagReport>) = jPlagReportRepository.deleteAll(jPlagReports)

}
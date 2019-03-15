package ru.nikstep.redink.model.repo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.nikstep.redink.model.entity.JPlagReport
import java.time.LocalDateTime

interface JPlagReportRepository : JpaRepository<JPlagReport, Long> {

    @Query("from JPlagReport r where r.createdAt < ?1")
    fun findAllCreatedBefore(date: LocalDateTime): List<JPlagReport>

}
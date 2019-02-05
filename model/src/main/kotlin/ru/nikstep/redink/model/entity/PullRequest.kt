package ru.nikstep.redink.model.entity

import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn

@Entity
data class PullRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(nullable = false)
    val number: Int,

    @Column(nullable = false)
    val installationId: Int,

    @Column(nullable = false)
    val creatorName: String,

    @Column(nullable = false)
    val repoOwnerName: String,

    @Column(nullable = false)
    val repoName: String,

    @Column(nullable = false)
    val repoFullName: String,

    @Column(nullable = false)
    val headSha: String,

    @Column(nullable = false)
    val branchName: String,

    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pull_request_changed_file", joinColumns = arrayOf(JoinColumn(name = "pullRequest")))
    val changedFiles: List<String>,

    @Column(nullable = false)
    var analysed: Boolean = false
)
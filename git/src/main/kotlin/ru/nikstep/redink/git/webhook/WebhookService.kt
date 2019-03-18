package ru.nikstep.redink.git.webhook

import ru.nikstep.redink.model.entity.PullRequest

/**
 * Service that receives webhook requests about pull requests from git services
 */
interface WebhookService {

    /**
     * Transform payload from a git service
     * and save it as a pull request
     */
    fun saveNewPullRequest(payload: String): PullRequest

    /**
     * Transform payload from a git service
     * and save it as a pull request
     */
    fun saveNewBaseFiles(payload: String)

}
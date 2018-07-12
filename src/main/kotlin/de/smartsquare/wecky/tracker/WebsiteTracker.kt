package de.smartsquare.wecky.tracker

import de.smartsquare.wecky.CrawlHandler
import de.smartsquare.wecky.domain.HashedWebsite
import org.slf4j.LoggerFactory


class WebsiteTracker() {

    companion object Factory {
        val log = LoggerFactory.getLogger(CrawlHandler::class.java.simpleName)
    }

    fun checkHash(newHashed: HashedWebsite, latest: HashedWebsite?): HashedWebsite? {
        if (latest?.hashValue == newHashed.hashValue) {
            log.info("Nothing changed on website [${newHashed.websiteId}]")
            return null
        }

        log.info("Website [${newHashed.websiteId}] changed, writing new hash [${newHashed.hashValue}]")
        return newHashed
    }
}
package de.smartsquare.wecky.crawler

import de.smartsquare.wecky.CrawlHandler
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.HashedWebsiteRepository
import de.smartsquare.wecky.domain.Website
import org.slf4j.LoggerFactory

class WebsiteTracker(val hashedWebsiteRepository: HashedWebsiteRepository) {

    companion object Factory {
        val log = LoggerFactory.getLogger(CrawlHandler::class.java.simpleName)
    }

    fun track(website: Website, newHashed: HashedWebsite) {
        val oldHashed = hashedWebsiteRepository.findBy(newHashed.websiteId, newHashed.hashValue)
        if (oldHashed != null) {
            log.info("Nothing changed on website [${website.id}]")
            return
        }

        log.info("Website [${website.id}] changed, writing new hash [${newHashed.hashValue}]")
        hashedWebsiteRepository.write(newHashed)
    }
}
package de.smartsquare.wecky.crawler

import de.smartsquare.wecky.CrawlHandler
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.Website
import de.smartsquare.wecky.dynamo.DynamoDbClient
import org.slf4j.LoggerFactory

class WebsiteTracker(val dynamoDbClient: DynamoDbClient) {

    companion object Factory {
        val log = LoggerFactory.getLogger(CrawlHandler::class.java.simpleName)
    }

    fun track(website: Website, newHashed: HashedWebsite) {
        val oldHashed = dynamoDbClient.readItem(newHashed.id)
        val changed = oldHashed?.hash != newHashed.hash

        if (changed) {
            log.info("Website [${website.id}] changed, writing new hash [${newHashed.hash}]")
            dynamoDbClient.write(newHashed)
        }
    }
}
package de.smartsquare.wecky.crawler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.Website
import de.smartsquare.wecky.domain.WebsiteChange
import de.smartsquare.wecky.dynamo.DynamoDbClient
import de.smartsquare.wecky.sqs.SqsPublisher

class WebsiteTracker(val dynamoDbClient: DynamoDbClient, val sqsPublisher: SqsPublisher) {

    val mapper = jacksonObjectMapper()

    fun track(website: Website, newHashed: HashedWebsite) {
        val oldHashed = dynamoDbClient.readItem(newHashed.url)
        val changed = oldHashed?.hash != newHashed.hash

        if (changed) {
            dynamoDbClient.write(newHashed)
            sqsPublisher.publishMessage(mapper.writeValueAsString(WebsiteChange(website.id, newHashed.content)))
        }
        // else do nothing :)
    }
}
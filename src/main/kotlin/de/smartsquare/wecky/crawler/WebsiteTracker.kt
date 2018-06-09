package de.smartsquare.wecky.crawler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.Website
import de.smartsquare.wecky.dynamo.DynamoDbClient
import de.smartsquare.wecky.sqs.SqsPublisher

class WebsiteTracker(val dynamoDbClient: DynamoDbClient, val sqsPublisher: SqsPublisher) {

    val mapper = jacksonObjectMapper()

    fun track(website: Website, hashedWebsite: HashedWebsite) {
        val websiteHashes = dynamoDbClient.readItem(hashedWebsite.url)
        val changed = websiteHashes
                .map { w -> w.hash }
                .filter { h -> h == hashedWebsite.hash }
                .none()

        if (changed) {
            dynamoDbClient.write(hashedWebsite)
            sqsPublisher.publishMessage(mapper.writeValueAsString(website))
        }
        // else do nothing :)
    }
}
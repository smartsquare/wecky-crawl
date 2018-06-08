package de.smartsquare.wecky

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.databind.ObjectMapper
import de.smartsquare.wecky.crawler.WebsiteCrawler
import de.smartsquare.wecky.domain.Website
import de.smartsquare.wecky.dynamo.DynamoDbClient

class CrawlHandler : RequestHandler<Website, Any> {

    override fun handleRequest(website: Website?, ctx: Context?): Any {

        val crawler = WebsiteCrawler(website!!.url)
        val dynamo = DynamoDbClient(DynamoDbClient.create())

        val hashedWebsite = crawler.crawlPage()
        dynamo.write(hashedWebsite)

        val headers = hashMapOf("Content-Type" to "application/json")

        val objectMapper = ObjectMapper()
        val websiteJson = objectMapper.writeValueAsString(hashedWebsite)
        return GatewayResponse(
                objectMapper.writeValueAsString(mapOf("Output" to websiteJson)),
                headers,
                200)
    }
}
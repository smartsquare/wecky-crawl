package de.smartsquare.wecky

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import de.smartsquare.wecky.crawler.WebsiteCrawler
import de.smartsquare.wecky.domain.Website
import de.smartsquare.wecky.dynamo.DynamoDbClient
import org.json.JSONObject

class CrawlHandler : RequestHandler<Any, Any> {

    override fun handleRequest(input: Any?, ctx: Context?): Any {

        val website: Website = input as Website
        val crawler = WebsiteCrawler(website.url)
        val dynamo = DynamoDbClient(DynamoDbClient.create())

        val hashedWebsite = crawler.crawlPage()
        dynamo.write(hashedWebsite)

        val headers = hashMapOf("Content-Type" to "application/json")

        return GatewayResponse(
                JSONObject().put("Output", JSONObject.valueToString(hashedWebsite)).toString(),
                headers,
                200)
    }
}
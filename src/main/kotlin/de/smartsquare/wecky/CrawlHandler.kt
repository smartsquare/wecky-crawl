package de.smartsquare.wecky

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.smartsquare.wecky.crawler.WebsiteCrawler
import de.smartsquare.wecky.domain.Website
import de.smartsquare.wecky.dynamo.DynamoDbClient
import java.io.InputStream
import java.io.OutputStream

class CrawlHandler : RequestStreamHandler {

    val mapper = jacksonObjectMapper()

    override fun handleRequest(input: InputStream, output: OutputStream, ctx: Context?) {
        val website = mapper.readValue(input, Website::class.java)
        val crawler = WebsiteCrawler(website.url)
        val dynamo = DynamoDbClient(DynamoDbClient.create())

        val hashedWebsite = crawler.crawlPage()
        dynamo.write(hashedWebsite)

        val websiteJson = mapper.writeValueAsString(hashedWebsite)
        mapper.writeValue(output, mapOf("Output" to websiteJson))
    }
}
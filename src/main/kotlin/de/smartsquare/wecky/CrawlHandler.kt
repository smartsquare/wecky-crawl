package de.smartsquare.wecky

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.smartsquare.wecky.crawler.WebsiteCrawler
import de.smartsquare.wecky.crawler.WebsiteTracker
import de.smartsquare.wecky.domain.Website
import de.smartsquare.wecky.dynamo.DynamoDbClient
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream

class CrawlHandler : RequestStreamHandler {

    companion object Factory {
        val log = LoggerFactory.getLogger(CrawlHandler::class.java.simpleName)
        val mapper = jacksonObjectMapper()
    }

    override fun handleRequest(input: InputStream, output: OutputStream, ctx: Context?) {
        val website = mapper.readValue(input, Website::class.java)

        // used for local test with sam
        val dyndbLocal = System.getenv("DYNDB_LOCAL")
        val amazonDynamoDB =
                if (dyndbLocal?.isNotEmpty() ?: false) {
                    log.info("Triggered local dev mode using local DynamoDB at [$dyndbLocal]")
                    System.setProperty("aws.accessKeyId", "foo")
                    System.setProperty("aws.secretKey", "bar")
                    AmazonDynamoDBClient.builder()
                            .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(dyndbLocal, "us-east-1"))
                            .build()
                } else {
                    log.info("Using production DynamoDB at eu_central_1")
                    AmazonDynamoDBClientBuilder.standard()
                            .withRegion(Regions.EU_CENTRAL_1)
                            .build()
                }
        val dynamo = DynamoDbClient(amazonDynamoDB)

        val crawler = WebsiteCrawler()
        val tracker = WebsiteTracker(dynamo)

        val hashedWebsite = crawler.crawlPage(website)
        tracker.track(website, hashedWebsite)
    }
}
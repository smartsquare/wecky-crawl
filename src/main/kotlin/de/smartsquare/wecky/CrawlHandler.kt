package de.smartsquare.wecky

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.smartsquare.wecky.crawler.WebsiteCrawler
import de.smartsquare.wecky.domain.HashedWebsiteRepository
import de.smartsquare.wecky.domain.Website
import de.smartsquare.wecky.tracker.WebsiteTracker
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

        val amazonDynamoDB = determineDynamoDB()
        val hashedWebsiteRepo = HashedWebsiteRepository(amazonDynamoDB)

        val crawler = WebsiteCrawler()
        val tracker = WebsiteTracker()
        val latest = hashedWebsiteRepo.findLatest(website.id)

        website
                .let(crawler::crawlPage)
                .let { tracker.checkHash(it, latest) }
                ?.let(hashedWebsiteRepo::write)
    }

    private fun getEnv() = System.getenv("DYNDB_LOCAL") ?: System.getProperty("DYNDB_LOCAL")

    private fun determineDynamoDB(): AmazonDynamoDB {
        val dyndbLocal = getEnv()
        return if (dyndbLocal?.isNotEmpty() == true) {
            log.info("Triggered local dev mode using local DynamoDB at [$dyndbLocal]")
            System.setProperty("aws.accessKeyId", "key")
            System.setProperty("aws.secretKey", "key2")
            AmazonDynamoDBClient.builder()
                    .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(dyndbLocal, "eu-central-1"))
                    .build()
        } else {
            log.info("Using production DynamoDB at eu-central-1")
            AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.EU_CENTRAL_1)
                    .build()
        }
    }
}
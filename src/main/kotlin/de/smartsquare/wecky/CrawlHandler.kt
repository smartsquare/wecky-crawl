package de.smartsquare.wecky

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.smartsquare.wecky.crawler.WebsiteCrawler
import de.smartsquare.wecky.crawler.WebsiteTracker
import de.smartsquare.wecky.domain.Website
import de.smartsquare.wecky.dynamo.DynamoDbClient
import de.smartsquare.wecky.sqs.SqsPublisher
import java.io.InputStream
import java.io.OutputStream

class CrawlHandler : RequestStreamHandler {

    val mapper = jacksonObjectMapper()

    override fun handleRequest(input: InputStream, output: OutputStream, ctx: Context?) {
        val website = mapper.readValue(input, Website::class.java)


        val amazonDynamoDB = AmazonDynamoDBClient.builder()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-1"))
                .build()
        val dynamo = DynamoDbClient(amazonDynamoDB)
        val sqs = SqsPublisher()
        val tracker = WebsiteTracker(dynamo, sqs)
        val crawler = WebsiteCrawler()

        val hashedWebsite = crawler.crawlPage(website)
        tracker.track(website, hashedWebsite)
    }
}
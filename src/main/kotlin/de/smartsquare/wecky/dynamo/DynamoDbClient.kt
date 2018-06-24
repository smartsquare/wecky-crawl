package de.smartsquare.wecky.dynamo

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import de.smartsquare.wecky.domain.HashedWebsite
import org.slf4j.LoggerFactory
import java.time.Instant


class DynamoDbClient(val dynamoDB: AmazonDynamoDB) {

    companion object Factory {
        fun create(): DynamoDB = DynamoDB(AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .build())

        val log = LoggerFactory.getLogger(DynamoDbClient::class.java.simpleName)
        val tableName = "WebsiteHashes"
    }

    fun createInitialTable() {
        log.info("Attempting to create table [$tableName]; please wait...")

        try {
            dynamoDB.describeTable(tableName)
        } catch (ex: ResourceNotFoundException) {
            val request = CreateTableRequest()
                    .withAttributeDefinitions(AttributeDefinition("id", ScalarAttributeType.S))
                    .withKeySchema(KeySchemaElement("id", KeyType.HASH))
                    .withProvisionedThroughput(ProvisionedThroughput(
                            10, 10))
                    .withTableName(tableName)

            dynamoDB.createTable(request)
        }
    }

    fun write(hashedWebsite: HashedWebsite) {
        createInitialTable()

        val item = mapOf("id" to AttributeValue(hashedWebsite.id),
                "url" to AttributeValue(hashedWebsite.url),
                "content" to AttributeValue(hashedWebsite.content),
                "hash" to AttributeValue(hashedWebsite.hash.toString()),
                "crawlDate" to AttributeValue(hashedWebsite.crawlDate.toEpochMilli().toString()))

        dynamoDB.putItem(tableName, item)

        log.info("PutItem succeeded: $hashedWebsite.id")
    }

    fun readItem(id: String): HashedWebsite? {
        val getItemRequest = GetItemRequest()
                .withKey(mapOf("id" to AttributeValue(id)))
                .withTableName(tableName)

        val item = dynamoDB.getItem(getItemRequest).item ?: return null

        return HashedWebsite(
                item.get("id")!!.s,
                item.get("url")!!.s,
                item.get("content")!!.s,
                item.get("hash")!!.s.toInt(),
                Instant.ofEpochMilli(item.get("crawlDate")!!.s.toLong())
        )
    }


}
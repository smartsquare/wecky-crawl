package de.smartsquare.wecky.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import de.smartsquare.wecky.domain.HashedWebsite
import org.slf4j.LoggerFactory
import java.time.Instant

class HashedWebsiteRepository(val dynamoDB: AmazonDynamoDB) {

    companion object Factory {
        val log = LoggerFactory.getLogger(HashedWebsiteRepository::class.java.simpleName)
        val tableName = "WebsiteHashes"
    }

    fun createInitialTable() {
        try {
            dynamoDB.describeTable(tableName)
        } catch (ex: ResourceNotFoundException) {
            log.info("Attempting to create table [$tableName]; please wait...")
            val request = CreateTableRequest()
                    .withAttributeDefinitions(AttributeDefinition("id", ScalarAttributeType.S))
                    .withKeySchema(KeySchemaElement("id", KeyType.HASH))
                    .withProvisionedThroughput(ProvisionedThroughput(
                            10, 10))
                    //the stream gets consumed by wecky-notify
                    .withStreamSpecification(StreamSpecification()
                            .withStreamEnabled(true)
                            .withStreamViewType(StreamViewType.NEW_IMAGE))
                    .withTableName(tableName)

            dynamoDB.createTable(request)
            log.info("Table [$tableName] created successfully")
        }
    }

    fun write(hashedWebsite: HashedWebsite) {
        createInitialTable()

        val item = mapOf("id" to AttributeValue(hashedWebsite.id),
                "websiteId" to AttributeValue(hashedWebsite.websiteId),
                "url" to AttributeValue(hashedWebsite.url),
                "content" to AttributeValue(hashedWebsite.content),
                "hash" to AttributeValue(hashedWebsite.hash.toString()),
                "crawlDate" to AttributeValue(hashedWebsite.crawlDate.toEpochMilli().toString()))

        dynamoDB.putItem(tableName, item)

        log.info("Stored new snapshot of website [${hashedWebsite.id}]")
    }

    fun readItem(websiteId: String): HashedWebsite? {
        createInitialTable()

        val attrValues = mapOf(":website_id" to AttributeValue(websiteId))
        val scanReq = ScanRequest()
                .withTableName(tableName)
                .withFilterExpression("websiteId = :website_id")
                .withExpressionAttributeValues(attrValues)

        val result = dynamoDB.scan(scanReq)
        val item = result.items.firstOrNull() ?: return null
        return HashedWebsite(
                item.get("id")!!.s,
                item.get("url")!!.s,
                item.get("content")!!.s,
                item.get("hash")!!.s.toInt(),
                Instant.ofEpochMilli(item.get("crawlDate")!!.s.toLong()),
                item.get("websiteId")!!.s)
    }


}
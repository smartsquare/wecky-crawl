package de.smartsquare.wecky.domain

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import org.slf4j.LoggerFactory
import java.time.Instant


class HashedWebsiteRepository(val dynamoDB: AmazonDynamoDB) {

    companion object Factory {
        val log = LoggerFactory.getLogger(HashedWebsiteRepository::class.java.simpleName)
        val tableName = "WebsiteHashes"
    }

    private fun createInitialTable() {
        try {
            dynamoDB.describeTable(tableName)
        } catch (ex: ResourceNotFoundException) {
            log.info("Attempting to create table [$tableName]; please wait...")
            val request = CreateTableRequest()
                    .withAttributeDefinitions(
                            AttributeDefinition("websiteId", ScalarAttributeType.S),
                            AttributeDefinition("crawlDate", ScalarAttributeType.N))
                    .withKeySchema(
                            KeySchemaElement("websiteId", KeyType.HASH),
                            KeySchemaElement("crawlDate", KeyType.RANGE))
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

    fun write(hashedWebsite: HashedWebsite): HashedWebsite {
        createInitialTable()

        val values = mapOf(
                "websiteId" to AttributeValue(hashedWebsite.websiteId),
                "url" to AttributeValue(hashedWebsite.url),
                "hashValue" to AttributeValue().withN(hashedWebsite.hashValue.toString()),
                "crawlDate" to AttributeValue().withN(hashedWebsite.crawlDate.toEpochMilli().toString()))

        dynamoDB.putItem(tableName, values)
        log.info("Stored new snapshot of website [${hashedWebsite.websiteId}]")
        return hashedWebsite
    }

    fun findLatest(websiteId: String): HashedWebsite? {
        createInitialTable()

        val query = QueryRequest()
                .withTableName(tableName)
                .withKeyConditionExpression("websiteId = :website_id")
                .withScanIndexForward(false)
                .withLimit(1)
                .withExpressionAttributeValues(mapOf(":website_id" to AttributeValue(websiteId)))


        val result = dynamoDB.query(query)
        return itemOrNull(result.items)
    }

    fun findBy(websiteId: String, hash: Int): HashedWebsite? {
        createInitialTable()

        val attrValues = mapOf(
                ":website_id" to AttributeValue(websiteId),
                //hash is a reserved keyword
                ":hash_value" to AttributeValue().withN(hash.toString()))
        val scanReq = ScanRequest()
                .withTableName(tableName)
                .withFilterExpression("websiteId = :website_id AND hashValue = :hash_value")
                .withExpressionAttributeValues(attrValues)

        val result = dynamoDB.scan(scanReq)
        return itemOrNull(result.items)
    }

    private fun itemOrNull(result: MutableList<MutableMap<String, AttributeValue>>): HashedWebsite? {
        val item = result.firstOrNull() ?: return null
        return HashedWebsite(
                item.get("websiteId")!!.s,
                item.get("url")!!.s,
                item.get("hashValue")!!.n.toInt(),
                Instant.ofEpochMilli(item.get("crawlDate")!!.n.toLong()))
    }


}
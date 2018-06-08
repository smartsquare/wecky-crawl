package de.smartsquare.wecky.dynamo

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.model.*
import de.smartsquare.wecky.domain.HashedWebsite
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Instant


class DynamoDbClient(val dynamoDB: DynamoDB) {

    companion object Factory {
        fun create(): DynamoDB = DynamoDB(AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_2)
                .build())

        val log = LoggerFactory.getLogger(DynamoDbClient::class.java.simpleName)
        val tableName = "WebsiteHashes"
    }

    fun deleteExistingTable() {
        try {
            val table = dynamoDB.getTable(tableName)

            table.delete()
            table.waitForDelete()
        } catch (e: ResourceNotFoundException) {
            log.trace("$tableName does not exist")
        }
    }

    fun createInitialTable() {

        log.info("Attempting to create table; please wait...")

        val table = dynamoDB.createTable(tableName,
                listOf(
                        KeySchemaElement("id_url", KeyType.HASH), // Partition key
                        KeySchemaElement("id_hash", KeyType.RANGE) // Sort Key
                ),

                listOf(
                        AttributeDefinition("id_url", ScalarAttributeType.S),
                        AttributeDefinition("id_hash", ScalarAttributeType.N)
                ),

                ProvisionedThroughput(10L, 10L))

        table.waitForActive()

        log.info("Success.  Table status: {}", table.getDescription().getTableStatus())

    }

    fun write(hashedWebsite: HashedWebsite) {

        val table = dynamoDB.getTable(tableName)

        val url = hashedWebsite.url
        val hash = hashedWebsite.hash
        val crawlMillis = Instant.now().toEpochMilli()

        val key = "$url $hash"

        table.putItem(Item()
                .withPrimaryKey("id_url", key, "id_hash", hash)
                .withString("url", url)
                .withInt("hash", hash)
                .withLong("crawlDate", crawlMillis))


        log.info("PutItem succeeded: $url ")
    }

//    fun readItem(name: String): List<Whisky> {
//        val table = dynamoDB.getTable(tableName)
//
//        val valueMap: ValueMap = ValueMap()
//                .withString(":name", name)
//        // .withLong(":from", Long.MIN_VALUE)
//        // .withLong(":to", Instant.now().plusSeconds(1000).toEpochMilli())
//
//        val spec = QuerySpec()
//                .withKeyConditionExpression("id_name = :name") //AND id_scrapingDate between :from and :to
//                .withValueMap(valueMap)
//
//        return table.query(spec).map { item ->
//            Whisky(
//                    item.getString("whiskyName"),
//                    item.getString("description"),
//                    item.getDouble("alcohol"),
//                    item.getDouble("liter"),
//                    BigDecimal.valueOf(item.getDouble("price")),
//                    Instant.ofEpochMilli(item.getString("id_scrapingDate").toLong())
//            )
//        }.toList()
//
//    }


}
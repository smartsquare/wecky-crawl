package de.smartsquare.wecky

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.dynamo.DynamoDbClient
import org.junit.Ignore
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Ignore("Needs local dynamodb")
class DynamoDbClientTest {

    private var dynamo: DynamoDbClient? = null

    @BeforeEach
    fun setUp() {
        val amazonDynamoDBClient = AmazonDynamoDBClient.builder()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-1"))
                .build()

        System.setProperty("aws.accessKeyId", "test1")
        System.setProperty("aws.secretKey", "test231")
        dynamo = DynamoDbClient(amazonDynamoDBClient)
    }

    @Test
    fun should_write_object_to_dynamo() {
        val hashWebsite = HashedWebsite("4711", "www.foobar.com", "<html/>")

        dynamo!!.write(hashWebsite)

        val item = dynamo!!.readItem("4711")
        assertNotNull(item)
    }

    @Test
    fun none_existing_item() {
        val item = dynamo!!.readItem("notexisting")
        assertNull(item)
    }
}
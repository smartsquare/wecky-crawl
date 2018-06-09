package de.smartsquare.wecky

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.dynamo.DynamoDbClient
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@Ignore("Needs local dynamodb")
class DynamoDbClientTest {

    private var dynamo: DynamoDbClient? = null

    @Before
    fun setUp() {
        val amazonDynamoDBClient = AmazonDynamoDBClient.builder()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-1"))
                .build()

        System.setProperty("aws.accessKeyId", "test1")
        System.setProperty("aws.secretKey", "test231")
        dynamo = DynamoDbClient(DynamoDB(amazonDynamoDBClient))
    }

    @Test
    fun should_write_object_to_dynamo() {
        val hashWebsite = HashedWebsite("www.foobar.com", 4711)

        dynamo!!.write(hashWebsite)

        val item = dynamo!!.readItem("www.foobar.com")
        assertNotNull(item)
    }
}
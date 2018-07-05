package de.smartsquare.wecky

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.HashedWebsiteRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty

@DisabledIfSystemProperty(named = "ci-server", matches = "true")
class HashedWebsiteRepositoryTest {

    private var repo: HashedWebsiteRepository? = null

    @BeforeEach
    fun setUp() {
        val amazonDynamoDBClient = AmazonDynamoDBClient.builder()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-1"))
                .build()

        System.setProperty("aws.accessKeyId", "test1")
        System.setProperty("aws.secretKey", "test231")
        repo = HashedWebsiteRepository(amazonDynamoDBClient)
    }

    @Test
    fun should_write_object_to_dynamo() {
        val hashWebsite = HashedWebsite("FOOBAR", "www.foobar.com", "<html/>")

        repo!!.write(hashWebsite)

        val item = repo!!.findBy("FOOBAR", hashWebsite.hashValue)
        assertNotNull(item)
    }

    @Test
    fun none_existing_item() {
        val item = repo!!.findBy("notexisting", 4711)
        assertNull(item)
    }
}
package de.smartsquare.wecky

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import de.smartsquare.wecky.domain.HashedWebsiteRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty

@DisabledIfSystemProperty(named = "ci-server", matches = "true")
internal class CrawlHandlerTest {

    lateinit var handler: CrawlHandler
    lateinit var repo: HashedWebsiteRepository

    @BeforeEach
    fun setUp() {
        handler = CrawlHandler()
        val dyndbLocal = "http://localhost:8000"
        System.setProperty("DYNDB_LOCAL", dyndbLocal)
        System.setProperty("aws.accessKeyId", "key")
        System.setProperty("aws.secretKey", "key2")
        val dyndbClient = AmazonDynamoDBClient.builder()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(dyndbLocal, "eu-central-1"))
                .build()
        repo = HashedWebsiteRepository(dyndbClient)

        try {
            dyndbClient.deleteTable(DeleteTableRequest(HashedWebsiteRepository.tableName))
        } catch (ex: ResourceNotFoundException) {
            // ignore non existing table
        }
    }

    @Test
    fun no_previous_hashes() {
        val inputStream = """{"id":"TIME","url":"http://time.is"}""".byteInputStream()

        handler.handleRequest(inputStream, ByteOutputStream(), null)

        assertThat(repo.findLatest("TIME"))
                .isNotNull
                .hasFieldOrPropertyWithValue("url", "https://time.is/")
    }
}
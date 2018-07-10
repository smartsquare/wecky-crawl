package de.smartsquare.wecky

import cloud.localstack.docker.LocalstackDocker
import cloud.localstack.docker.LocalstackDockerExtension
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.HashedWebsiteRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(LocalstackDockerExtension::class)
@DisabledIfSystemProperty(named = "ci-server", matches = "true")
class HashedWebsiteRepositoryTest {

    private var repo: HashedWebsiteRepository? = null

    @BeforeEach
    fun setUp() {
        val dyndbLocal = LocalstackDocker.INSTANCE.endpointDynamoDB
        val dyndbClient = AmazonDynamoDBClient.builder()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(dyndbLocal, "eu-central-1"))
                .build()

        System.setProperty("aws.accessKeyId", "test1")
        System.setProperty("aws.secretKey", "test231")
        repo = HashedWebsiteRepository(dyndbClient)

        try {
            dyndbClient.deleteTable(DeleteTableRequest(HashedWebsiteRepository.tableName))
        } catch (ex: ResourceNotFoundException) {
            // ignore non existing table
        }
    }

    @Test
    fun should_write_object_to_dynamo() {
        val hashWebsite = HashedWebsite("FOOBAR", "www.foobar.com", "<html/>", "diff")

        repo!!.write(hashWebsite)

        val item = repo!!.findBy("FOOBAR", hashWebsite.hashValue)
        assertNotNull(item)
    }

    @Test
    fun none_existing_item() {
        val item = repo!!.findBy("notexisting", 4711)
        assertNull(item)
    }

    @Test
    fun should_find_latest_by_crawldate() {
        val now = Instant.now()
        val second = HashedWebsite("FOOBAR", "second", "second", "diff", crawlDate = now.minusSeconds(10))
        val third = HashedWebsite("FOOBAR", "third", "third", "diff", crawlDate = now.minusSeconds(5))
        val first = HashedWebsite("FOOBAR", "first", "first", "diff", crawlDate = now.minusSeconds(20))
        val current = HashedWebsite("FOOBAR", "current", "current", "diff")

        repo!!.write(second)
        repo!!.write(third)
        repo!!.write(first)

        val item = repo!!.findLatest(current.websiteId)
        assertNotNull(item)
        assertEquals(third, item)
    }

}
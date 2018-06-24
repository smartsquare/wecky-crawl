package de.smartsquare.wecky.crawler

import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.Website
import de.smartsquare.wecky.dynamo.DynamoDbClient
import de.smartsquare.wecky.sqs.SqsPublisher
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

internal class WebsiteTrackerTest {

    @MockK
    lateinit var dynamoDbClient: DynamoDbClient
    @MockK
    lateinit var sqsPublisher: SqsPublisher

    @InjectMockKs
    lateinit var tracker: WebsiteTracker

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { dynamoDbClient.write(any()) } just Runs
        every { sqsPublisher.publishMessage(any()) } just Runs
    }

    @Test
    fun do_nothing_for_unchanged_website() {
        val website = Website("foobar", "foobar.de")
        val hashedWebsite = HashedWebsite("4711", "foobar.de", "<html/>")
        val previousHashed = HashedWebsite("4711", "foobar.de", "<html/>")

        every { dynamoDbClient.readItem(hashedWebsite.url) } returns previousHashed

        tracker.track(website, hashedWebsite)

        verify(exactly = 0) { sqsPublisher.publishMessage(any()) }
        verify(exactly = 0) { dynamoDbClient.write(any()) }
    }

    @Test
    fun persist_and_publish_changed_website() {
        val website = Website("foobar", "foobar.de")
        val hashedWebsite = HashedWebsite("4711", "foobar.de", "<html/>")
        val previousHashed = HashedWebsite("4711", "foobar.de", "<html><body/></html>")

        every { dynamoDbClient.readItem(hashedWebsite.url) } returns previousHashed

        tracker.track(website, hashedWebsite)

        verify(exactly = 1) { sqsPublisher.publishMessage(any()) }
        verify(exactly = 1) { dynamoDbClient.write(any()) }
    }
}
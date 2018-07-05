package de.smartsquare.wecky.crawler

import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.HashedWebsiteRepository
import de.smartsquare.wecky.domain.Website
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class WebsiteTrackerTest {

    @MockK
    lateinit var hashedWebsiteRepository: HashedWebsiteRepository

    @InjectMockKs
    lateinit var tracker: WebsiteTracker

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { hashedWebsiteRepository.write(any()) } just Runs
    }

    @Test
    fun do_nothing_for_unchanged_website() {
        val website = Website("foobar", "foobar.de")
        val hashedWebsite = HashedWebsite("4711", "foobar.de", "<html/>", "diff")
        val previousHashed = hashedWebsite.copy()

        every { hashedWebsiteRepository.findBy(hashedWebsite.websiteId, hashedWebsite.hashValue) } returns previousHashed

        tracker.track(website, hashedWebsite)

        verify(exactly = 0) { hashedWebsiteRepository.write(any()) }
    }

    @Test
    fun persist_and_publish_changed_website() {
        val website = Website("foobar", "foobar.de")
        val hashedWebsite = HashedWebsite("4711", "foobar.de", "<html/>", "diff")

        every { hashedWebsiteRepository.findBy(hashedWebsite.websiteId, hashedWebsite.hashValue) } returns null

        tracker.track(website, hashedWebsite)

        verify(exactly = 1) { hashedWebsiteRepository.write(any()) }
    }
}
package de.smartsquare.wecky.tracker

import de.smartsquare.wecky.domain.HashedWebsite
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

internal class WebsiteTrackerTest {

    val tracker = WebsiteTracker()

    @Test
    fun do_nothing_for_unchanged_website() {
        val hashedWebsite = HashedWebsite("4711", "foobar.de", "<html/>".hashCode())
        val previousHashed = hashedWebsite.copy()

        val newHashed = tracker.checkHash(hashedWebsite, previousHashed)
        assertNull(newHashed)
    }

    @Test
    fun persist_and_publish_changed_website() {
        val hashedWebsite = HashedWebsite("4711", "foobar.de", "<html/>".hashCode())
        val previousHashed = HashedWebsite("4711", "foobar.de", "<body/>".hashCode())

        val newHashed = tracker.checkHash(hashedWebsite, previousHashed)

        assertThat(newHashed?.websiteId).isEqualTo(hashedWebsite.websiteId)
        assertThat(newHashed?.hashValue).isNotEqualTo(previousHashed.hashValue)
    }
}
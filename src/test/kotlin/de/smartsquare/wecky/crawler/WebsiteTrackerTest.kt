package de.smartsquare.wecky.crawler

import de.smartsquare.wecky.domain.HashedWebsite
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

internal class WebsiteTrackerTest {

    val tracker = WebsiteTracker()

    @Test
    fun do_nothing_for_unchanged_website() {
        val hashedWebsite = HashedWebsite("4711", "foobar.de", "<html/>", "")
        val previousHashed = hashedWebsite.copy(content = "<html>")

        val newHashed = tracker.track(hashedWebsite, previousHashed)
        assertNull(newHashed)
    }

    @Test
    fun persist_and_publish_changed_website() {
        val hashedWebsite = HashedWebsite("4711", "foobar.de", "<html/>", "")
        val previousHashed = HashedWebsite("4711", "foobar.de", "<body/>", "")

        val newHashed = tracker.track(hashedWebsite, previousHashed)

        assertThat(newHashed?.websiteId).isEqualTo(hashedWebsite.websiteId)
        assertThat(newHashed?.content).isEqualTo(hashedWebsite.content)
        assertThat(newHashed?.diff).isEqualTo("~&lt;body~**&lt;html**/&gt;")
    }
}
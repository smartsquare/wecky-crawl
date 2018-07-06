package de.smartsquare.wecky.crawler

import com.github.difflib.text.DiffRowGenerator
import de.smartsquare.wecky.CrawlHandler
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.HashedWebsiteRepository
import org.slf4j.LoggerFactory


class WebsiteTracker(val hashedWebsiteRepository: HashedWebsiteRepository) {

    companion object Factory {
        val log = LoggerFactory.getLogger(CrawlHandler::class.java.simpleName)
        //create a configured DiffRowGenerator
        var diffGenerator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .oldTag { f -> "~" }      //introduce markdown style for strikethrough
                .newTag { f -> "**" }     //introduce markdown style for bold
                .build()
    }

    fun track(newHashed: HashedWebsite) {
        val oldHashed = hashedWebsiteRepository.findBy(newHashed.websiteId, newHashed.hashValue)
        if (oldHashed != null) {
            log.info("Nothing changed on website [${newHashed.websiteId}]")
            return
        }

        val latest = hashedWebsiteRepository.findLatest(newHashed.websiteId)
        val diff = latest?.content?.diffTo(newHashed.content) ?: ""

        val diffedHash = newHashed.copy(diff = diff)
        log.info("Website [${diffedHash.websiteId}] changed, writing new hash [${diffedHash.hashValue}]")
        hashedWebsiteRepository.write(diffedHash)
    }

    fun String.diffTo(other: String): String {
        val diffRows = diffGenerator.generateDiffRows(this.split("\n"), other.split("\n"))
        return diffRows.map { it.oldLine }.joinToString("\n")
    }

}
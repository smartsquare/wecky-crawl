package de.smartsquare.wecky.crawler

import de.smartsquare.wecky.CrawlHandler
import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.Website
import org.slf4j.LoggerFactory

class WebsiteCrawler(val jsoup: JsoupWrapper = JsoupWrapper()) {

    companion object Factory {
        val log = LoggerFactory.getLogger(CrawlHandler::class.java.simpleName)
    }

    fun crawlPage(website: Website): HashedWebsite {
        log.info("Crawling website at [${website.url}]")
        val doc = jsoup.readWebPage(website.url)
        val docAsString = doc.toString()
        return HashedWebsite(website.id, doc.location(), docAsString, "")
    }

}


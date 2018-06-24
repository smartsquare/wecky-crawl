package de.smartsquare.wecky.crawler

import de.smartsquare.wecky.domain.HashedWebsite
import de.smartsquare.wecky.domain.Website

class WebsiteCrawler(val jsoup: JsoupWrapper = JsoupWrapper()) {

    fun crawlPage(website: Website): HashedWebsite {

        val doc = jsoup.readWebPage(website.url)
        val docAsString = doc.toString()
        return HashedWebsite(website.id, doc.location(), docAsString)
    }
}


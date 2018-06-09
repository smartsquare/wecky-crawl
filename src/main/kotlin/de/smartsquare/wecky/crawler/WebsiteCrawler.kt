package de.smartsquare.wecky.crawler

import de.smartsquare.wecky.domain.HashedWebsite

class WebsiteCrawler(val baseUrl: String, val jsoup: JsoupWrapper = JsoupWrapper()) {

    fun crawlPage(): HashedWebsite {

        val doc = jsoup.readWebPage(baseUrl)
        return HashedWebsite(doc.location(), doc.toString().hashCode())
    }
}


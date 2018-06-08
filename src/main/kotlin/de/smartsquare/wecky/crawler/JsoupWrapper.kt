package de.smartsquare.wecky.crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 *
 */
class JsoupWrapper {

    fun readWebPage(url: String): Document {
        return Jsoup.connect(url).get()
    }
}
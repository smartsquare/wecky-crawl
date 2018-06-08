package de.smartsquare.wecky.domain

import org.jsoup.nodes.Document

class HashedWebsite(val doc: Document) {
    val url: String = doc.location()
    val hash: Int = doc.toString().hashCode()
}
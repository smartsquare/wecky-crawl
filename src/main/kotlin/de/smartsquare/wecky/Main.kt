package de.smartsquare.wecky

import de.smartsquare.wecky.crawler.WebsiteCrawler
import org.slf4j.LoggerFactory

/**
 * Main script to start crawling locally
 */

val log = LoggerFactory.getLogger("Main")

fun main(args: Array<String>) {
    val websiteCrawler = WebsiteCrawler(args[0])

    val hashedWebsite = websiteCrawler.crawlPage()
    log.info(hashedWebsite.hash.toString())
}

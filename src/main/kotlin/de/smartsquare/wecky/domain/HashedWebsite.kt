package de.smartsquare.wecky.domain

import java.time.Instant

data class HashedWebsite(
        val websiteId: String,
        val url: String,
        val content: String,
        val diff: String?,
        val hashValue: Int = content.hashCode(),
        val crawlDate: Instant = Instant.now()
)
package de.smartsquare.wecky.domain

import java.time.Instant
import java.util.*

data class HashedWebsite(
        val websiteId: String,
        val url: String,
        val content: String,
        val hashValue: Int = content.hashCode(),
        val crawlDate: Instant = Instant.now(),
        val id: String = UUID.randomUUID().toString()
)
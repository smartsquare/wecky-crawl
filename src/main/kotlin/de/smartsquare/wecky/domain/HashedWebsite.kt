package de.smartsquare.wecky.domain

import java.time.Instant

data class HashedWebsite(
        val id: String,
        val url: String,
        val content: String,
        val hash: Int = content.hashCode(),
        val crawlDate: Instant = Instant.now()
)
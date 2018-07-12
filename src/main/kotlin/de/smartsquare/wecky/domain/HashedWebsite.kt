package de.smartsquare.wecky.domain

import java.time.Instant

data class HashedWebsite(
        val websiteId: String,
        val url: String,
        val hashValue: Int,
        val crawlDate: Instant = Instant.now()
)
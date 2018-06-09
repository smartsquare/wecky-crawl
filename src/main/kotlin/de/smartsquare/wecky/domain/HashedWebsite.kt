package de.smartsquare.wecky.domain

import java.time.Instant

data class HashedWebsite(val url: String, val hash: Int, val crawlDate: Instant = Instant.now())
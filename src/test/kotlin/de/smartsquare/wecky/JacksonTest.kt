package de.smartsquare.wecky

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.smartsquare.wecky.domain.Website
import org.junit.Assert.assertNotNull
import org.junit.Test

class JacksonTest {

    val mapper = jacksonObjectMapper()

    @Test
    fun test_jackson_serialization() {
        val jsonString = mapper.writeValueAsString(Website("foobar", "www.foobar.com"))
        assertNotNull(mapper.readValue(jsonString, Website::class.java))
    }
}
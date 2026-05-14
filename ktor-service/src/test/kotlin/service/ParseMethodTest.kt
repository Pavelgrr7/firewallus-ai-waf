package com.pavelryzh.service

import com.pavelryzh.service.dto.HttpMethod
import com.pavelryzh.service.dto.parseMethod
import org.junit.Test
import kotlin.test.assertEquals

class ParseMethodTest {

    @Test
    fun testValidMethods() {
        assertEquals(HttpMethod.GET, parseMethod("GET"))
        assertEquals(HttpMethod.POST, parseMethod("POST"))
        assertEquals(HttpMethod.PUT, parseMethod("put"))
        assertEquals(HttpMethod.PATCH, parseMethod("pAtCh"))
        assertEquals(HttpMethod.DELETE, parseMethod("delete"))
    }

    @Test
    fun testInvalidMethods() {
        assertEquals(HttpMethod.UNKNOWN, parseMethod("invalid"))
        assertEquals(HttpMethod.UNKNOWN, parseMethod("UNKNOWN_METHOD"))
        assertEquals(HttpMethod.UNKNOWN, parseMethod(""))
        assertEquals(HttpMethod.UNKNOWN, parseMethod(" "))
    }
}

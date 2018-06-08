package de.smartsquare.wecky

data class GatewayResponse(val body: String, val headers: Map<String, String>, val statusCode: Int)
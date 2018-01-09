/*
 * Copyright (C) 2016-2017  Irotsoma, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
/*
 * Created by irotsoma on 8/12/16.
 */
package com.irotsoma.cloudbackenc.cloudbackencui

import com.irotsoma.cloudbackenc.common.cloudservices.CloudServiceCallbackURL
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT)
@Profile("test") //used to force use of the application-test.properties file if present
class CloudServiceCallbackControllerTest {
    @LocalServerPort
    private var port: Int = 0
    @Value("\${server.ssl.key-store:#{null}}")
    private var useSSL: String? = null

    //TODO: add SSL test once implemented in controller
    var protocol: String = "http"
    val testUUID = "f8bed9c2-c68b-4ab4-a66a-f16a6b46b768"

    @Test
    fun testAuthenticateCallback(){
        val restTemplate: TestRestTemplate
        if (useSSL!=null && useSSL!="") {
            protocol= "https"
            trustSelfSignedSSL()
            restTemplate = TestRestTemplate(TestRestTemplate.HttpClientOption.SSL)
        } else {
            protocol = "http"
            restTemplate = TestRestTemplate()
        }
        val requestHeaders = HttpHeaders()
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        val httpEntity = HttpEntity<CloudServiceCallbackURL>(CloudServiceCallbackURL(testUUID, "https://irotsoma.com"), requestHeaders)
        val returnValue = restTemplate.postForEntity("$protocol://localhost:$port/cloud-service-callback", httpEntity, Map::class.java)
        assert(returnValue.statusCode == HttpStatus.ACCEPTED)
}}
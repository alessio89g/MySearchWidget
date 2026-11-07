package com.alessio89g.mysearchwidget.overlay

import org.junit.Assert.*
import org.junit.Test

class TypedWebUrlTest {
 @Test fun opensWebAddressesWithoutRewritingTheirPath() {
  assertEquals("https://example.org/A?q=x%20y#Top",typedWebUrl("example.org/A?q=x%20y#Top"))
  assertEquals("http://localhost:8080/path",typedWebUrl("HTTP://localhost:8080/path"))
  assertEquals("https://[::1]:8443/",typedWebUrl("[::1]:8443/"))
 }

 @Test fun leavesQueriesAndUnsupportedAddressesToTheSearchEngine() {
  for(input in listOf("hello","example.org docs","person@example.org","ftp://example.org","https:///example.org","example.org:99999")) {
   assertNull(input,typedWebUrl(input))
  }
 }
}

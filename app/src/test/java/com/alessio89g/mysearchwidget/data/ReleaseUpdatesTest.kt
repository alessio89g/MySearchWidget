package com.alessio89g.mysearchwidget.data
import com.alessio89g.mysearchwidget.config.ReleaseUpdates
import org.junit.Assert.*
import org.junit.Test

class ReleaseUpdatesTest {
 @Test fun comparesVersionsNumericallyAndRejectsPrereleases() {
  assertTrue(ReleaseUpdates.newer("v1.14.0","1.13.4"))
  assertTrue(ReleaseUpdates.newer("v1.10.0","1.9.9"))
  assertFalse(ReleaseUpdates.newer("v1.14.0","1.14.0"))
  assertFalse(ReleaseUpdates.newer("v1.14.0-beta1","1.13.4"))
  assertFalse(ReleaseUpdates.newer("v1.13.4","1.14.0"))
 }
 @Test fun onlyAcceptsStableApkFromOriginalRepository() {
  val json="""{"tag_name":"v1.14.0","draft":false,"prerelease":false,"assets":[{"name":"MySearchWidget-1.14.0.apk","browser_download_url":"https://github.com/alessio89g/MySearchWidget/releases/download/v1.14.0/MySearchWidget-1.14.0.apk"}]}"""
  assertNotNull(ReleaseUpdates.parse(json,"1.13.4"))
  assertNull(ReleaseUpdates.parse(json.replace("\"draft\":false","\"draft\":true"),"1.13.4"))
  assertNull(ReleaseUpdates.parse(json.replace("\"prerelease\":false","\"prerelease\":true"),"1.13.4"))
  assertNull(ReleaseUpdates.parse(json.replace("https://github.com/","https://other.example/"),"1.13.4"))
  assertNull(ReleaseUpdates.parse(json,"1.14.0"))
 }
}

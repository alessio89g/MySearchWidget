package com.alessio89g.mysearchwidget.overlay

import java.net.URI
import java.util.Locale

/** Recognizes web addresses while leaving ordinary input to the search engine. */
internal fun typedWebUrl(input: String): String? {
 val hasWebScheme=input.startsWith("https://",ignoreCase=true) ||
  input.startsWith("http://",ignoreCase=true)
 val candidate=if(hasWebScheme) input else "https://$input"
 val uri=runCatching {URI(candidate).parseServerAuthority()}.getOrNull() ?: return null
 val host=uri.host ?: return null
 if(uri.rawUserInfo!=null || uri.port>65535) return null
 if(!hasWebScheme && !host.equals("localhost",ignoreCase=true) && '.' !in host && !host.startsWith('[')) return null
 return uri.scheme.lowercase(Locale.ROOT)+candidate.substring(uri.scheme.length)
}

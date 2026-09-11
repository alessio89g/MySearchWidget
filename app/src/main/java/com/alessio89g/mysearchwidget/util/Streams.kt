package com.alessio89g.mysearchwidget.util
import java.io.InputStream
import java.io.ByteArrayOutputStream
// InputStream.readNBytes is not available on every supported Android release.
fun InputStream.readLimited(limit:Int):ByteArray {
 val output=ByteArrayOutputStream();val buffer=ByteArray(8192)
 while(output.size()<limit){val count=read(buffer,0,minOf(buffer.size,limit-output.size()));if(count<0)break;if(count==0)continue;output.write(buffer,0,count)}
 return output.toByteArray()
}

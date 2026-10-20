package com.comma.bulletin;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

public final class HttpsFeedSource implements FeedSource {
 private final String endpoint;
 public HttpsFeedSource(String endpoint){this.endpoint=endpoint;}
 public String fetch() throws Exception {
  // Unique query and no-cache bypass stale CDN and device HTTP caches.
  URL u=new URL(endpoint+(endpoint.contains("?")?"&":"?")+"t="+System.currentTimeMillis());
  if(!"https".equals(u.getProtocol()))throw new IOException("HTTPS required");
  HttpsURLConnection c=(HttpsURLConnection)u.openConnection();
  c.setUseCaches(false);c.setConnectTimeout(10000);c.setReadTimeout(10000);c.setInstanceFollowRedirects(false);
  c.setRequestProperty("Accept","application/json");c.setRequestProperty("Cache-Control","no-cache, no-store");c.setRequestProperty("Pragma","no-cache");
  try {
   if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());
   try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
    byte[] buf=new byte[8192];int n,total=0;
    while((n=in.read(buf))!=-1){total+=n;if(total>8388608)throw new IOException("Feed too large");out.write(buf,0,n);}
    String raw=out.toString(StandardCharsets.UTF_8.name());Item.parse(raw);android.util.Log.i("BulletinSync","Feed fetched: "+endpoint);return raw;
   }
  }finally{c.disconnect();}
 }
}

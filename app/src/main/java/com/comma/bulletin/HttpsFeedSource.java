package com.comma.bulletin;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

public final class HttpsFeedSource implements FeedSource {
 private final String endpoint;
 public HttpsFeedSource(String endpoint){this.endpoint=endpoint;}
 public String fetch() throws Exception {
  URL u=new URL(endpoint); if(!"https".equals(u.getProtocol())) throw new IOException("HTTPS required");
  HttpsURLConnection c=(HttpsURLConnection)u.openConnection();
  c.setConnectTimeout(15000); c.setReadTimeout(15000); c.setInstanceFollowRedirects(false); c.setRequestProperty("Accept","application/json");
  try {
   if(c.getResponseCode()!=200) throw new IOException("HTTP "+c.getResponseCode());
   try(InputStream in=c.getInputStream(); ByteArrayOutputStream out=new ByteArrayOutputStream()){
    byte[] buf=new byte[8192]; int n,total=0;
    while((n=in.read(buf))!=-1){total+=n; if(total>1048576) throw new IOException("Feed too large"); out.write(buf,0,n);}
    String raw=out.toString(StandardCharsets.UTF_8.name()); Item.parse(raw); return raw;
   }
  } finally {c.disconnect();}
 }
}

package com.comma.bulletin;
/** A fresh primary request is attempted on every sync, even after a fallback. */
public final class FallbackFeedSource implements FeedSource {
 private final FeedSource primary,fallback;
 public FallbackFeedSource(FeedSource primary,FeedSource fallback){this.primary=primary;this.fallback=fallback;}
 public String fetch() throws Exception {
  try{return validated(primary);}catch(Exception first){try{return validated(fallback);}catch(Exception second){second.addSuppressed(first);throw second;}}
 }
 private String validated(FeedSource source) throws Exception {String raw=source.fetch();Item.parse(raw);return raw;}
}

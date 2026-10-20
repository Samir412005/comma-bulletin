package com.comma.bulletin;

/** Replace this single source at composition time when the bot API is ready. */
public interface FeedSource { String fetch() throws Exception; }

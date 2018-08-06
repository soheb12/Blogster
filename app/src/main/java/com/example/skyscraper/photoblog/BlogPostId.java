package com.example.skyscraper.photoblog;

public class BlogPostId {

    public String blogPostId;

    public <T extends BlogPostId> T withId(final String id)
    {
        this.blogPostId = id;
        return (T) this;
    }
}

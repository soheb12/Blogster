package com.example.skyscraper.photoblog;


import java.util.Date;

public class BlogPost extends BlogPostId{

    String user_id , desc , image_uri , thumb_uri ;
    Date timestamp;

    public BlogPost()
    {

    }

    public BlogPost(String user_id, String desc, String image_uri, String thumb_uri,Date timestamp) {
        this.user_id = user_id;
        this.desc = desc;
        this.image_uri = image_uri;
        this.thumb_uri = thumb_uri;
        this.timestamp = timestamp;
    }

//setters

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public void setThumb_uri(String thumb_uri) {
        this.thumb_uri = thumb_uri;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

//getters

    public String getUser_id() {

        return user_id;
    }

    public String getDesc() {
        return desc;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public String getThumb_uri() {
        return thumb_uri;
    }

    public Date getTimestamp() {
        return timestamp;
    }



}

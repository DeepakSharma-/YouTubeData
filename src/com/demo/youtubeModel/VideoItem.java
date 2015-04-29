package com.demo.youtubeModel;

import java.math.BigInteger;
/**We use this class to store the following information about a YouTube video:
  YouTube ID
   title
   description
   thumbnail URL
   commentCount
 * */

public class VideoItem {
    private String title;
    private String description;
    private String thumbnailURL;
    private String id;
    private BigInteger commentCount;
     
    public String getId() {
        return id;
    }
     
    public void setId(String id) {
        this.id = id;
    }
     
    public String getTitle() {
        return title;
    }
     
    public void setTitle(String title) {
        this.title = title;
    }
     
    public String getDescription() {
        return description;
    }
     
    public void setDescription(String description) {
        this.description = description;
    } 
     
    public String getThumbnailURL() {
        return thumbnailURL;
    }
     
    public void setThumbnailURL(String thumbnail) {
        this.thumbnailURL = thumbnail;      
    }

	public BigInteger getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(BigInteger commentCount) {
		this.commentCount = commentCount;
	}
    
    
         
}
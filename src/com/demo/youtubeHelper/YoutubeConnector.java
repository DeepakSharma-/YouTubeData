package com.demo.youtubeHelper;

/**
 * To avoid having to deal with the YouTube Data API directly in our Activity, 
 *  This class has the following member variables:
 *  - an instance of the YouTube class that will be used for communicating with the YouTube API.
 *  - an instance of YouTube.Search.List to represent a search query
 *  - YouTube API key as a static String
 We initialize the above variables in the constructor. 
 To initialize the instance of YouTube, the YouTube.Builder class has to be used. The classes that will be responsible for the network connection and the JSON processing are passed to the builder.
 * */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.demo.youtubeModel.SearchQuery;
import com.demo.youtubeModel.VideoItem;
import com.demo.youtubesearch.R;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

public class YoutubeConnector {
	private YouTube youtube;
	private YouTube.Search.List query;
	public static String NEXT_PAGE_TOKEN = null;
	public long maxResultSize = 10L;
	public static long LAST_VIDEO_COUNT = 0L;
	private YouTube.Videos.List videoDetails;

	// private YouTube.Videos.GetRating videoRatings;
	// Your developer key goes here
	public static final String KEY = "AIzaSyAMOv2Ii9QfmARXZT72a_3PzoAnn-1rMAg";

	public YoutubeConnector(Context context) {
		youtube = new YouTube.Builder(new NetHttpTransport(),
				new JacksonFactory(), new HttpRequestInitializer() {
					@Override
					public void initialize(HttpRequest hr) throws IOException {
					}
				}).setApplicationName(context.getString(R.string.app_name))
				.build();

		try {
			videoDetails = youtube.videos().list("id,snippet,statistics");
			videoDetails.setKey(KEY);
			videoDetails
					.setFields("items(snippet/title,snippet/description,statistics/commentCount,snippet/thumbnails/default/url)");
			query = youtube.search().list("id,snippet");
			query.setKey(KEY);
			query.setType("video");
			query.setMaxResults(maxResultSize);
		} catch (IOException e) {
			Log.d("YC", "Could not initialize: " + e);
		}
	}

	/**
	 * search method is used to create a search request. The list method is then
	 * used to mention the details we want in the search results. After getting
	 * id from search result we use VideoDetail class of Youtube api and set
	 * values in VideoItem List.
	 * */
	public List<VideoItem> search(String keywords) {
		List<VideoItem> items = new ArrayList<VideoItem>();
		query.setQ(keywords);
		query.setFields("nextPageToken,items(id/videoId)");
		if (NEXT_PAGE_TOKEN != null && NEXT_PAGE_TOKEN.length() > 0) {
			query.setPageToken(NEXT_PAGE_TOKEN);
		}

		try {
			SearchListResponse response = query.execute();
			List<SearchResult> results = response.getItems();
			NEXT_PAGE_TOKEN = response.getNextPageToken();
			if (results != null && results.size() > 0) {
				String videoIdList = results.get(0).getId().getVideoId();

				for (int i = 1; i < results.size(); i++) {
					videoIdList += "," + results.get(i).getId().getVideoId();
				}
				videoDetails.setId(videoIdList);
				VideoListResponse videoDetailList = videoDetails.execute();
				List<Video> videoDetail = videoDetailList.getItems();
				for (Video video : videoDetail) {
					VideoItem item = new VideoItem();
					item.setTitle(video.getSnippet().getTitle());
					item.setDescription(video.getSnippet().getDescription());
					item.setThumbnailURL(video.getSnippet().getThumbnails()
							.getDefault().getUrl());
					item.setId(video.getId());
					item.setCommentCount(video.getStatistics()
							.getCommentCount());
					items.add(item);
				}
			}
			return items;
		} catch (IOException e) {
			Log.d("YC", "Could not search: " + e);
			return null;
		}
	}

	/**
	 * This method will return a List to fill autocomplete adapter. This method
	 * will hit Youtube client api and fetch title of videos to show it in
	 * adapter
	 * */
	public List<SearchQuery> searchVideoTitle(String keywords) {
		query.setQ(keywords);
		query.setFields("items(id/videoId,snippet/title)");
		try {
			SearchListResponse response = query.execute();
			List<SearchResult> results = response.getItems();

			List<SearchQuery> items = new ArrayList<SearchQuery>();
			for (SearchResult result : results) {

				SearchQuery item = new SearchQuery();
				item.setTitle(result.getSnippet().getTitle());
				items.add(item);
			}
			return items;
		} catch (IOException e) {
			Log.d("YC", "Could not search: " + e);
			return null;
		}
	}
}

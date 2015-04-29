package com.demo.youtubesearch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.youtubeHelper.EndlessScrollListener;
import com.demo.youtubeHelper.YoutubeConnector;
import com.demo.youtubeModel.SearchQuery;
import com.demo.youtubeModel.VideoItem;
import com.squareup.picasso.Picasso;

/**
 * This class has fields that represent the views we mentioned in
 * activity_search.xml. It also has a Handler to make updates on the user
 * interface thread.
 * 
 * In the onCreate method, we initialize the views and add an
 * OnEditorActionListener to the AutoCompleteTextView to know when the user has
 * finished entering keywords.
 * */
public class VideoSearchActivity extends Activity {

	private AutoCompleteTextView searchInput;
	private ListView videosFound;
	private SearchQuery searchQuery;
	private Handler handler;
	List<SearchQuery> arrayQuery;
	YoutubeSearchAuto searchVideoTitles;
	ArrayAdapter adapter;

	String search = "http://suggestqueries.google.com/complete/search?client=youtube&ds=yt&q=";

	private List<VideoItem> searchResults;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		searchInput = (AutoCompleteTextView) findViewById(R.id.search_input);
		videosFound = (ListView) findViewById(R.id.videos_found);
		arrayQuery = new ArrayList<SearchQuery>();
		adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
				arrayQuery);
		searchInput.setAdapter(adapter);
		handler = new Handler();

		searchResults = new ArrayList<VideoItem>();
		updateVideosFound();
		searchInput.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Toast.makeText(VideoSearchActivity.this,
						arrayQuery.get(arg2).getTitle(), 4000).show();
				searchInput.setText(arrayQuery.get(arg2).getTitle());
				hideSoftKeyboard(VideoSearchActivity.this);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				searchInput.setSelection(searchInput.getText().length());
				searchResults = new ArrayList<VideoItem>();
				searchOnYoutube(arrayQuery.get(arg2).getTitle());
			}
		});
		searchOnYoutube("");
		searchInput
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							hideSoftKeyboard(VideoSearchActivity.this);
							searchResults = new ArrayList<VideoItem>();
							searchOnYoutube(v.getText().toString());
							return false;
						}
						return true;
					}
				});

		searchInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				new YoutubeSearchAuto().execute(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		videosFound.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				// Triggered only when new data needs to be appended to the list
				// Add whatever code is needed to append new items to your
				// AdapterView

				if (YoutubeConnector.NEXT_PAGE_TOKEN != null)
					searchOnYoutube(searchInput.getEditableText().toString());
				/*
				 * YoutubeConnector yc = new YoutubeConnector(
				 * VideoSearchActivity.this);
				 * 
				 * List<VideoItem> newVideoList =
				 * yc.search(searchInput.getEditableText().toString());
				 * searchResults.addAll(newVideoList); updateVideosFound();
				 */
				Toast.makeText(
						VideoSearchActivity.this,
						"NextToken = " + YoutubeConnector.NEXT_PAGE_TOKEN
								+ "\nLoad more" + totalItemsCount + "\nPage"
								+ page, 3000).show();
			}
		});

		addClickListener();

	}

	/**
	 * In this method, we create a new Thread to initialize a YoutubeConnector
	 * instance and run its search method. A new thread is necessary, because
	 * network operations cannot be performed on the the main user interface
	 * thread. If you forget to do this, you will face a runtime exception. Once
	 * the results are available, the handler is used to update the user
	 * interface.
	 */
	private void searchOnYoutube(final String keywords) {
		new Thread() {
			public void run() {
				YoutubeConnector yc = new YoutubeConnector(
						VideoSearchActivity.this);
				List<VideoItem> newVideoList = yc.search(keywords);
				if (newVideoList != null)
					searchResults.addAll(newVideoList);
				handler.post(new Runnable() {
					public void run() {
						if (lvVideoAdapter != null)
							lvVideoAdapter.notifyDataSetChanged();
					}
				});
			}
		}.start();
	}

	ArrayAdapter<VideoItem> lvVideoAdapter;

	/**
	 * In the updateVideosFound method, we generate an ArrayAdapter and pass it
	 * on to the ListView to display the search results. In the getView method
	 * of the adapter, we inflate the video_item.xml layout and update its views
	 * to display information about the search result.
	 * 
	 * The Picasso library's load method is used to fetch the thumbnail of the
	 * video and the into method is used to pass it to the ImageView.
	 */
	private void updateVideosFound() {
		lvVideoAdapter = new ArrayAdapter<VideoItem>(getApplicationContext(),
				R.layout.video_item, searchResults) {
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return searchResults.size();
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				if (convertView == null) {
					convertView = getLayoutInflater().inflate(
							R.layout.video_item, parent, false);
				}
				ImageView thumbnail = (ImageView) convertView
						.findViewById(R.id.video_thumbnail);
				TextView title = (TextView) convertView
						.findViewById(R.id.video_title);
				TextView description = (TextView) convertView
						.findViewById(R.id.video_description);
				TextView comments = (TextView) convertView
						.findViewById(R.id.comments);
				VideoItem searchResult = searchResults.get(position);

				Picasso.with(getApplicationContext())
						.load(searchResult.getThumbnailURL()).into(thumbnail);
				title.setText(searchResult.getTitle());
				comments.setText("Comments : " + searchResult.getCommentCount());
				description.setText(searchResult.getDescription());
				convertView.setTag(R.string.tag, searchResult.getId());
				return convertView;

			}
		};
		videosFound.setAdapter(lvVideoAdapter);
	}

	private void addClickListener() {
		videosFound
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> av, View v, int pos,
							long id) {
						String videoId = null;
						if (v.getTag(R.string.tag) != null)
							videoId = v.getTag(R.string.tag).toString();
						Toast.makeText(VideoSearchActivity.this, "" + videoId,
								4000).show();
						 Intent intent = new Intent(VideoSearchActivity.this,
						 PlayerActivity.class);
						 intent.putExtra("VIDEO_ID", videoId);
						 startActivity(intent);
					}

				});
	}

	// This method will hide keyboard
	public void hideSoftKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
				.getWindowToken(), 0);
	}

	// Fetches all video from youtube to set value on adapter for AutoComplete
	private class YoutubeSearchAuto extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... searchWord) {
			String data = "";
			try {
				YoutubeConnector yc = new YoutubeConnector(
						VideoSearchActivity.this);
				arrayQuery = yc.searchVideoTitle(searchWord[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			adapter = new ArrayAdapter<SearchQuery>(VideoSearchActivity.this,
					android.R.layout.simple_list_item_1, arrayQuery) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					final TextView txtVideoTitle;
					if (convertView != null) {
						txtVideoTitle = (TextView) convertView;
					} else {
						txtVideoTitle = (TextView) ((LayoutInflater) VideoSearchActivity.this
								.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
								.inflate(android.R.layout.simple_list_item_1,
										parent, false);
					}
					txtVideoTitle.setText(arrayQuery.get(position).getTitle());
					return txtVideoTitle;
				}
			};
			searchInput.setAdapter(adapter);
			// parse data here and notify adapter after setting data in
			// SearchQuery class.
			adapter.notifyDataSetChanged();
		}
	}

}
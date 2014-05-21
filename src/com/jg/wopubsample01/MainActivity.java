package com.jg.wopubsample01;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.mopub.mobileads.AdConfiguration;
import com.mopub.mobileads.BaseVideoPlayerActivity;
import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.factories.VastManagerFactory;
import com.mopub.mobileads.util.vast.VastManager;
import com.mopub.mobileads.util.vast.VastVideoConfiguration;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.os.Build;

public class MainActivity extends ActionBarActivity implements VastManager.VastManagerListener {
	
	private final static String TAG = MainActivity.class.getSimpleName();
	private VastManager wVastManager;
	private static final String VAST_URL_STRING = "http://ads.videohub.tv/vast2.0?vpaid=N&vpaidType=full&wrapper=true&partnerId=441864&campaignId=1776694&videoPageUrl=[VIDEO_HOMEPAGE]&ssSL=[CURRENT_PAGE_URL]&videoId=[ID]&videoTitle=[TITLE]&videoDesc=[DESCRIPTION]&videoUrl=[VIDEO_URL]&random=VIEWING_IDCURRENT_TIME&ssADcC=iframe,html,static";
	private String daVastXml;
	private VastVideoConfiguration wVastVideoConfiguration;
	private AdConfiguration wAdConfiguration;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		
		private MoPubView moPubView;
		private Button wStartVast;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			wStartVast = (Button) getActivity().findViewById(R.id.startVast);
			if (wStartVast != null)
				wStartVast.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						MainActivity ma = (MainActivity) getActivity();
						ma.startWaVast();
					}
				});
			moPubView = (MoPubView) getActivity().findViewById(R.id.adview);
			moPubView.setAdUnitId("0c727f9dc1214df6a7fd9fffcddda463"); // Enter your Ad Unit ID from www.mopub.com
			moPubView.loadAd();
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			moPubView.destroy();
		}
	}

	@Override
	public void onVastVideoConfigurationPrepared(VastVideoConfiguration vastVideoConfiguration) {
        if (vastVideoConfiguration == null) {
            //mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.VIDEO_DOWNLOAD_ERROR);
            return;
        }

        wVastVideoConfiguration = vastVideoConfiguration;
        //mCustomEventInterstitialListener.onInterstitialLoaded();
	}
	
	public void startWaVast() {
		new GetVastXmlWithTask().execute("");
	}

	private class GetVastXmlWithTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			String responseString = null;
			try {
				responseString = getHttpResponse();
			} catch (final Exception ex) {
				Log.d("MainActivity with Task", ex.toString());
			}
			return responseString;
		}

		@Override
		protected void onPostExecute(String response) {
			super.onPostExecute(response);
			try {
				Log.d(TAG, "We're post-executing");
				daVastXml = response;
				wVastManager = VastManagerFactory.create(MainActivity.this);
				wVastManager.prepareVastVideoConfiguration(daVastXml, MainActivity.this);
				Log.d(TAG, "We're post-prepareVastConfiging");
				wAdConfiguration = new AdConfiguration(MainActivity.this);
				BaseVideoPlayerActivity.startVast(MainActivity.this, wVastVideoConfiguration, wAdConfiguration);
			} catch (final Exception ex) {
				Log.d("MainActivity with Task", ex.toString());
			}
		}
	}

	private String getHttpResponse() throws IOException {
		String responseString = null;
		DefaultHttpClient client = new DefaultHttpClient();
		Log.d("MainActivity flickr url", VAST_URL_STRING);
		HttpGet get = new HttpGet(VAST_URL_STRING);
		HttpResponse resp = client.execute(get);
		HttpEntity entity = resp.getEntity();
		InputStream is = entity.getContent();
		responseString = readToEnd(is);
		is.close();
		Log.d("MainActivity responseString", responseString);
		return responseString;
	}

	private static String readToEnd(InputStream input) throws IOException {
		DataInputStream dis = new DataInputStream(input);
		byte[] stuff = new byte[1024];
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		int read = 0;
		while ((read = dis.read(stuff)) != -1) {
			buff.write(stuff, 0, read);
		}

		return new String(buff.toByteArray());
	}

}

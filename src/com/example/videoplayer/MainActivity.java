package com.example.videoplayer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;

public class MainActivity extends Activity {
	static View frame1;
	static player SurfaceView;
	static boolean pause=false;
	static ProgressBar progressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	@Override
    protected void onDestroy() {
		pause=false;
		super.onDestroy();
	}
	@Override
	public void onPause() {
		pause=true; 
		super.onPause(); 
	}
	@Override
	public void onResume() {

		super.onResume();
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
	public static class PlaceholderFragment extends Fragment  {
		
		 
		public PlaceholderFragment() {} 
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			frame1=rootView.findViewById(R.id.frame1);
			
			SurfaceView = (player)rootView.findViewById(R.id.textureView1);
			
			//ftp://master255.org/%D0%9A%D0%BB%D0%B8%D0%BF%D1%8B/S/SKRILLEX/Skrillex%20-%20Summit%20(feat.%20Ellie%20Goulding)%20%5BVideo%20by%20Pilerats%5D.mp4
			//http://master255.org/res/Клипы/S/SKRILLEX/Skrillex - Summit (feat. Ellie Goulding) [Video by Pilerats].mp4
			SurfaceView.setVideoPath("http://master255.org/res/%D0%9A%D0%BB%D0%B8%D0%BF%D1%8B/S/SKRILLEX/Skrillex%20-%20Summit%20(feat.%20Ellie%20Goulding)%20%5BVideo%20by%20Pilerats%5D.mp4", getActivity());
			/*Uri uri = Uri.parse("http://master255.org/res/%D0%9A%D0%BB%D0%B8%D0%BF%D1%8B/S/SKRILLEX/Skrillex%20-%20Summit%20(feat.%20Ellie%20Goulding)%20%5BVideo%20by%20Pilerats%5D.mp4");
		    VideoActivity.openVideo(getActivity(), uri, "video title");*/
		    //not work!
			SurfaceView.setMediaController(new mediac(getActivity(), frame1));

			
			SurfaceView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
	            @Override
	            public void onPrepared(final MediaPlayer mp) {
	                	SurfaceView.startVideoPlayback();
	            }
	        }); 
			return rootView;
		}
		
		
	    public class mediac extends MediaController
	    {
	    	
	        public mediac(Context context, View anchor)
	        {
	            super(context);
	            super.setAnchorView(anchor);
	        }

	        @Override
	        public void setAnchorView(View view)
	        {}
	        

	        
	    }
	}

}

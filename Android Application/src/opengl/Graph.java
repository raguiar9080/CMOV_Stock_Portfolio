package opengl;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class Graph extends Fragment {

	private GLSurfaceView mView; 
	private PieChart mRenderer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		
		//TODO dummy data
		ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		elems.add(new BasicNameValuePair("MSFT", Integer.valueOf(20).toString()));
		elems.add(new BasicNameValuePair("TWTW", Integer.valueOf(30).toString()));
		elems.add(new BasicNameValuePair("NEW", Integer.valueOf(10).toString()));
		elems.add(new BasicNameValuePair("VALV", Integer.valueOf(45).toString()));
		elems.add(new BasicNameValuePair("TTTMO", Integer.valueOf(3).toString()));
		elems.add(new BasicNameValuePair("LOLED", Integer.valueOf(30).toString()));
		elems.add(new BasicNameValuePair("LAST", Integer.valueOf(20).toString()));
		

		
		mView = new GLSurfaceView(getActivity().getBaseContext());
		// Set to use OpenGL ES 2.0
		//mView.setEGLContextClientVersion(2); 
		
		mRenderer = new PieChart(elems, getActivity().getBaseContext()); 
		mView.setRenderer(mRenderer); 
		//getActivity().setContentView(mView);
		return mView; 
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		return mRenderer.onTouchEvent(event); 
	}
}
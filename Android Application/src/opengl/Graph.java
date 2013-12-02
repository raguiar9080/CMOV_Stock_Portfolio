package opengl;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.graphics.PixelFormat;
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
		
		/*ArrayList<Pair<String, ArrayList<Double>>> elems = new ArrayList<Pair<String,ArrayList<Double>>>();
		Pair<String, ArrayList<Double>> elem = new Pair<String, ArrayList<Double>> ("Average", (new ArrayList<Double>()));
		elem.second.add(1.0);
		elem.second.add(1.3);
		elem.second.add(3.5);
		elem.second.add(5.0);
		elems.add(elem);
		elem = new Pair<String, ArrayList<Double>> ("Max", (new ArrayList<Double>()));
		elem.second.add(4.0);
		elem.second.add(7.21);
		elem.second.add(13.5);
		elem.second.add(5.6);
		elem.second.add(9.1);
		elems.add(elem);*/
		
		
		mView = new GLSurfaceView(getActivity().getBaseContext());
		mView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		mView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		mView.setZOrderOnTop(true);
		
		// Set to use OpenGL ES 2.0
		//mView.setEGLContextClientVersion(2); 
		
		mRenderer = new PieChart(elems, getActivity().getBaseContext()); 
		//mRenderer = new LineChart(elems, getActivity().getBaseContext());
		mView.setRenderer(mRenderer);
		return mView; 
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		return mRenderer.onTouchEvent(event); 
	}
}
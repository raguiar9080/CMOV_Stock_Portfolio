package opengl;

import java.util.ArrayList;

import org.apache.http.NameValuePair;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class PieGraph extends Fragment {

	private GLSurfaceView mView; 
	private PieChart mRenderer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mView = new GLSurfaceView(getActivity().getBaseContext());
		mView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		mView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		mView.setZOrderOnTop(true);
		
		mRenderer = new PieChart(null, getActivity().getBaseContext()); 
		//mRenderer = new LineChart(elems, getActivity().getBaseContext());
		mView.setRenderer(mRenderer);
		return mView;
	}
	
	public void drawPie(ArrayList<NameValuePair> elems)
	{
		if(elems!= null && elems.size()>0)
			mRenderer.restart(elems);
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		return mRenderer.onTouchEvent(event); 
	}
}
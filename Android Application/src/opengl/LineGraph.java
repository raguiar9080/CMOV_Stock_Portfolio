package opengl;

import java.util.ArrayList;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import common.Series;

public class LineGraph extends Fragment {

	private GLSurfaceView mView; 
	private LineChart mRenderer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		
		mView = new GLSurfaceView(getActivity().getBaseContext());
		mView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		mView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		mView.setZOrderOnTop(true);
		
		// Set to use OpenGL ES 2.0
		//mView.setEGLContextClientVersion(2); 
		
		mRenderer = new LineChart(null, getActivity().getBaseContext()); 
		mView.setRenderer(mRenderer);
		return mView;
	}
	
	public void drawLine(ArrayList<Series> arrayList)
	{
		mRenderer.restart(arrayList);
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		return mRenderer.onTouchEvent(event); 
	}
}
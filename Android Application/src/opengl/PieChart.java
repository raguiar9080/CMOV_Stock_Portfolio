package opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import opengl.text.GLText;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.view.MotionEvent;

class PieChart implements Renderer {
	private final int Dimensions = 3;
	private final Short segments = 360;
	private final Double radius = 1.0;
	
	private final float[] Colors = {
			0.0f , 0.0f , 0.0f,	
			1.0f , 0.0f , 0.0f,
			0.0f , 1.0f , 0.0f,
			0.0f , 0.0f , 1.0f,
			0.0f , 1.0f , 1.0f,
	};

	private final float[] BackColor = {
			0.0f, 0.0f, 0.0f,0.0f
	};

	private final float[] TextColor = {
			0.0f, 0.0f, 0.0f, 1.0f
	};

	private ArrayList<NameValuePair> data;
	private Double sum_values;

	private Context context= null;
	private FloatBuffer mPieVB = null; 
	private FloatBuffer mLabelVB = null;
	private ShortBuffer mPieIB = null; 
	private ShortBuffer mLabelIB = null;
	private int[] mNumOfIndices = null;   
	private GLText glText;

	private int width;
	private int height; 

	public float mAngleX = 0.0f; 
	public float mAngleY = 0.0f; 
	public float mAngleZ = 0.0f; 
	/*private float mPreviousX; 
	private float mPreviousY; 
	private final float TOUCH_SCALE_FACTOR = 0.6f;*/

	public PieChart(ArrayList<NameValuePair> elems, Context context) {
		this.context = context;
		this.data = elems;

		//create N partitions in pie chart
		if(elems!=null)
		{
			mNumOfIndices = new int[elems.size()];

			sum_values = 0.0;
			for (NameValuePair value : elems)
				sum_values += Double.parseDouble(value.getValue());
		}
	} 

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig arg1) {
		gl.glClearColor(BackColor[0],BackColor[1],BackColor[2],BackColor[3]); 

		gl.glDisable(GL10.GL_DITHER);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);

		//Cannot Enable due to text, dunno why :S
		//gl.glEnable(GL10.GL_DEPTH_TEST);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		// Create the GLText
		glText = new GLText( gl, context.getAssets() );

		// Load the font from file (set size + padding), creates the texture
		// NOTE: after a successful call to this the font is ready for rendering!
		glText.load( "Roboto-Regular.ttf", 16, 2, 2 );  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)

		// Get all the buffers ready
		setAllBuffers();
	}

	public void restart(ArrayList<NameValuePair> elems) {
		this.data = elems;
		
		//create N partitions in pie chart
		if(elems!=null)
		{
			mNumOfIndices = new int[elems.size()];

			sum_values = 0.0;
			for (NameValuePair value : elems)
				sum_values += Double.parseDouble(value.getValue());		
		}
		setAllBuffers();
	}

	public void onDrawFrame(GL10 gl) { 
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); 
		gl.glMatrixMode(GL10.GL_MODELVIEW); 
		gl.glLoadIdentity();
		this.DrawPieLabels(gl);
		this.DrawPieChart(gl);
	}

	//just a function to automate writing text
	public void DrawText(GL10 gl, String text, float pos_x, float pos_y)
	{
		// enable texture + alpha blending
		// NOTE: this is required for text rendering! we could incorporate it into
		// the GLText class, but then it would be called multiple times (which impacts performance).
		gl.glEnable( GL10.GL_TEXTURE_2D );              // Enable Texture Mapping
		gl.glEnable( GL10.GL_BLEND );                   // Enable Alpha Blend
		gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );  // Set Alpha Blend Function

		glText.begin( TextColor[0], TextColor[1], TextColor[2], TextColor[3]);
		glText.draw(text, pos_x, pos_y );
		glText.end();

		// disable texture + alpha
		gl.glDisable( GL10.GL_BLEND );                  // Disable Alpha Blend
		gl.glDisable( GL10.GL_TEXTURE_2D );             // Disable Texture Mapping
	}

	public void DrawPieLabels(GL10 gl) {
		if(data==null || data.size()==0)
		{
			glText.load( "Roboto-Regular.ttf", 80, 2, 2 );
			DrawText(gl, "NO DATA", width/2 - glText.getLength("NO DATA")/2, height/2 - glText.getCharHeight()/2);
			glText.load( "Roboto-Regular.ttf", 14, 2, 2 );
			return;
		}

		int label_size = 40;
		int text_center_y;
		int text_center_x;

		//landscape
		if(width > height)
		{
			text_center_y = height;
			text_center_x = height;
		}
		else
		{
			text_center_y = height - width;
			text_center_x = 0;
		}

		text_center_y -= (10 + (1.5 * glText.getCharHeight()));
		text_center_x += 10;

		float maxLabelSize = -1;
		for(int i = 0 ; i < data.size() ; i++)
			if(glText.getLength(data.get(i).getName()) > maxLabelSize)
				maxLabelSize = glText.getLength(data.get(i).getName());

		//1 label(offset) + 1 label(real) + 0.5 label(offset) + text
		maxLabelSize += label_size * 2.5f;
		int numberLabelsPerRow = (int)((width - text_center_x) / maxLabelSize);


		gl.glPushMatrix();
		//go to begining position
		gl.glTranslatef(text_center_x,  text_center_y, 0.0f);

		gl.glPushMatrix();
		int row = 0;
		for(int i = 0 ; i < data.size() ; i++)
		{
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mLabelVB); 

			gl.glPushMatrix();

			gl.glTranslatef(label_size,  0.0f, 0.0f);
			gl.glScalef(label_size , glText.getCharHeight(), 0.0f);

			gl.glColor4f(Colors[(i*3) % Colors.length], Colors[((i*3) + 1) % Colors.length], Colors[((i*3) + 2) % Colors.length], 1.0f);
			gl.glDrawElements(GL10.GL_TRIANGLES, mLabelIB.capacity(), 
					GL10.GL_UNSIGNED_SHORT, mLabelIB); 

			gl.glPopMatrix();

			DrawText(gl, data.get(i).getName(), label_size * 2.5f, 0.0f );

			row++;
			if(row >= numberLabelsPerRow)
			{
				//pass next line
				gl.glPopMatrix();
				gl.glTranslatef(0.0f,  - 1.5f * glText.getCharHeight(), 0.0f);
				gl.glPushMatrix();
				row = 0;
			}
			else
				//pass to next row
				gl.glTranslatef(maxLabelSize,  0.0f, 0.0f);
		}
		//first pop to pop line to line matrix saved
		gl.glPopMatrix();
		gl.glPopMatrix();
	}


	public void DrawPieChart(GL10 gl) {
		if(data==null || data.size()==0)
			return;

		gl.glPushMatrix();
		//landscape
		if(width > height)
		{
			gl.glTranslatef(height/2, height/2, 0.0f);
			gl.glScalef(height/2, height/2, 0.0f);
		}
		else
		{
			gl.glTranslatef(width/2, height - width/2, 0.0f);
			gl.glScalef(width/2, width/2, 0.0f);
		}

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mPieVB); 

		// Draw all triangles
		int offset = 0;
		for(int i = 0 ; i < data.size() ; i++)
		{
			gl.glColor4f(Colors[(i*3) % Colors.length], Colors[((i*3) + 1) % Colors.length], Colors[((i*3) + 2) % Colors.length], 1.0f);

			//swap color
			gl.glDrawElements(GL10.GL_TRIANGLES, mNumOfIndices[i], 
					GL10.GL_UNSIGNED_SHORT, mPieIB.position(offset)); 
			offset += mNumOfIndices[i];

		}

		gl.glPopMatrix();
	}

	private void setAllBuffers(){
		if(data==null || data.size()==0)
			return;

		float labelCoords[] = {
				// X, Y, Z
				0.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
		}; 
		short labelIndexList[] = {
				0, 1, 2,
				0, 2, 3,
		};


		// initialize vertex Buffer for triangle 
		ByteBuffer vbb_label = ByteBuffer.allocateDirect(labelCoords.length * 4); // (# of coordinate values * 4 bytes per float)				

		vbb_label.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
		mLabelVB = vbb_label.asFloatBuffer(); // create a floating point buffer from the ByteBuffer
		mLabelVB.put(labelCoords); // add the coordinates to the FloatBuffer
		mLabelVB.position(0); // set the buffer to read the first coordinate

		ByteBuffer tbibb_label = ByteBuffer.allocateDirect(labelIndexList.length * 2); 
		tbibb_label.order(ByteOrder.nativeOrder());
		mLabelIB = tbibb_label.asShortBuffer();
		mLabelIB.put(labelIndexList); 
		mLabelIB.position(0); 



		float vertexlist[] = new float [(segments + 1)  * Dimensions];

		// Create the circle in the coordinates origin
		vertexlist[0] = 0.0f;
		vertexlist[1] = 0.0f;
		vertexlist[2] = 0.0f; 

		int vertexnumber = 3;
		for (int a = 0; a < 360; a += 360 / segments)
		{
			double angle= Math.toRadians(a);
			vertexlist[vertexnumber] = (float) (Math.cos(angle) * this.radius);
			vertexlist[vertexnumber + 1] = (float) (Math.sin(angle) * this.radius);
			vertexlist[vertexnumber + 2] = 0.0f;
			vertexnumber+=Dimensions;
		}

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertexlist.length * 4); 
		vbb.order(ByteOrder.nativeOrder());
		mPieVB = vbb.asFloatBuffer();
		mPieVB.put(vertexlist); 
		mPieVB.position(0); 
		// at this stage vertexlist has the coordinates of a circle, 1st vertex it's the center

		// Set buffer with vertex indices
		// this is made with triangles. always : center, v1, vnext. repeat ad infinitum
		short pieIndexList[] = new short[segments * Dimensions];
		short j = 0;
		double current_sum_perc = 0.0;
		int current_index = 0;
		int last_vertex_sum = 0;
		double current_perc = Double.parseDouble(data.get(0).getValue()) / sum_values;
		for (short i = 0 ; i < segments - 1; i++, j+=Dimensions)
		{
			//line not needed due to initialization to zero
			//pieIndexList[j] = 0;
			pieIndexList[j + 1] = (short) (i + 1);
			pieIndexList[j + 2] = (short) (i + 2);

			//last slice of pie, make circle complete
			if(current_index == data.size() - 1)
			{
				mNumOfIndices[current_index] = (segments * Dimensions) - last_vertex_sum;
				current_index++;
			}
			else if(current_index < data.size() - 1)
			{
				//checking percentages to see if we reach a new slice
				if((((i + 1.0) / segments) - current_sum_perc) > current_perc)
				{
					current_sum_perc += current_perc;
					mNumOfIndices[current_index] = j - last_vertex_sum;
					last_vertex_sum = j;
					current_index ++;
					current_perc = Double.parseDouble(data.get(current_index).getValue()) / sum_values;
				}
			}
		}

		//line not needed due to initialization to zero
		//pieIndexList[j] = 0;
		pieIndexList[j + 1] = (short) (segments);
		pieIndexList[j + 2] = 1;

		ByteBuffer tbibb = ByteBuffer.allocateDirect(pieIndexList.length * 2); 
		tbibb.order(ByteOrder.nativeOrder());
		mPieIB = tbibb.asShortBuffer();
		mPieIB.put(pieIndexList); 
		mPieIB.position(0); 
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// Save width and height
		this.width = width;                             // Save Current Width
		this.height = height;                           // Save Current Height

		gl.glViewport( 0, 0, width, height );

		// Setup orthographic projection
		gl.glMatrixMode( GL10.GL_PROJECTION );          // Activate Projection Matrix
		gl.glLoadIdentity();                            // Load Identity Matrix
		gl.glOrthof(                                    // Set Ortho Projection (Left,Right,Bottom,Top,Front,Back)
				0, width,
				0, height,
				1.0f, -1.0f
				);

		/*// Save width and height
		float aspect = (float)width / height; 

		// Take into account device orientation
		if (width > height)
			gl.glFrustumf(-aspect, aspect, -1.0f, 1.0f, 1.0f, 10.0f); 
		else
			gl.glFrustumf(-1, 1, -1/aspect, 1/aspect, 1, 10);*/

	} 

	public boolean onTouchEvent(MotionEvent e) { 
		/*float x = e.getX();
		float y = e.getY();
		switch (e.getAction()) { 
		case MotionEvent.ACTION_MOVE: 
			float dx = x - mPreviousX; 
			float dy = y - mPreviousY; 
			mAngleY = (mAngleY + (int)(dx * TOUCH_SCALE_FACTOR) + 360) % 360; 
			mAngleX = (mAngleX + (int)(dy * TOUCH_SCALE_FACTOR) + 360) % 360; 
			break; 
		} 
		mPreviousX = x; 
		mPreviousY = y; */
		return true;
	}
}
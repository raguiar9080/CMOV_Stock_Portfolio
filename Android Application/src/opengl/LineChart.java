package opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import opengl.text.GLText;
import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.view.MotionEvent;

import common.Series;

class LineChart implements Renderer {
	private final int Dimensions = 3;
	private final Double drawRange = 2.0;
	private float drawStart = -1;

	private final float[] Colors = {
			0.0f , 0.0f , 0.0f,	
			1.0f , 0.0f , 0.0f,
			0.0f , 1.0f , 0.0f,
			0.0f , 0.0f , 1.0f,
			0.0f , 1.0f , 1.0f,
	};
	
	private final float[] BackColor = {
			0.0f, 0.0f, 0.0f, 0.0f
	};
	
	private final float[] TextColor = {
			0.0f, 0.0f, 0.0f, 1.0f
	};


	private ArrayList<Series> data = null;
	Double max = Double.MIN_VALUE;
	Double min = Double.MAX_VALUE;

	private Context context= null;
	private FloatBuffer mLineVB = null; 
	private FloatBuffer mLabelVB = null;
	private FloatBuffer mBackVB = null;
	private ShortBuffer mLineIB = null; 
	private ShortBuffer mLabelIB = null;
	private ShortBuffer mBackIB = null;
	private GLText glText;

	private int width;
	private int height; 

	public float mAngleX = 0.0f; 
	public float mAngleY = 0.0f; 
	public float mAngleZ = 0.0f; 
	/*private float mPreviousX; 
	private float mPreviousY; 
	private final float TOUCH_SCALE_FACTOR = 0.6f;*/

	public LineChart(ArrayList<Series> elems, Context context) {
		this.context = context;
		this.data = elems;
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
		glText.load( "Roboto-Regular.ttf", 14, 2, 2 );  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)

		// Get all the buffers ready
		setAllBuffers();
	} 

	public void restart(ArrayList<Series> arrayList) {
		this.data = arrayList;
		max = Double.MIN_VALUE;
		min = Double.MAX_VALUE;
		
		setAllBuffers();
	}
	
	public void onDrawFrame(GL10 gl) { 
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); 
		gl.glMatrixMode(GL10.GL_MODELVIEW); 
		gl.glLoadIdentity();
		this.DrawLineLabels(gl);
		this.DrawBack(gl);
		this.DrawLineChart(gl);
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

	public void DrawLineLabels(GL10 gl) {
		if(data==null)
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
			if(glText.getLength(data.get(i).getFirst()) > maxLabelSize)
				maxLabelSize = glText.getLength(data.get(i).getFirst());

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

			DrawText(gl, data.get(i).getFirst(), label_size * 2.5f, 0.0f );

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

	public void DrawBack(GL10 gl)
	{
		if(data==null)
			return;

		gl.glPushMatrix();
		//landscape
		if(width > height)
		{
			gl.glTranslatef(height/2, height/2, -0.5f);
			gl.glScalef(height/2, height/2, 0.0f);
		}
		else
		{
			gl.glTranslatef(width/2, height - width/2, -0.5f);
			gl.glScalef(width/2, width/2, 0.0f);
		}

		//So it doesnt hit borders
		gl.glScalef(0.9f, 0.9f, 0.0f);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mBackVB);

		gl.glColor4f(BackColor[0], BackColor[1], BackColor[2], 1.0f);
		gl.glDrawElements(GL10.GL_LINE_STRIP, mBackIB.capacity(), 
				GL10.GL_UNSIGNED_SHORT, mBackIB);

		gl.glPopMatrix();
	}

	public void DrawLineChart(GL10 gl) {
		if(data==null)
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

		//So it doesnt hit borders
		gl.glScalef(0.9f, 0.9f, 0.0f);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mLineVB);

		// Draw all lines
		int offset = 0;
		for(int i = 0 ; i < data.size() ; i++)
		{
			gl.glColor4f(Colors[(i*3) % Colors.length], Colors[((i*3) + 1) % Colors.length], Colors[((i*3) + 2) % Colors.length], 1.0f);

			//swap color
			gl.glDrawElements(GL10.GL_LINE_STRIP, data.get(i).getSecond().size(), 
					GL10.GL_UNSIGNED_SHORT, mLineIB.position(offset)); 
			
			/*for(int index = offset ; index < offset + data.get(i).second.size()  ; index++)
			{
				Double value = data.get(i).second.get(index - offset);
				DrawText(gl, value.toString(), mLineVB.get(index), mLineVB.get(index + 1));
			}*/
			offset += data.get(i).getSecond().size();

		}

		gl.glPopMatrix();
	}

	private void setAllBuffers(){
		if(data == null)
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



		//INIT OF LINECHART BUFFERS
		int totalNumberPoints = 0;

		
		for (Series elem : data)
		{
			totalNumberPoints += elem.getSecond().size();
			for (Double value : elem.getSecond())
			{
				if(value > max)
					max = value;
				if(value < min)
					min = value;
			}
		}

		float vertexlist[] = new float [totalNumberPoints * Dimensions];

		//draw x = [-1,1]
		//draw y = [-1,1]
		int series = 0;
		int elem = 0;
		int elems_x = data.get(series).getSecond().size() - 1;
		for (int a = 0; a < totalNumberPoints; a++, elem++)
		{
			if(elem > elems_x)
			{
				elem=0;
				series++;
				elems_x = data.get(series).getSecond().size() - 1;
			}
			Double value = data.get(series).getSecond().get(elem);
			Double range = max - min;


			vertexlist[a * Dimensions] = (float) (drawStart + (elem / ((float)(elems_x))) * this.drawRange);
			vertexlist[(a * Dimensions) + 1] = (float) (drawStart + ((value - min) / range) * this.drawRange);
			vertexlist[(a * Dimensions) + 2] = 0.0f;
		}

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertexlist.length * 4); 
		vbb.order(ByteOrder.nativeOrder());
		mLineVB = vbb.asFloatBuffer();
		mLineVB.put(vertexlist); 
		mLineVB.position(0); 


		short lineIndexList[] = new short[totalNumberPoints];
		for (short i = 0 ; i < totalNumberPoints; i++)
			lineIndexList[i] = i;

		ByteBuffer tbibb = ByteBuffer.allocateDirect(lineIndexList.length * 2); 
		tbibb.order(ByteOrder.nativeOrder());
		mLineIB = tbibb.asShortBuffer();
		mLineIB.put(lineIndexList); 
		mLineIB.position(0);

		//BACK AREA
		float backvertexlist[] = {
				-1.0f, -1.0f, 0.0f,
				-1.0f, 1.0f, 0.0f,
				1.0f, -1.0f, 0.0f,
		};

		short backIndexList[] = {
				0, 1,
				0, 2,
		};

		ByteBuffer back_vbb = ByteBuffer.allocateDirect(backvertexlist.length * 4); 
		back_vbb.order(ByteOrder.nativeOrder());
		mBackVB = back_vbb.asFloatBuffer();
		mBackVB.put(backvertexlist); 
		mBackVB.position(0); 

		ByteBuffer back_tbibb = ByteBuffer.allocateDirect(backIndexList.length * 2); 
		back_tbibb.order(ByteOrder.nativeOrder());
		mBackIB = back_tbibb.asShortBuffer();
		mBackIB.put(backIndexList); 
		mBackIB.position(0);
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
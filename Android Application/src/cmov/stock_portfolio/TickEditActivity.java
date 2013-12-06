package cmov.stock_portfolio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import cmov.stock.stock_portfolio.R;

import common.Common;

public class TickEditActivity extends Activity {
	private String tick = "";
	private Integer owned = 0;
	private Integer type = Common.ADD;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tick_edit_activity);

		Bundle b = getIntent().getExtras();
		
		
		final Button end= (Button) findViewById(R.id.endTickEdit);
		
		if(b!= null && b.containsKey("tick") && b.containsKey("owned"))
		{
			type = Common.EDIT;
			end.setText("Update");
			
			((EditText) findViewById(R.id.tickNameEditText)).setText(b.getString("tick"));
			((EditText) findViewById(R.id.tickOwnedEditText)).setText(Integer.valueOf(b.getInt("owned")).toString());
		}
		
		
		end.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				tick = ((EditText) findViewById(R.id.tickNameEditText)).getText().toString();
				owned = Integer.parseInt(((EditText) findViewById(R.id.tickOwnedEditText)).getText().toString());
				Intent returnIntent = new Intent();
				returnIntent.putExtra("tick",tick);
				returnIntent.putExtra("owned",owned);
				returnIntent.putExtra("type",type);
				setResult(RESULT_OK,returnIntent);     
				finish();
			}	
		});
		
		
	}

}

package cmov.stock_portfolio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import cmov.stock.stock_portfolio.R;
import cmov.stock_portfolio.Portfolio.StockAdapter;

import common.Common;
import common.Stock;

public class TickEditActivity extends Activity {
	private String tick = "";
	private Integer owned = 0;
	private Integer type = Common.ADD;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tick_edit_activity);

		Bundle b = getIntent().getExtras();
		
		Spinner spn=(Spinner) findViewById(R.id.spinner1);
		spn.setVisibility(View.GONE);
		
		final Button end= (Button) findViewById(R.id.endTickEdit);
				
		if(b!= null && b.containsKey("tick") && b.containsKey("owned"))
		{
			type = Common.EDIT;
			Log.d("Test",b.getString("tick"));
			end.setText("Update");
			
			((EditText) findViewById(R.id.tickNameEditText)).setText(b.getString("tick"));
			((EditText) findViewById(R.id.tickOwnedEditText)).setText(Integer.valueOf(b.getInt("owned")).toString());
		}
		else
		{			
			spn.setVisibility(View.VISIBLE);
			StockAdapter adapter = new StockAdapter(this, R.layout.row_stock);
			spn.setAdapter(adapter);
			spn.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						EditText edit=(EditText) findViewById(R.id.tickNameEditText);
						Object o=parent.getItemAtPosition(position);
						edit.setText(o.toString());											
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
			});
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
	
	public class StockAdapter extends ArrayAdapter<Stock> {

		public StockAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId, Common.popular);
		}

		public int getCount(){
			return Common.popular.size();
		}

		public Stock getItem(int position){
			return Common.popular.get(position);
		}

		public long getItemId(int position){
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = getDropDownView(position, convertView, parent);
			v.setBackgroundColor(0x00000000);
			return v;
		}

		// And here is when the "chooser" is popped up
		// Normally is the same view, but you can customize it if you want
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			LayoutInflater vi = (LayoutInflater) TickEditActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_stock, null);
			Stock o = Common.popular.get(position);
			if (o != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					tt.setText(o.getTick());
				}
				if(bt != null){
					bt.setText(o.getFullName());
				}
			}
			//selected choice
			if (position == Common.selected)
				v.setBackgroundColor(0xFF2980b9);
			return v;
		}
	}

}

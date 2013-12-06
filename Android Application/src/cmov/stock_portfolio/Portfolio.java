package cmov.stock_portfolio;

import java.util.ArrayList;

import opengl.LineGraph;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import cmov.stock.stock_portfolio.R;

import common.Common;
import common.Network;
import common.Series;
import common.Stock;

public class Portfolio extends Fragment {
	StockAdapter adapter;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Common.REQ_CODE_TICK)
		{
			if(resultCode == Activity.RESULT_OK)
			{      
				if(data.getIntExtra("type", 0) == Common.ADD)
				{
					Stock tmp = new Stock(data.getStringExtra("tick"), data.getIntExtra("owned", 0));
					adapter.add(tmp);					
				}
				else
				{
					//update owned
					if(Common.stocks.get(Common.selected).getTick().equals(data.getStringExtra("tick")))
					{
						Common.stocks.get(Common.selected).setOwned(data.getIntExtra("owned", 0));
						adapter.notifyDataSetChanged();

					}
					else
					{
						Stock tmp = new Stock(data.getStringExtra("tick"), data.getIntExtra("owned", 0));
						adapter.remove(Common.stocks.get(Common.selected));
						adapter.add(tmp);
					}
					//new tick - just force new
					
					//not needed but forced. Already called because of EditActivity losing UI
					onResume();
				}
			}
		}
	}

	public void addStock()
	{
		Intent i = new Intent(getActivity(), TickEditActivity.class);
		startActivityForResult(i, Common.REQ_CODE_TICK);
	}

	public void editStock()
	{
		if(!(Common.selected> -1 && Common.selected < Common.stocks.size()))
			return;

		Intent i = new Intent(getActivity(), TickEditActivity.class);
		//Create a bundle object
		Bundle b = new Bundle();

		//Inserts a String value into the mapping of this Bundle
		b.putString("tick", Common.stocks.get(Common.selected).getTick());
		b.putInt("owned", Common.stocks.get(Common.selected).getOwned());

		//Add the bundle to the intent.
		i.putExtras(b);

		startActivityForResult(i, Common.REQ_CODE_TICK);
	}

	public void removeStock()
	{
		if(!(Common.selected> 0 && Common.selected < Common.stocks.size()))
			return;

		adapter.remove(Common.stocks.get(Common.selected));

		//this keeps Common.selected correct
		if(Common.selected >= Common.stocks.size())
			Common.selected--;

		//force refresh data
		onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.portfolio, container, false);

		adapter = new StockAdapter(getActivity(), R.layout.row_stock);
		final Spinner spinner = (Spinner) view.findViewById(R.id.TicksList);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Common.selected = position;

				Refresh();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				getView().findViewById(R.id.stockInformation).setVisibility(View.GONE);
			}
		});
		
		this.setHasOptionsMenu(true);
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.portfolio, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId())
		{
		case R.id.action_add:
			addStock();
			break;
		case R.id.action_sync_stock:
			new AsyncGetEvolution().execute();
			break;
		case R.id.action_edit:
			editStock();
			break;
		case R.id.action_remove:
			removeStock();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume()
	{
		if(Common.selected != - 1 && getView() != null)
		{
			Refresh();
			
			adapter.notifyDataSetChanged();
		}
		else if(Common.selected == - 1 && getView() != null)
		{
			getView().findViewById(R.id.stockInformation).setVisibility(View.GONE);
		}
		super.onResume();
	}
	
	public void Refresh()
	{
		getView().findViewById(R.id.stockInformation).setVisibility(View.VISIBLE);

		final TextView owned = (TextView) getView().findViewById(R.id.ownedShares);
		final TextView value = (TextView) getView().findViewById(R.id.shareValue);
		final TextView total = (TextView) getView().findViewById(R.id.totalValue);
		final TextView checked = (TextView) getView().findViewById(R.id.lastChecked);

		owned.setText(Common.stocks.get(Common.selected).getOwned().toString());
		value.setText(Common.stocks.get(Common.selected).getValue().toString() + "$");
		total.setText(Common.stocks.get(Common.selected).getTotalValue().toString() + "$");
		checked.setText(Common.stocks.get(Common.selected).getLastCheck());
		
		LineGraph lineFrag = (LineGraph) getActivity().getSupportFragmentManager().findFragmentById(R.id.stock_graph);
		lineFrag.drawLine(Common.stocks.get(Common.selected).getHistory());
	}

	public class StockAdapter extends ArrayAdapter<Stock> {

		public StockAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId, Common.stocks);
		}

		public int getCount(){
			return Common.stocks.size();
		}

		public Stock getItem(int position){
			return Common.stocks.get(position);
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
			LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_stock, null);
			Stock o = Common.stocks.get(position);
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

	public class AsyncGetEvolution extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			//TODO make params usefull
			elems.add(new BasicNameValuePair("a","9"));
			elems.add(new BasicNameValuePair("b","5"));
			elems.add(new BasicNameValuePair("c","2013"));
			elems.add(new BasicNameValuePair("d","9"));
			elems.add(new BasicNameValuePair("e","19"));
			elems.add(new BasicNameValuePair("f","2013"));
			elems.add(new BasicNameValuePair("g","d"));
			elems.add(new BasicNameValuePair("s","DELL"));

			Network connection = new Network(Common.SERVER_URL_CHARTS + "table.txt", "GET", elems, true);
			connection.run();
			return Common.convertJSON(connection.getResultObject(),true);
		}
		protected void onPostExecute(JSONObject result) {
			System.out.println(result.toString());
			
			


			
			//TODO PARSE
			ArrayList<Series> elems = new ArrayList<Series>();
			Series elem = new Series("Average");
			elem.add(1.0);
			elem.add(1.3);
			elem.add(3.5);
			elem.add(5.0);
			elems.add(elem);
			elem = new Series("Max");
			elem.add(4.0);
			elem.add(7.21);
			elem.add(13.5);
			elem.add(5.6);
			elem.add(9.1);
			elems.add(elem);
			
			Common.stocks.get(Common.selected).setHistory(elems);	
			
			
			Refresh();
		}
	}
}
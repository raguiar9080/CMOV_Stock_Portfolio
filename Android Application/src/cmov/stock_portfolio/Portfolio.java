package cmov.stock_portfolio;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import opengl.LineGraph;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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
import android.widget.Toast;
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

		LineGraph lineGraf = (LineGraph) getActivity().getSupportFragmentManager().findFragmentById(R.id.stock_graph);
		lineGraf.drawLine(Common.stocks.get(Common.selected).getHistory());
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
			Calendar ca1 = Calendar.getInstance();
			ca1.setTime(new Date());

			elems.add(new BasicNameValuePair("d",Integer.toString(ca1.get(Calendar.MONTH))));
			elems.add(new BasicNameValuePair("e",Integer.toString(ca1.get(Calendar.DAY_OF_MONTH))));
			elems.add(new BasicNameValuePair("f",Integer.toString(ca1.get(Calendar.YEAR))));

			// Remove 30 days
			ca1.add(Calendar.DATE, -30);

			elems.add(new BasicNameValuePair("a",Integer.toString(ca1.get(Calendar.MONTH))));
			elems.add(new BasicNameValuePair("b",Integer.toString(ca1.get(Calendar.DAY_OF_MONTH))));
			elems.add(new BasicNameValuePair("c",Integer.toString(ca1.get(Calendar.YEAR))));
			elems.add(new BasicNameValuePair("g","d"));
			elems.add(new BasicNameValuePair("s",Common.stocks.get(Common.selected).getTick()));

			Network connection = new Network(Common.SERVER_URL_CHARTS + "table.txt", "GET", elems, true);
			connection.run();
			return Common.convertJSON(connection.getResultObject(),true);
		}
		protected void onPostExecute(JSONObject result) {
			try {
				ArrayList<Series> elems = new ArrayList<Series>();
				Series closeSeries = new Series("Close");
				Series highSeries = new Series("High");
				Series lowSeries = new Series("Low");

				JSONArray all_ticks = (JSONArray) result.get("Values");
				for (int i = 0 ; i < all_ticks.length() ; i++)
				{
					closeSeries.add(all_ticks.getJSONObject(i).getDouble("Close"));
					highSeries.add(all_ticks.getJSONObject(i).getDouble("High"));
					lowSeries.add(all_ticks.getJSONObject(i).getDouble("Low"));
				}

				elems.add(closeSeries);
				elems.add(highSeries);
				elems.add(lowSeries);

				Common.stocks.get(Common.selected).setHistory(elems);

				Toast.makeText(getActivity(), "Data Fetched", Toast.LENGTH_SHORT).show();

				Refresh();				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
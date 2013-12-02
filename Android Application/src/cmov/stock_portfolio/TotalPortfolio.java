package cmov.stock_portfolio;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import cmov.stock.stock_portfolio.R;

import common.Common;
import common.Network;
import common.Stock;

public class TotalPortfolio extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.total_portfolio, container, false);

		this.setHasOptionsMenu(true);
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.total_portfolio, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId())
		{
		case R.id.action_sync:
			new AsyncGetStockInfo().execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		final TextView owned = (TextView) getView().findViewById(R.id.totalShares);
		final TextView value = (TextView) getView().findViewById(R.id.totalValue);

		owned.setText(Common.getSumShares().toString());
		value.setText(Common.getSumValue().toString() + "$");
		super.onResume();
	}

	public class AsyncGetStockInfo extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			elems.add(new BasicNameValuePair("f","snl1d1t1v"));
			elems.add(new BasicNameValuePair("s",Common.getAllOwnedTicks()));

			Network connection = new Network(Common.SERVER_URL_FINANCES + "d/quotes", "GET", elems, true);
			connection.run();
			return Common.convertJSON(connection.getResultObject(),false);
		}
		protected void onPostExecute(JSONObject result) {
			try {
				JSONArray all_ticks = (JSONArray) result.get("Values");
				for (int i = 0 ; i < all_ticks.length() ; i++)
				{
					JSONObject tick = all_ticks.getJSONObject(i);
					Stock tmp = Common.stocks.get(i);

					tmp.setExchanges(tick.getInt("Exchanges"));
					tmp.setLastCheck(tick.getString("Date") + " " + tick.getString("Time"));
					tmp.setValue(tick.getDouble("Value"));
					tmp.setFullName(tick.getString("Name"));

					Common.stocks.set(i, tmp);
				}

				onResume();
				//Redraw(getView());

				Toast.makeText(getActivity(), "Data Updated", Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(result.toString());
		}
	}
}
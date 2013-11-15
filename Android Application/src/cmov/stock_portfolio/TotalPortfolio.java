package cmov.stock_portfolio;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
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
		
		final TextView owned = (TextView) view.findViewById(R.id.totalShares);
		final TextView value = (TextView) view.findViewById(R.id.totalValue);

		owned.setText(Common.getSumShares().toString());
		value.setText(Common.getSumValue().toString());

		final Button getData = (Button) view.findViewById(R.id.getData);
		getData.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AsyncGetStockInfo().execute();
			}
		});
		return view;
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
					tmp.setValue(tick.getInt("Value"));
					tmp.setFullName(tick.getString("Name"));

					Common.stocks.set(i, tmp);
				}

				final TextView owned = (TextView) getView().findViewById(R.id.totalShares);
				final TextView value = (TextView) getView().findViewById(R.id.totalValue);

				owned.setText(Common.getSumShares().toString());
				value.setText(Common.getSumValue().toString());

				Toast.makeText(getActivity(), "Data Retrieved Sucessfully", Toast.LENGTH_SHORT).show();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			System.out.println(result.toString());
		}
	}
}
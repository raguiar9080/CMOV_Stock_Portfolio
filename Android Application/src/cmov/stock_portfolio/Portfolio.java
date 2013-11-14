package cmov.stock_portfolio;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import cmov.stock.stock_portfolio.R;

import common.Common;
import common.Network;
import common.Stock;

public class Portfolio extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.portfolio, container, false);

		Common.adapter = new ArrayAdapter<Stock>(getActivity(), android.R.layout.simple_list_item_1, Common.stocks); 
		final ListView list = (ListView) view.findViewById(R.id.listView1);
		list.setAdapter(Common.adapter); 

		new AsyncGetEvolution().execute();
		return view;
	}

	public class AsyncGetEvolution extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			elems.add(new BasicNameValuePair("f","sl1d1t1v"));
			elems.add(new BasicNameValuePair("s","DELL"));

			Network connection = new Network(Common.SERVER_URL_FINANCES + "d/quotes", "GET", elems, true);
			connection.run();
			return Common.convertJSON(connection.getResultObject(),false);
		}
		protected void onPostExecute(JSONObject result) {
			System.out.println(result.toString());
			Stock object = new Stock("MSFT","Microsoft Corporation",10);
			Common.adapter.add(object);
		}
	}
}


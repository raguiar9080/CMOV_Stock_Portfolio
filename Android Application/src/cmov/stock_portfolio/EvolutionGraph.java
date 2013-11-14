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
import cmov.stock.stock_portfolio.R;

import common.Common;
import common.Network;

public class EvolutionGraph extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.evolution_graph, container, false);

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
		}
	}
}


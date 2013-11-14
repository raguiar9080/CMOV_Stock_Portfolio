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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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
		final ListView list = (ListView) view.findViewById(R.id.TicksList);
		list.setAdapter(Common.adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			private View previous_view = null;
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {				
				//TODO visual change to selected
				if(previous_view!=null)
					previous_view.setBackgroundColor(0x00000000);
				view.setBackgroundColor(0xFF33b5e5);
				previous_view = view;
				
				final TextView owned = (TextView) getView().findViewById(R.id.ownedShares);
				final TextView value = (TextView) getView().findViewById(R.id.shareValue);
				final TextView total = (TextView) getView().findViewById(R.id.totalValue);
				final TextView checked = (TextView) getView().findViewById(R.id.lastChecked);
				
				owned.setText(Common.stocks.get(position).getOwned().toString());
				value.setText(Common.stocks.get(position).getValue().toString());
				total.setText(Common.stocks.get(position).getTotalValue().toString());
				checked.setText(Common.stocks.get(position).getLastCheck());

				Common.selected = position;
				
				//TODO refresh fragment
				//final EvolutionGraph graphEvo = (EvolutionGraph) getView().findViewById(R.id.GraphEvolution);

			}
		});

		final Button getData = (Button) view.findViewById(R.id.getData);
		getData.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AsyncGetStockInfo().execute();
			}
		});
		
		final Button refreshFrag = (Button) view.findViewById(R.id.refreshFragment);
		refreshFrag.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO refresh fragment
			}
		});
		
		
		
		//TODO
		Common.adapter.add(new Stock("MSFT","Microsoft Corporation",10));
		Common.adapter.add(new Stock("TWIT","Twiter",20));
		Common.adapter.add(new Stock("ASDD","Corporation",30));
		Common.adapter.add(new Stock("AWEQ","Bad Corporation",40));
		Common.adapter.add(new Stock("GSFT","Good Corporation",50));
		Common.adapter.add(new Stock("ESFT","Evil Corporation",60));
		Common.adapter.add(new Stock("VALV","Valve Corporation",70));
		Common.adapter.add(new Stock("QSFT","Healthy Corporation",80));
		Common.adapter.add(new Stock("SSFT","Sick Corporation",90));
		
		//TODO
		//list.performItemClick(view, 0, 0);
		
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
			elems.add(new BasicNameValuePair("f","sl1d1t1v"));
			elems.add(new BasicNameValuePair("s","DELL"));

			Network connection = new Network(Common.SERVER_URL_FINANCES + "d/quotes", "GET", elems, true);
			connection.run();
			return Common.convertJSON(connection.getResultObject(),false);
		}
		protected void onPostExecute(JSONObject result) {
			System.out.println(result.toString());
		}
	}
}


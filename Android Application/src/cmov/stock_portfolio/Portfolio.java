package cmov.stock_portfolio;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
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
import android.widget.Toast;
import cmov.stock.stock_portfolio.R;

import common.Common;
import common.Network;
import common.Stock;

public class Portfolio extends Fragment {
	private StockAdapter adapter = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.portfolio, container, false);

		this.adapter = new StockAdapter(getActivity(), R.layout.row_stock);
		final ListView list = (ListView) view.findViewById(R.id.TicksList);
		//setListAdapter(this.adapter);
        
		//adapter = new ArrayAdapter<Stock>(getActivity(), android.R.layout.simple_list_item_1); 
		list.setAdapter(adapter);
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
		adapter.add(new Stock("MSFT","Microsoft Corporation",10));
		adapter.add(new Stock("TWIT","Twiter",20));
		adapter.add(new Stock("ASDD","Corporation",30));
		adapter.add(new Stock("AWEQ","Bad Corporation",40));
		adapter.add(new Stock("GSFT","Good Corporation",50));
		adapter.add(new Stock("ESFT","Evil Corporation",60));
		adapter.add(new Stock("VALV","Valve Corporation",70));
		adapter.add(new Stock("QSFT","Healthy Corporation",80));
		adapter.add(new Stock("SSFT","Sick Corporation",90));

		//TODO
		//list.performItemClick(view, 0, 0);

		return view;
	}

	public class AsyncGetStockInfo extends AsyncTask<Void, Void,  JSONObject> {
		private ArrayList<NameValuePair> elems = new ArrayList<NameValuePair>();
		private Integer index;
		private Stock selected;
		
		@Override
		protected void onPreExecute() {
			index = Common.selected;
			selected = Common.stocks.get(Common.selected);
			super.onPreExecute();
		}
		@Override
		protected JSONObject doInBackground(Void... params) {
			elems.add(new BasicNameValuePair("f","sl1d1t1v"));
			elems.add(new BasicNameValuePair("s",selected.getTick()));

			Network connection = new Network(Common.SERVER_URL_FINANCES + "d/quotes", "GET", elems, true);
			connection.run();
			return Common.convertJSON(connection.getResultObject(),false);
		}
		protected void onPostExecute(JSONObject result) {
			try {
				selected.setLastCheck(result.get("Date") + " " + result.get("Time"));
				selected.setValue(result.getInt("Value"));
				selected.setExchanges(result.getInt("Exchanges"));
				Common.stocks.set(index, selected);
				
				final TextView value = (TextView) getView().findViewById(R.id.shareValue);
				final TextView total = (TextView) getView().findViewById(R.id.totalValue);
				final TextView checked = (TextView) getView().findViewById(R.id.lastChecked);

				value.setText(selected.getValue().toString());
				total.setText(selected.getTotalValue().toString());
				checked.setText(selected.getLastCheck());
				
				Toast.makeText(getActivity(), "Data Retrieved Sucessfully", Toast.LENGTH_SHORT).show();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private class StockAdapter extends ArrayAdapter<Stock> {

        public StockAdapter(Context context, int textViewResourceId) {
                super(context, textViewResourceId, Common.stocks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row_stock, null);
                }
                Stock o = Common.stocks.get(position);
                if (o != null) {
                        TextView tt = (TextView) v.findViewById(R.id.toptext);
                        TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                        if (tt != null) {
                              tt.setText(o.getTick());                            }
                        if(bt != null){
                              bt.setText(o.getFullName());
                        }
                }
                return v;
        }
}
}
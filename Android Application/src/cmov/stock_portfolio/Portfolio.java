package cmov.stock_portfolio;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cmov.stock.stock_portfolio.R;

import common.Common;
import common.Network;
import common.Stock;

public class Portfolio extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.portfolio, container, false);

		StockAdapter adapter = new StockAdapter(getActivity(), R.layout.row_stock);
		final Spinner spinner = (Spinner) view.findViewById(R.id.TicksList);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				getView().findViewById(R.id.stockInformation).setVisibility(View.VISIBLE);

				final TextView owned = (TextView) getView().findViewById(R.id.ownedShares);
				final TextView value = (TextView) getView().findViewById(R.id.shareValue);
				final TextView total = (TextView) getView().findViewById(R.id.totalValue);
				final TextView checked = (TextView) getView().findViewById(R.id.lastChecked);

				owned.setText(Common.stocks.get(position).getOwned().toString());
				value.setText(Common.stocks.get(position).getValue().toString());
				total.setText(Common.stocks.get(position).getTotalValue().toString());
				checked.setText(Common.stocks.get(position).getLastCheck());

				Common.selected = position;

				final WebView graphEvo = (WebView) getView().findViewById(R.id.GraphEvolution);
				final ProgressBar webViewProgress = (ProgressBar) getView().findViewById(R.id.webViewProgress);
				graphEvo.setVisibility(View.GONE);
				webViewProgress.setVisibility(View.GONE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				getView().findViewById(R.id.stockInformation).setVisibility(View.GONE);
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
				String url =  "http://chart.apis.google.com/chart?cht=p3&chs=500x200&chd=e:TNTNTNGa&chts=000000,16&chtt=A+Better+Web&chl=Hello|Hi|anas|Explorer&chco=FF5533,237745,9011D3,335423&chdl=Apple|Mozilla|Google|Microsoft";

				final WebView graphEvo = (WebView) getView().findViewById(R.id.GraphEvolution);
				graphEvo.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageStarted(WebView view, String url, Bitmap favicon) {
						super.onPageStarted(view, url, favicon);
						final ProgressBar webViewProgress = (ProgressBar) getView().findViewById(R.id.webViewProgress);
						webViewProgress.setVisibility(View.VISIBLE);
					}

					@Override
					public void onPageFinished(WebView view, String url) {
						super.onPageFinished(view, url);
						view.setVisibility(View.VISIBLE);
						final ProgressBar webViewProgress = (ProgressBar) getView().findViewById(R.id.webViewProgress);
						webViewProgress.setVisibility(View.GONE);
					}
				});
				graphEvo.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

				graphEvo.loadUrl(url);				
			}
		});

		return view;
	}

	private class StockAdapter extends ArrayAdapter<Stock> {

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
					tt.setText(o.getTick());                            }
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
		}
	}
}
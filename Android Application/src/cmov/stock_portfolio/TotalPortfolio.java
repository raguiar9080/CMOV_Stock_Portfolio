package cmov.stock_portfolio;

import common.Common;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cmov.stock.stock_portfolio.R;

public class TotalPortfolio extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.total_portfolio, container, false);
		
		
		final TextView owned = (TextView) view.findViewById(R.id.totalShares);
		final TextView value = (TextView) view.findViewById(R.id.totalValue);

		owned.setText(Common.getSumShares().toString());
		value.setText(Common.getSumValue().toString());
		
		
		return view;
	}
}
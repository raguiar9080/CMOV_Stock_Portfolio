package common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Environment;

@SuppressLint("SimpleDateFormat")
public class Common {
	public static Integer REQ_CODE_TICK = 1;
	public static Integer ADD = 0;
	public static Integer EDIT = 1;
	public static List<Stock> stocks = new ArrayList<Stock>();
	public static Integer selected = -1;

	public static Boolean mExternalStorageAvailable = false;
	public static Boolean mExternalStorageWriteable = false;
	static String state = Environment.getExternalStorageState();


	public static final String FILE_NAME = "Stock_Portfolio";
	public static final String SERVER_URL_CHARTS = "http://ichart.finance.yahoo.com/";
	public static final String SERVER_URL_FINANCES = "http://finances.yahoo.com/";

	public static class DateUtils {
		public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

		public static String now() {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
			return sdf.format(cal.getTime());
		}
	}

	public static JSONObject convertJSON(String input, Boolean paramsInResponse)
	{
		try
		{
			input = input.replaceAll("\"N/A\"", "-");
			input = input.replaceAll("N/A", "0");
			input = input.replaceAll("\"", "");
			if (paramsInResponse)
			{
				JSONObject result = new JSONObject();

				String[] lines = input.split("\n");
				String[] params = lines[0].split(",");
				JSONArray jsonarray = new JSONArray();
				for (int i = 1 ; i < lines.length ; i++)
				{
					String[] values = lines[i].split(",");
					JSONObject jsonvalue = new JSONObject();
					for (int x = 0 ; x < values.length ; x++)
						jsonvalue.accumulate(params[x], values[x]);

					jsonarray.put(jsonvalue);
				}
				result.accumulate("Values",jsonarray);
				return result;
			}
			else
			{
				JSONObject result = new JSONObject();
				String[] params = {"Tick","Name", "Value","Date","Time","Exchanges"};
				String[] lines = input.split("\n");
				JSONArray jsonarray = new JSONArray();
				for (int i = 0 ; i < lines.length ; i++)
				{
					String[] values = lines[i].split(",");
					JSONObject jsonvalue = new JSONObject();
					for (int x = 0 ; x < values.length ; x++)
						jsonvalue.accumulate(params[x], values[x]);

					jsonarray.put(jsonvalue);
				}
				result.accumulate("Values",jsonarray);
				return result;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
		return null;
	}

	public static Double getSumValue()
	{
		Double count = 0.0;
		for (Stock stock : stocks)
		{
			count += stock.getTotalValue();
		}
		return count;
	}
	
	public static Integer getSumShares()
	{
		Integer count = 0;
		for (Stock stock : stocks)
		{
			count += stock.getOwned();
		}
		return count;
	}
	
	public static String getAllOwnedTicks()
	{
		String count = "";
		for (Stock stock : stocks)
		{
			count += stock.getTick() + ",";
		}
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public static void loadStocks(Application application) {
		try {
			FileInputStream fstream = application.openFileInput(FILE_NAME);
			ObjectInputStream in = new ObjectInputStream(fstream);
			stocks = (ArrayList<Stock>) in.readObject();
			fstream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();			
		}
	}
	
	public static void saveStocks(Application application) {
		// Create file 
		try {
			FileOutputStream fstream = application.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
			ObjectOutputStream out = new ObjectOutputStream(fstream);
			out.writeObject(stocks);
			fstream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();			
		}
	}
}
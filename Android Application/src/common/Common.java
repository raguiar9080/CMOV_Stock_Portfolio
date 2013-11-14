package common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.widget.ArrayAdapter;

@SuppressLint("SimpleDateFormat")
public class Common {
	public static List<Stock> stocks = new ArrayList<Stock>();
	public static ArrayAdapter<Stock> adapter = null;
	public static Integer selected = -1;
	
	
	public static final String PREFS_NAME = "Stock_Portfolio";
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

				String[] lines = input.split("\n");
				JSONArray jsonarray = new JSONArray();
				for (int i = 0 ; i < lines.length ; i++)
				{
					String[] values = lines[i].split(",");
					JSONObject jsonvalue = new JSONObject();
					for (int x = 0 ; x < values.length ; x++)
						jsonvalue.accumulate(Integer.valueOf(x).toString(), values[x]);

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
}
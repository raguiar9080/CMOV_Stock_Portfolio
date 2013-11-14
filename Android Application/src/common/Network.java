package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class Network implements Runnable {

	private String link, resultString, method;
	private ArrayList<NameValuePair> payload;
	private int readTimeout = 10000, connectionTimeout = 15000;
	private String resultObject;
	private Boolean paramsInURL;

	public Network(String link, String method, ArrayList<NameValuePair> payload, Boolean paramsInURL) {
		this.link = link;
		this.method = method;
		this.payload = payload;
		this.paramsInURL = paramsInURL;
	}

	@Override
	public void run() {
		connect();
	}

	private void connect() {
		HttpURLConnection con = null;
		String line = "";
		StringBuffer sb = new StringBuffer();
		try {
			if(payload != null && paramsInURL)
			{
				link += "?";
				for(NameValuePair pair : payload)
					link += pair.getName() + "=" + pair.getValue() + "&";
			}
			
			URL url = new URL(link);
			con = (HttpURLConnection) url.openConnection();
			con.setReadTimeout(readTimeout);
			con.setConnectTimeout(connectionTimeout);
			con.setRequestMethod(method);
			con.setDoInput(true);

			if(payload != null && !paramsInURL) {
				con.setDoOutput(true);

				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

				for(NameValuePair pair : payload)
					params.add(new BasicNameValuePair(pair.getName(), pair.getValue()));

				OutputStream os = con.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(os, "UTF-8"));
				writer.write(getQuery(params));
				writer.flush();
				writer.close();
				os.close();
			}

			// Start the connection
			con.connect();

			// Read results from the query
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8" ));

			while ((line = reader.readLine()) != null)
				sb.append(line + "\n");

			resultString = sb.toString();

			setResultObject(resultString);
			reader.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			if (con != null)
				con.disconnect();
		}
	}

	private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	public String getResultObject() {
		return resultObject;
	}

	public void setResultObject(String resultObject) {
		this.resultObject = resultObject;
	}

}
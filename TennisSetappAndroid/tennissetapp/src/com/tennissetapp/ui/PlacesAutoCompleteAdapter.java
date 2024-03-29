package com.tennissetapp.ui;

import com.tennissetapp.json.GeoCodeResult;
import com.tennissetapp.rest.Client;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<GeoCodeResult.Result> implements Filterable {
    private GeoCodeResult geoCode;

    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }
    
    @Override
    public int getCount() {
    	return geoCode.results.size();
    }

    @Override
    public GeoCodeResult.Result getItem(int index) {
    	return geoCode.results.get(index);
    }
    
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    geoCode = Client.getInstance().googleGeocode(constraint.toString());
                    
                    // Assign the data to the FilterResults
                    filterResults.values = geoCode.results;
                    filterResults.count = geoCode.results.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }
}



//private ArrayList<String> autocomplete(String input) {
//ArrayList<String> resultList = null;
//C.logger.info("autocomplete HERE " + input);
//HttpURLConnection conn = null;
//StringBuilder jsonResults = new StringBuilder();
//try {
//	
//	//--env
////	  StringBuilder sb = new StringBuilder(PLACES_API_BASE);
////    sb.append("?sensor=false&key=" + API_KEY);
////    sb.append("&components=country:uk");
////    sb.append("&input=" + URLEncoder.encode(input, "utf8"));
//	
////	AIzaSyC3yVrvJmFhkZKaobus2FQQExmR03fkm84
//	//--
//  StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
//  sb.append("?sensor=false&key=" + API_KEY);
//  sb.append("&components=country:uk");
//  sb.append("&input=" + URLEncoder.encode(input, "utf8"));
//  
//  URL url = new URL(sb.toString());
//  conn = (HttpURLConnection) url.openConnection();
//  InputStreamReader in = new InputStreamReader(conn.getInputStream());
//  
//  // Load the results into a StringBuilder
//  int read;
//  char[] buff = new char[1024];
//  while ((read = in.read(buff)) != -1) {
//      jsonResults.append(buff, 0, read);
//  }
//  C.logger.info("autocomplete RESPONSE HERE " + jsonResults);
//} catch (MalformedURLException e) {
//  Log.e(LOG_TAG, "Error processing Places API URL", e);
//  return resultList;
//} catch (IOException e) {
//  Log.e(LOG_TAG, "Error connecting to Places API", e);
//  return resultList;
//} finally {
//  if (conn != null) {
//      conn.disconnect();
//  }
//}
//
//try {
//  // Create a JSON object hierarchy from the results
//  JSONObject jsonObj = new JSONObject(jsonResults.toString());
//  JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
//  
//  // Extract the Place descriptions from the results
//  resultList = new ArrayList<String>(predsJsonArray.length());
//  for (int i = 0; i < predsJsonArray.length(); i++) {
//      resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
//  }
//} catch (JSONException e) {
//  Log.e(LOG_TAG, "Cannot process JSON results", e);
//}
//
//return resultList;
//}
package com.mysampleapp;

import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.api.CloudLogicAPI;
import com.amazonaws.mobile.api.CloudLogicAPIConfiguration;
import com.amazonaws.mobile.api.CloudLogicAPIFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.mobilehelper.util.ThreadUtils;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chris on 5/15/2017.
 */

public class Route extends Fragment{

    private static String R_TAG = "RouteTAG";

    private TextView routeName;
    private TextView startLong;
    private TextView startLat;
    private TextView endLong;
    private TextView endLat;
    private TextView Rused;
    private String uIden;

    //Data Fetchers
    private String getUserID = "kps";
    private String[] rd;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.route, container, false);

        routeName = (TextView) v.findViewById(R.id.Rname);
        startLong = (TextView) v.findViewById(R.id.RstartLongitude);
        endLong = (TextView) v.findViewById(R.id.RendLatitude);
        startLat = (TextView) v.findViewById(R.id.RstartLatitude);
        endLat = (TextView) v.findViewById(R.id.RendLatitude);
        //Returning the layout file after inflating

        /*if (!isGettingViewed) {
            (new getDynamoDBData()).execute(getUserID);
        }*/

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisible)
    {
        super.setUserVisibleHint(isVisible);

        if (isVisible) {
            // fetchdata() contains logic to show data when page is selected mostly asynctask to fill the data
            (new getDynamoDBData()).execute(getUserID);
        }
    }

    private class getDynamoDBData extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... params) {
            try {
                Log.d(R_TAG, "Get Route Began");
                CloudLogicAPIConfiguration apiConfiguration;
                apiConfiguration = CloudLogicAPIFactory.getAPIs()[0]; //for Route API
                final String endpoint = apiConfiguration.getEndpoint();
                Log.d(R_TAG, "Endpoint : " + endpoint);

                final String method ="POST";
                final String path = "/getRoute";
                //final String body = "{\"email\" : \""+params[0]+"\", \"password\" : \"" + params[1] +"\"}";
                final String body = "{\"userId\" :\"" + params[0] + "\"}";

                Log.d(R_TAG, "Body : " + body);
                Log.d(R_TAG, "Path : " + path);
                Log.d(R_TAG, "Method : " + method);

                String queryStringText = "?lang=en_US";
                final Map<String, String> parameters = convertQueryStringToParameters(queryStringText);
                final CloudLogicAPI client = AWSMobileClient.defaultMobileClient().createAPIClient(apiConfiguration.getClientClass());
                Log.d(R_TAG, "CAME BACK 2!!!!");
                final Map<String, String> headers = new HashMap<String, String>();
                Log.d(R_TAG, "CAME BACK 3!!!!");
                final byte[] content = body.getBytes(StringUtils.UTF8);
                Log.d(R_TAG, "CAME BACK 4!!!!");

                ApiRequest tmpRequest = new ApiRequest(client.getClass().getSimpleName())
                        .withPath(path)
                        .withHttpMethod(HttpMethodName.valueOf(method))
                        .withHeaders(headers)
                        .addHeader("Content-Type", "application/json")
                        .withParameters(parameters);
                Log.d(R_TAG, "CAME BACK 5!!!!");

                final ApiRequest request;

                // Only set body if it has content.
                if (body.length() > 0) {
                    request = tmpRequest
                            .addHeader("Content-Length", String.valueOf(content.length))
                            .withBody(content);
                } else {
                    request = tmpRequest;
                }

                //final Fragment fragment = this;

                Log.d(R_TAG, "Invoking ROUTES API Request : " + request.getHttpMethod() + ":" + request.getPath());
                final ApiResponse response = client.execute(request);
                final InputStream responseContentStream = response.getContent();

                if (responseContentStream != null) {
                    final String responseData = IOUtils.toString(responseContentStream);
                    Log.d(R_TAG, "Response : " + responseData);

                    JSONObject obj = new JSONObject(responseData);
                    //rd = (obj.getString("routes").toString()).split(":");
                    Log.d(R_TAG, obj.getString("routes").toString());


                    setResponseBodyText(obj.getString("routes").toString());

                }else{
                    Log.d(R_TAG, "Response is null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * receives the JSON string and dumps it into an EditText widget associated with
     *   class variable mResultField
     * @param text */
    private void setResponseBodyText(final String text) {

        ThreadUtils.runOnUiThread(new Runnable() {

            @Override
            public void run() { routeName.setText(text);  }

        });

    }

    /** * setups parameters of query string in Map format to pass to AWS request
     * @param queryStringText
     * @return */
    private Map<String,String> convertQueryStringToParameters(String queryStringText) {
        Log.d(R_TAG, "convertQueryStringToParameters CALLED!");
        while (queryStringText.startsWith("?") && queryStringText.length() > 1) {
            queryStringText = queryStringText.substring(1);
        }

        final UrlQuerySanitizer sanitizer = new UrlQuerySanitizer();
        sanitizer.setAllowUnregisteredParamaters(true);
        sanitizer.parseQuery(queryStringText);

        final List<UrlQuerySanitizer.ParameterValuePair> pairList = sanitizer.getParameterList();
        final Map<String, String> parameters = new HashMap<>();

        for (final UrlQuerySanitizer.ParameterValuePair pair : pairList) {
            Log.d(R_TAG, pair.mParameter + " = " + pair.mValue);
            parameters.put(pair.mParameter, pair.mValue);
        }

        return parameters;
    }

}

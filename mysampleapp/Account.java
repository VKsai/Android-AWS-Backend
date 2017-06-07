package com.mysampleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//AWS Imports
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.api.CloudLogicAPI;
import com.amazonaws.mobile.api.CloudLogicAPIConfiguration;
import com.amazonaws.mobile.api.CloudLogicAPIFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.amazonaws.regions.ServiceAbbreviations.Email;


/**
 * Created by Chris on 5/15/2017.
 */

public class Account extends Fragment{

    private static String LOG_TAG = "AccTest";

    //Class variables representing the request, the CloudLopciAPI to communicate from
    // client to send the request.
    private Button enterButton;
    private EditText login;
    private EditText password;
    private EditText firstName;
    private EditText lastName;

    //Data
    private String email;
    private String passData;
    private String fnData;
    private String lnData;
    private String[] emailParts;
    private String userIden, userIdCheck;
    private String UID, PASS;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        View v = inflater.inflate(R.layout.account, container, false);

        final SharedPreferences pref = getContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        login = (EditText) v.findViewById(R.id.edit_login);
        password = (EditText) v.findViewById(R.id.edit_pass);
        firstName = (EditText) v.findViewById(R.id.edit_first);
        lastName = (EditText) v.findViewById(R.id.edit_last);



        enterButton = (Button) v.findViewById(R.id.enterButton);
        enterButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                email = login.getText().toString();
                //Breaking string
                emailParts = email.split("@");
                userIden = emailParts[0];

                passData = password.getText().toString();
                fnData = firstName.getText().toString();
                lnData = lastName.getText().toString();

                if(pref.getString("UID", null) != null)
                {

                    (new checkLogin()).execute(userIden,email,passData,fnData,lnData);
                }
                else
                {
                    editor.putString("UID", userIden);
                    (new createNewUser()).execute(userIden,fnData,lnData,passData,email);
                }
                //insertData(loginData, passData, fnData, lnData);
                //(new insertClass()).execute(loginD,pass,fn,ln);
                Log.d(LOG_TAG, "Password : " + passData);
                Log.d(LOG_TAG, "Email : " + email);

                //(new checkLogin()).execute(email,passData,fnData,lnData);
                Log.d(LOG_TAG, "Button Clicked!");
            }
            });

        return  v;
    }


    //Create User
    private class createNewUser extends AsyncTask<String, Void, Void>
    {
        @Override
    protected Void doInBackground(String... params) {
        try {
            Log.d(LOG_TAG, "Create User Began");
            CloudLogicAPIConfiguration apiConfiguration;
            apiConfiguration = CloudLogicAPIFactory.getAPIs()[1]; //for Biker API
            final String endpoint = apiConfiguration.getEndpoint();
            Log.d(LOG_TAG, "Endpoint : " + endpoint);

            final String method ="POST";
            final String path = "/createUser";
            //final String body = "{\"email\" : \""+params[0]+"\", \"password\" : \"" + params[1] +"\"}";
            final String body = "{\"userID\" :\"" + params[0] + "\", \"firstName\" : \"" + params[1] + "\", \"lastName\" : \"" + params[2] + "\", \"password\" : \"" + params[3] + "\", \"email\" : \"" + params[4] + "\"}";

            Log.d(LOG_TAG, "Body : " + body);
            Log.d(LOG_TAG, "Path : " + path);
            Log.d(LOG_TAG, "Method : " + method);

            String queryStringText = "?lang=en_US";
            final Map<String, String> parameters = convertQueryStringToParameters(queryStringText);
            final CloudLogicAPI client = AWSMobileClient.defaultMobileClient().createAPIClient(apiConfiguration.getClientClass());
            Log.d(LOG_TAG, "CAME BACK 2!!!!");
            final Map<String, String> headers = new HashMap<String, String>();
            Log.d(LOG_TAG, "CAME BACK 3!!!!");
            final byte[] content = body.getBytes(StringUtils.UTF8);
            Log.d(LOG_TAG, "CAME BACK 4!!!!");

            ApiRequest tmpRequest = new ApiRequest(client.getClass().getSimpleName())
                    .withPath(path)
                    .withHttpMethod(HttpMethodName.valueOf(method))
                    .withHeaders(headers)
                    .addHeader("Content-Type", "application/json")
                    .withParameters(parameters);
            Log.d(LOG_TAG, "CAME BACK 5!!!!");

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

            Log.d(LOG_TAG, "Invoking API w/ Request : " + request.getHttpMethod() + ":" + request.getPath());
            final ApiResponse response = client.execute(request);
            final InputStream responseContentStream = response.getContent();

            if (responseContentStream != null) {
                final String responseData = IOUtils.toString(responseContentStream);
                Log.d(LOG_TAG, "Response : " + responseData);

            }else{
                Log.d(LOG_TAG, "Response is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

        @Override
        protected void onPostExecute(Void result) {

            try {

                Log.d(LOG_TAG, "Finished Insertion");


            } catch (Exception e) {
                e.printStackTrace();
                //(new ErrorMessage(getActivity().getBaseContext())).ErrorMessageToast(e.getMessage());
            }
        }

    }

    //Login Operation
    private class checkLogin extends AsyncTask<String, Void, Void> {
        String result="";
        //String userLogin="";

        @Override
        protected Void doInBackground(String... params) {
            try {

                Log.d(LOG_TAG, "Verify User Began;");
                //userLogin = params[0];

                CloudLogicAPIConfiguration apiConfiguration;
                apiConfiguration = CloudLogicAPIFactory.getAPIs()[1]; //for Biker API
                final String endpoint = apiConfiguration.getEndpoint();
                Log.d(LOG_TAG, "Endpoint : " + endpoint);

                final String method ="POST";
                final String path = "/verifyUser";
                final String body = "{\"email\" : \""+params[0]+"\", \"password\" : \"" + params[1] +"\"}";

                Log.d(LOG_TAG, "Body : " + body);
                Log.d(LOG_TAG, "Path : " + path);
                Log.d(LOG_TAG, "Method : " + method);

                String queryStringText = "?lang=en_US";
                final Map<String, String> parameters = convertQueryStringToParameters(queryStringText);
                final CloudLogicAPI client = AWSMobileClient.defaultMobileClient().createAPIClient(apiConfiguration.getClientClass());
                Log.d(LOG_TAG, "CAME BACK 2!!!!");
                final Map<String, String> headers = new HashMap<String, String>();
                Log.d(LOG_TAG, "CAME BACK 3!!!!");
                final byte[] content = body.getBytes(StringUtils.UTF8);
                Log.d(LOG_TAG, "CAME BACK 4!!!!");

                ApiRequest tmpRequest = new ApiRequest(client.getClass().getSimpleName())
                        .withPath(path)
                        .withHttpMethod(HttpMethodName.valueOf(method))
                        .withHeaders(headers)
                        .addHeader("Content-Type", "application/json")
                        .withParameters(parameters);
                Log.d(LOG_TAG, "CAME BACK 5!!!!");

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

                Log.d(LOG_TAG, "Invoking API w/ Request : " + request.getHttpMethod() + ":" + request.getPath());
                final ApiResponse response = client.execute(request);
                final InputStream responseContentStream = response.getContent();

                if (responseContentStream != null) {
                    final String responseData = IOUtils.toString(responseContentStream);
                    result = responseData;
                    Log.d(LOG_TAG, "Response : " + responseData);
                }else{
                    Log.d(LOG_TAG, "Response is null");
                    Toast.makeText(getContext(), "Login Error!", Toast.LENGTH_SHORT).show();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            try {

                Log.d(LOG_TAG, "Finished the operation");


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

        /** * setups parameters of query string in Map format to pass to AWS request
         * @param queryStringText
         * @return */
        private Map<String,String> convertQueryStringToParameters(String queryStringText) {
            Log.d(LOG_TAG, "convertQueryStringToParameters CALLED!");
            while (queryStringText.startsWith("?") && queryStringText.length() > 1) {
                queryStringText = queryStringText.substring(1);
            }

            final UrlQuerySanitizer sanitizer = new UrlQuerySanitizer();
            sanitizer.setAllowUnregisteredParamaters(true);
            sanitizer.parseQuery(queryStringText);

            final List<UrlQuerySanitizer.ParameterValuePair> pairList = sanitizer.getParameterList();
            final Map<String, String> parameters = new HashMap<>();

            for (final UrlQuerySanitizer.ParameterValuePair pair : pairList) {
                Log.d(LOG_TAG, pair.mParameter + " = " + pair.mValue);
                parameters.put(pair.mParameter, pair.mValue);
            }

            return parameters;
        }
}

package ae.shjcoop.digitalsignage;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faisalkhalid on 8/6/18.
 */

public class Client {


        public ClientDelegate mCallback;
    public AppUpdateDelegate mAppUpdateCallback;
    RequestQueue queue;
    Context mContext;


    Client(Context context){
        mContext = context;
       queue  = Volley.newRequestQueue(mContext);
    }

    String baseURL = "https://mobileapi.shjcoop.ae/MobileAppServices/DigitalSignage/api/";

    public void ping(Context context, String name,String uuid,  String macAddress) throws Exception {

        String url = baseURL+"register/"+macAddress;

        Log.d("register",url);

// Instantiate the RequestQueue.
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("register", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("register", error.getLocalizedMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("uniqueID", uuid);
                params.put("name", name);
                return params;
            }
        };

        postRequest.setShouldCache(false);
        queue.add(postRequest);

    }



    public void getDevice(Context context,String macAddress) throws Exception {

        String url = baseURL+"device/"+macAddress;

// Instantiate the RequestQueue.
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Boolean results = jsonObject.getBoolean("Result");
                            if (results) {

                                Device device = new Device();
                                JSONObject deviceObject = jsonObject.getJSONObject("Device");

                                if(deviceObject != null) {
                                    Log.d("getDevice123",response);

                                device.UUID = deviceObject.getString("uuid");
                                device.name = deviceObject.getString("name");
                                device.contentModifiedOn = deviceObject.getString("contentmodifiedon");
                                device.contentURL = deviceObject.getString("contenturl");
                                device.lastPingOn = deviceObject.getString("lastpingon");
                                device.macAddress = deviceObject.getString("macaddress");
                                device.isRepeatable = deviceObject.getString("repeatable") == "1";
                                    device.isSound = deviceObject.getInt("sound") == 1;

                                    Log.d("device3232",device.name);


                                mCallback.onClientDeviceResponse(device);

                            }
                             else {
                                    Log.d("devicenull","device is null");
                                }
                            }


                        }
                        catch (JSONException e) {
                            Log.d("fksiddiqui", "error hai");

                        }


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
Log.d("fatal","error hai bhai is me");
                    }
                });

        postRequest.setShouldCache(false);
        queue.add(postRequest);

    }

    public void getAppVersion(Context context) throws Exception {
        String url = "https://mobileapi.shjcoop.ae/MobileAppFileServices/Store/ANDROID/digitalsignage/code.json";


// Instantiate the RequestQueue.
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");

                            Log.d("appcode",String.valueOf(code));

                         mAppUpdateCallback.onUpdateVersionResponse(code);
                        }
                        catch (JSONException e) {
                            Log.d("appcode", e.getLocalizedMessage());

                        }


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("appcode",error.toString());
                    }
                });

        postRequest.setShouldCache(false);
        queue.add(postRequest);

    }






    interface ClientDelegate {
        public  void onClientDeviceResponse(Device device);

    }

    interface AppUpdateDelegate {
        public  void onUpdateVersionResponse(int version);

    }

    }


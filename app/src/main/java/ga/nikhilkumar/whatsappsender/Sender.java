package ga.nikhilkumar.whatsappsender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.opencsv.CSVReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileReader;


public class Sender extends AppCompatActivity {
    SharedPreferences sp;
    Context activityContext = this;
    Button browsebtn, sendbtn, accbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        browsebtn = findViewById(R.id.browsebtn);
        sendbtn = findViewById(R.id.sendbtn);
        accbtn = findViewById(R.id.accbtn);
        checkUpdate();
    }
    private void setOnClick(View v){

        switch (v.getId()){

            case R.id.browsebtn:
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        startActivityForResult(intent, 7);
                    }
                });
                break;

            case R.id.sendbtn:
                v.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(activityContext, WASenderFgSvc.class);
                        intent.putExtra("start", true);
                        sp.edit().putBoolean("running", true).commit();
                        startService(intent);
                    }
                });
                break;

            case R.id.accbtn:
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                });
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 7 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                try {
                    Log.e("data", new CSVReader(new FileReader(uri.getPath())).readAll().get(0).toString());
                } catch (java.io.IOException e) {
                    Toast.makeText(activityContext, "Error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onResume() {
        if (!sp.getBoolean("running", false)) {
            Intent intent = new Intent(this, WASenderFgSvc.class);
            stopService(intent);
        }
        super.onResume();
    }

    private void checkUpdate() {
        RequestQueue mRequestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest("https://api.nikhilkumar.ga/version/whatsappsender/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("latest") == 1) {
                        setOnClick(browsebtn);
                        setOnClick(sendbtn);
                        setOnClick(accbtn);
                    } else {
                        Toast.makeText(activityContext, "Update to proceed :(", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(activityContext, "Server Error :( Try again later", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activityContext, "Connectivity Error :(", Toast.LENGTH_SHORT).show();
            }
        });
        request.setShouldCache(false);
        mRequestQueue.add(request);

    }
}

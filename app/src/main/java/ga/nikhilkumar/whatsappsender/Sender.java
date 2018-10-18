package ga.nikhilkumar.whatsappsender;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Sender extends AppCompatActivity {

    Button sendbtn,accbtn,spbtn;
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.edit().putBoolean("sent",false).commit();
        sendbtn = findViewById(R.id.sendbtn);
        spbtn = findViewById(R.id.spbtn);
        accbtn = findViewById(R.id.accbtn);
        setOnClick(sendbtn);
        setOnClick(spbtn);
        setOnClick(accbtn);

        sp.registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        // Implementation
                        if (key.equals("sent")&&prefs.getBoolean("sent",false)){
                            prefs.edit().putBoolean("sent",false).commit();
                            send();
                        }
                    }
                }
        );

    }
    private void setOnClick(View v){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        switch (v.getId()){

            case R.id.sendbtn:
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        send();
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

            case R.id.spbtn:
                sp.edit().putBoolean("sent",true).apply();
                break;
        }
    }
    private void send(){
        Intent intent = new Intent();
        intent.setPackage("com.whatsapp.w4b");
        intent.setAction("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.TEXT","rfghjkghjkljhghjkjhj gyhjk\nhnjmk");
        intent.putExtra("jid","918002572171@s.whatsapp.net");
        startActivity(intent);
    }
}

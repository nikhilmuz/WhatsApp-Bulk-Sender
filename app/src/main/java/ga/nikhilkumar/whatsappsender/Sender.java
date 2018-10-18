package ga.nikhilkumar.whatsappsender;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class Sender extends AppCompatActivity {
    Context activityContext = this;
    Button sendbtn, accbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
        sendbtn = findViewById(R.id.sendbtn);
        accbtn = findViewById(R.id.accbtn);
        setOnClick(sendbtn);
        setOnClick(accbtn);
    }
    private void setOnClick(View v){

        switch (v.getId()){

            case R.id.sendbtn:
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(activityContext, WASenderFgSvc.class);
                        intent.putExtra("start", true);
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

    private void send(String recipient) {
        Intent intent = new Intent();
        intent.setPackage("com.whatsapp.w4b");
        intent.setAction("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "rfghjkghjkljhghjkjhj gyhjk\nhnjmk");
        intent.putExtra("jid", recipient + "@s.whatsapp.net");
        startActivity(intent);
    }
}

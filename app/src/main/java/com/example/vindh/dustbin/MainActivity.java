package com.example.vindh.dustbin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    String Username = "dustbin";
    String Password = "censific";
    String host = "tcp://10.56.10.173:1883";
    String topicstr = "Home/dustbin";
    String topicnoti="Home/noti";
    String topicstat="Home/status";
    TextView m;
    String message;
    Vibrator vib;
    Ringtone rt;
    MqttAndroidClient client,clienttwo;

    NotificationCompat.Builder notification;
    final int uniqueID = 45612;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);



        m = (TextView) findViewById(R.id.tvv);
        vib=(Vibrator) getSystemService(VIBRATOR_SERVICE);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        rt=RingtoneManager.getRingtone(getApplicationContext(),uri);



        String clientId = "dustbin";
        client = new MqttAndroidClient(MainActivity.this, host, clientId);

        clienttwo = new MqttAndroidClient(MainActivity.this, host, clientId);
        MqttConnectOptions options = new MqttConnectOptions();



        try {
            IMqttToken token = clienttwo.connect();

            token.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


        try {
            IMqttToken token = client.connect();

            token.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    setsubscriber();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("hey in arrived");

                if (topic.equals("Home/noti")) {
                    shownotification();
                } else {
                    m.setText(new String(message.getPayload()));
                    vib.vibrate(500);
                    rt.play();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });




    }

    private void setsubscriber() {

        try {
            client.subscribe(topicstat, 0);
            System.out.println("hey subscribed to status");
            client.subscribe(topicnoti,0);
            System.out.println("hey subscribed to notification");

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public void pub(View v) {

        message = "Send Status";

        try {
            Toast.makeText(MainActivity.this, "Sent", Toast.LENGTH_SHORT).show();

            clienttwo.publish(topicstr, message.getBytes(), 0, false);


        } catch (MqttException e) {
            e.printStackTrace();
        }


    }


    public void shownotification(){
        notification.setSmallIcon(R.drawable.icon);
        notification.setTicker("This is the ticker");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("DUSTBIN FULL");
        notification.setContentText("Dustbin is full, please go and clean it ASAP");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        //Builds notification and issues it
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        rt.play();
        vib.vibrate(500);
        nm.notify(uniqueID, notification.build());
    }
}




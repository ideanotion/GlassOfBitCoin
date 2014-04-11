package com.example.bitcoin_glass;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.*;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.Bundle;
import android.os.IBinder;
import android.util.JsonReader;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;
//import com.gdkdemo.livecard.common.LiveCardDemoConstants;
import com.example.bitcoin_glass.Log;
import com.example.bitcoin_glass.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

// ...
public class BitCoinService extends Service
{
    // "Life cycle" constants
	 //public static final String PREFS_NAME = "MyPrefsFile";
	 //public static final String PREFS_VAR = "BitCoinValue";

    // [1] Starts from this..
    private static final int STATE_NORMAL = 1;

    // [2] When panic action has been triggered by the user.
    private static final int STATE_PANIC_TRIGGERED = 2;

    // [3] Note that cancel, or successful send, etc. change the state back to normal
    // These are intermediate states...
    private static final int STATE_CANCEL_REQUESTED = 4;
    private static final int STATE_CANCEL_PROCESSED = 8;
    private static final int STATE_PANIC_PROCESSED = 16;
    // ....

    // Global "state" of the service.
    // Currently not being used...
    private int currentState;


    // For live card
    private LiveCard liveCard;

    private static final String cardId = "livecarddemo2_card";

    // "Heart beat".
    private Timer heartBeat = null;

    // No need for IPC...
    public class LocalBinder extends Binder {
        public BitCoinService getService() {
            return BitCoinService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();


    public BitCoinService()
    {
        super();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        currentState = STATE_NORMAL;
        
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.xxx");  
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i("Received start id " + startId + ": " + intent);

        onServiceStart();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // ????
        onServiceStart();
        return mBinder;
    }

    @Override
    public void onDestroy()
    {
        // ???
    	if(heartBeat != null) {
            heartBeat.cancel();
            heartBeat = null;
        }
        onServiceStop();

        super.onDestroy();
    }


    // Service state handlers.
    // ....

    private boolean onServiceStart()
    {
        Log.d("onServiceStart() called.");

        publishCard(this);
        if(heartBeat == null) {
            heartBeat = new Timer();
        }
        startHeartBeat();

        currentState = STATE_NORMAL;
        return true;
    }

    private boolean onServicePause()
    {
        //Log.d("onServicePause() called.");
        return true;
    }
    private boolean onServiceResume()
    {
        //Log.d("onServiceResume() called.");
        return true;
    }

    private boolean onServiceStop()
    {
        //Log.d("onServiceStop() called.");

    	  unpublishCard(this);
          // ...

          // Stop the heart beat.
          // ???
          // onServiceStop() is called when the service is destroyed.... ??? Need to check
          if(heartBeat != null) {
              heartBeat.cancel();
          }
        // ...

        return true;
    }


    // For live cards...

    private void publishCard(Context context)
    {
        //Log.d("publishCard() called.");
    	if (liveCard == null) {
            TimelineManager tm = TimelineManager.from(context);
            liveCard = tm.createLiveCard(cardId);
//             // liveCard.setNonSilent(false);       // Initially keep it silent ???
//             liveCard.setNonSilent(true);      // for testing, it's more convenient. Bring the card to front.
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.bitcoinlivecard);
            liveCard.setViews(remoteViews);
            //Intent intent = new Intent(context, MainActivity.class);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            liveCard.setAction(PendingIntent.getActivity(/*context*/this, 0, intent, 0));
            liveCard.publish(LiveCard.PublishMode.REVEAL);
        } else {
            // Card is already published.
            return;
        }
    }
    // This will be called by the "HeartBeat".
    public void updateCard(Context context, String numb) throws IOException, JSONException
    {
        //Log.d("updateCard() called.");
        // if (liveCard == null || !liveCard.isPublished()) {
        if (liveCard == null) {
            // Use the default content.
            publishCard(context);
        } else {
            // Card is already published.

//            // ????
//            // Without this (if use "republish" below),
//            // we will end up with multiple live cards....
//            liveCard.unpublish();
//            // ...


            // getLiveCard() seems to always publish a new card
            //       contrary to my expectation based on the method name (sort of a creator/factory method).
            // That means, we should not call getLiveCard() again once the card has been published.
//            TimelineManager tm = TimelineManager.from(context);
//            liveCard = tm.createLiveCard(cardId);
//            liveCard.setNonSilent(true);       // Bring it to front.
            // TBD: The reference to remoteViews can be kept in this service as well....
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.bitcoinlivecard);
            String content = "";

            
            
            // http call ends ----------
            long now = System.currentTimeMillis();
            //content = "Updated: " + now;
            content = numb;
            // ...

            //livecard_header
            String header_string="BTC - USD";
            String money_symbol="$";
            if(BitCoinOfGlassConstants.CUR_POS>0)
            {
            	header_string="BTC - "+BitCoinOfGlassConstants.mString[BitCoinOfGlassConstants.CUR_POS];
            	money_symbol = BitCoinOfGlassConstants.mSymbol[BitCoinOfGlassConstants.CUR_POS];
            }
            remoteViews.setCharSequence(R.id.livecard_header, "setText", header_string);
            remoteViews.setCharSequence(R.id.livecard_content, "setText", money_symbol+content);
            liveCard.setViews(remoteViews);

            // Do we need to re-publish ???
            // Unfortunately, the view does not refresh without this....
            /*Intent intent = new Intent(context, BitCoinService.class);
            liveCard.setAction(PendingIntent.getActivity(context, 0, intent, 0));
            */
            
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            liveCard.setAction(PendingIntent.getActivity(context, 0, intent, 0));
            // Is this if() necessary???? or Is it allowed/ok not to call publish() when updating????
            liveCard.publish(LiveCard.PublishMode.REVEAL);
            /*if(! liveCard.isPublished()) {
                liveCard.publish(LiveCard.PublishMode.REVEAL);
            } else {
                // ????
                // According to the doc,
                // it appears we should call publish() every time the content changes...
                // But, it seems to work without re-publishing...
                //if(Log.D) Log.d("liveCard not published at " + now);
            }*/
        }
    }

    private void unpublishCard(Context context)
    {
        //Log.d("unpublishCard() called.");
        if (liveCard != null) {
            liveCard.unpublish();
            liveCard = null;
        }
    }


    private void startHeartBeat()
    {
        final Handler handler = new Handler();
        TimerTask liveCardUpdateTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //updateCard(BitCoinService.this);
                        	new RetrieveFeedTask().execute("asdf");
                        } catch (Exception e) {
                            Log.e("Failed to run the task.", e);
                        }
                    }
                });
            }
        };
        heartBeat.schedule(liveCardUpdateTask, 0L, 10000L); // Every 10 seconds...
    }

    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
      }

      public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
          BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          String jsonText = readAll(rd);
          JSONObject json = new JSONObject(jsonText);
          return json;
        } finally {
          is.close();
        }
      }
    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

    	    private Exception exception;

    	    protected String doInBackground(String... urls) {
    	        try {
    	        	
    	        	
    	        	String requestURL = "http://blockchain.info/ticker";
    	            URL webRequest= new URL(requestURL);
    	         
    	            JSONObject json = readJsonFromUrl(requestURL);

    	            //JSONObject json = readJsonFromUrl(requestURL);
    	            //test2
    	            String cur_string = "USD";
    	            if(BitCoinOfGlassConstants.CUR_POS>0)
    	            {
    	            	cur_string=BitCoinOfGlassConstants.mString[BitCoinOfGlassConstants.CUR_POS];
    	            }
    	            
    	            String output =json.getJSONObject(cur_string).getString("15m"); 
    	            
    	            Double output_num= Double.parseDouble(output);
    	            Double roundOff = Math.round(output_num * 100.0) / 100.0;
    	            output = String.valueOf(roundOff);
    	            
    	            Log.i("BitCoin Service: bit value-"+ output);
    	           
    	            SharedPreferences settings = getSharedPreferences(BitCoinOfGlassConstants.PREFS_NAME, 0);
    	            SharedPreferences.Editor editor = settings.edit();
    	            editor.putString(BitCoinOfGlassConstants.PREFS_VAR, output);
    	            editor.commit();
    	            
    	            return output;
    	        } catch (Exception e) {
    	            this.exception = e;
    	            Log.i("bitcoin: . exception"+e);
    	            return null;
    	        }
    	    }

    	    protected void onPostExecute(String feed) {
    	        // TODO: check this.exception 
    	        // TODO: do something with the feed
    	    	try{
    	    	if(feed!=null)
    	    	{
    	    		updateCard(BitCoinService.this, feed);
    	    	}
    	    	}catch(Exception e)
    	    	{
    	    		
    	    	}
    	    }
    	    
    	   
    	}
       
   }    

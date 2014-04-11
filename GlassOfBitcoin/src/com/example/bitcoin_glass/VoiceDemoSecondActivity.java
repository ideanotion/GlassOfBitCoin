package com.example.bitcoin_glass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.TextView;


import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import com.example.bitcoin_glass.BitCoinOfGlassConstants;

import java.util.ArrayList;

// The alternative activity
public class VoiceDemoSecondActivity extends Activity
{
	private TextToSpeech mSpeech;
	// public static final String PREFS_NAME = "MyPrefsFile";
	// public static final String PREFS_VAR = "BitCoinValue";
    // For tap events
    private GestureDetector mGestureDetector;
    // Voice action.
    private String voiceAction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("bitcoin voice demo onCreate() called.");

        setContentView(R.layout.activity_voicedemo2_second);

        // For gesture handling.
        mGestureDetector = createGestureDetector(this);
        voiceAction = getVoiceAction(getIntent());
        mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Do nothing.
            	readCardAloud(voiceAction);
            }
        });
        
        if(Log.I) Log.i("voiceAction = " + voiceAction);
        processVoiceAction(voiceAction);
        
        /*TextView t=new TextView(this); 

        t=(TextView)findViewById(R.id.voicedemo2_second_main_content); 
        t.setText("Step One: blast egg");*/
    }
    @Override
    public void onDestroy() {
     
        mSpeech.shutdown();

     
        super.onDestroy();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d("onResume() called.");

        // This makes it impossible to exit the app.
//        voiceAction = getVoiceAction(getIntent());
//        if(Log.I) Log.i("voiceAction = " + voiceAction);
//        processVoiceAction(voiceAction);
    }

    // Returns the "first" word from the phrase following the prompt.
    private String getVoiceAction(Intent intent)
    {
        if(intent == null) {
            return null;
        }
        String action = null;
        Bundle extras = intent.getExtras();
        ArrayList<String> voiceActions = null;
        if(extras != null) {
            voiceActions = extras.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
            if(voiceActions != null && !voiceActions.isEmpty()) {
                if(Log.D) {
                    for(String a : voiceActions) {
                        Log.d("action = " + a);
                    }
                }
                action = voiceActions.get(0);
            }
        }
        return action;
    }

    // Opens the WordDictation activity,
    // or, quits the program.
    private void processVoiceAction(String voiceAction)
    {
    	//update card
    	
    	updateCard(voiceAction);
    	/*
        if(voiceAction != null) {
            if(voiceAction.equals(VoiceDemoConstants.ACTION_START_MAIN_ACTIVITY)
            || voiceAction.equals(VoiceDemoConstants.ACTION_START_FIRST_ACTIVITY)) {
                Log.i("Starting VoiceDemo2 main activity.");
                openVoiceDemoMainActivity();
                this.finish();   // ???
            } else if(voiceAction.equals(VoiceDemoConstants.ACTION_START_SECOND_ACTIVITY)) {
                Log.i("VoiceDemo2 second activity is being started.");
            } else if(voiceAction.equals(VoiceDemoConstants.ACTION_STOP_VOICEDEMO)) {
                Log.i("VoiceDemo2 second activity has been terminated upon start.");
                this.finish();
            } else {
                Log.w("Unknown voice action: " + voiceAction);
            }
        } else {
            Log.w("No voice action provided.");
        }*/
    }
    private void updateCard(String voiceAction)
    {
    	
    	TextView t=new TextView(this); 
    	t=(TextView)findViewById(R.id.voicedemo2_second_main_content);
    	t.setText(voiceAction);

    }
    
    private void readCardAloud(String voiceAction)
    {
    	SharedPreferences settings = getSharedPreferences(BitCoinOfGlassConstants.PREFS_NAME, 0);
        String curBitValue = settings.getString(BitCoinOfGlassConstants.PREFS_VAR, "");
    	try{
	        //Assume voiceAction is number
	        Double output_num= Double.parseDouble(curBitValue);
	        Double transfer_number = Double.parseDouble(voiceAction);
	        
	        Double result_num= transfer_number/output_num;
	        
	        result_num = Math.round(result_num * 100.0) / 100.0;
	        String final_string = String.valueOf(result_num);
	        Log.i("bitcoin voice speaking:"+final_string);
	        String headingText = "Bitcoin value of "+transfer_number+" dollars is "+final_string;
	    	mSpeech.speak(headingText, TextToSpeech.QUEUE_FLUSH, null);  
	    	
	    	updateCard(headingText);
	    } catch (Exception e) {
	    	
	    	Log.i("bitcoin voice exception:"+e);
	    	String headingText = "Your input is :"+voiceAction+" dollars, and the current bit coin value is:"+curBitValue;
	    	mSpeech.speak(headingText, TextToSpeech.QUEUE_FLUSH, null);    	
	    }
        
    	//String headingText = "The bitcoin value is :"+voiceAction+" dollars, and the cur bit coin value is:"+curBitValue;
    	//mSpeech.speak(headingText, TextToSpeech.QUEUE_FLUSH, null);    	
    }
    
    private void openVoiceDemoMainActivity()
    {
        /*Intent intent = new Intent(this, VoiceDemoActivity.class);
        // ???
        startActivity(intent.setFlags(    // Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP));*/
    }


    ///////////////////////////////////////////////////////
    /// Gesture handling
    //

    @Override
    public boolean onGenericMotionEvent(MotionEvent event)
    {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return super.onGenericMotionEvent(event);
    }

    private GestureDetector createGestureDetector(Context context)
    {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if(Log.I) Log.i("gesture = " + gesture);
                if (gesture == Gesture.TAP) {
                    //openVoiceDemoMainActivity();
                    return true;
                } // etc...
                return false;
            }
        });
        return gestureDetector;
    }
    public final class VoiceDemoConstants
    {
        // The name of the "extra" key for VoiceDemo2 intent service.
        public static final String EXTRA_KEY_VOICE_ACTION = "extra_action";

        // Action commands.
        public static final String ACTION_START_MAIN_ACTIVITY = "main activity";
        public static final String ACTION_START_FIRST_ACTIVITY = "first activity";
        public static final String ACTION_START_SECOND_ACTIVITY = "second activity";
        public static final String ACTION_STOP_VOICEDEMO = "stop voice demo";
        // ...

    }

}

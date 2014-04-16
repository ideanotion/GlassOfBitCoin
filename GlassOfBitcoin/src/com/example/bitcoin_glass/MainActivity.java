package com.example.bitcoin_glass;


import com.example.bitcoin_glass.SetCurrencyScrollAdapter.CurrencyComponents;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.touchpad.GestureDetector.BaseListener;
import com.google.android.glass.widget.CardScrollView;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity {
    /** Request code for setting the timer. */
    private static final int SET_TIMER = 100;
    public static final String EXTRA_CURRENCY = "currency_value";

    private BitCoinService bitCoinService;
    private boolean mResumed;
    private boolean mSettingTimer;
    
    private static final int SELECT_VALUE = 100;
    
    private AudioManager mAudioManager;
    private CardScrollView mView;
    //private GestureDetector mDetector;
    private SetCurrencyScrollAdapter mAdapter;
    private ServiceConnection mConnection = new ServiceConnection() {
    	  @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
              if (service instanceof BitCoinService.LocalBinder) {
              	bitCoinService = ((BitCoinService.LocalBinder) service).getService();
                  openOptionsMenu();
              }
              // No need to keep the service bound.
              unbindService(this);
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {
              // Nothing to do here.
          }
    };

    private void doStopService()
    {
        stopService(new Intent(this, BitCoinService.class));
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAdapter = new SetCurrencyScrollAdapter(this);
        mAdapter.setCurrencyValue(getIntent().getLongExtra(EXTRA_CURRENCY, 0));

        mView = new CardScrollView(this) {
            @Override
            public final boolean dispatchGenericFocusedEvent(MotionEvent event) {
                /*if (mDetector.onMotionEvent(event)) {
                    return true;
                }*/
                return super.dispatchGenericFocusedEvent(event);
            }
        };
        mView.setAdapter(mAdapter);
        setContentView(mView);

        //mDetector = new GestureDetector(this);//.setBaseListener(this);
        bindService(new Intent(this, BitCoinService.class), mConnection, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        mResumed = true;
        openOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    public void openOptionsMenu() {
    	Log.i("btc: openOptionsMenu");
        if (mResumed && bitCoinService != null) {
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.i("btc: onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final boolean timeSet = true;//mTimer.getDurationMillis() != 0;
        Log.i("btc: onPrepareOptionsMenu");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.action_settings:
            	setCurrency();
            	break;

            case R.id.stop:
            	doStopService();
            	finish();
                return true;
            /*case R.id.voice:
            	openVoiceDemoSecondActivity();
            	break;*/
            case R.id.camera:
            	openCamActivity();
            	break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
    	Log.i("bitcoin mainactivity setCurrency()");
        if (!mSettingTimer) {
            // Nothing else to do, closing the Activity.
            finish();
            
        }
    }

    private void setCurrency() {
    	Log.i("bitcoin mainactivity setCurrency()");
    	
    	 int position = mView.getSelectedItemPosition();
    	 Log.i("bitcoin mainactivity setCurrency(): "+position);
         SetCurrencyScrollAdapter.CurrencyComponents component =
                 (SetCurrencyScrollAdapter.CurrencyComponents) mAdapter.getItem(position);
         Intent selectValueIntent = new Intent(this, SelectValueActivity.class);

         selectValueIntent.putExtra(SelectValueActivity.EXTRA_COUNT, component.getMaxValue());
         selectValueIntent.putExtra(
                 SelectValueActivity.EXTRA_INITIAL_VALUE,
                 (int) mAdapter.getCurrencyComponent(component));
         startActivityForResult(selectValueIntent, SELECT_VALUE);
         mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
         mSettingTimer = true;
         Log.i("bitcoin mainactivity setCurrency() - everything is alright: "+(int) mAdapter.getCurrencyComponent(component));
    }
    
    @Override
    public void onBackPressed() {
    	Log.i("bitcoin - onbackpressed:"+mAdapter.getCurrencyValue());
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_CURRENCY, mAdapter.getCurrencyValue());
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.i("bitcoin - onActivityResult: nothing to show");
        if (resultCode == RESULT_OK && requestCode == SELECT_VALUE) {
            int position = mView.getSelectedItemPosition();
            CurrencyComponents component =
                    (CurrencyComponents) mAdapter.getItem(position);

            mAdapter.setCurrencyComponent(
                    component, data.getIntExtra(SelectValueActivity.EXTRA_SELECTED_VALUE, 0));
            mView.updateViews(true);
          finish();
        }
    }

    private void openVoiceDemoSecondActivity()
    {
        Intent intent = new Intent(this, VoiceDemoSecondActivity.class);
        startActivity(intent);
       
    }
    
    private void openCamActivity()
    {
        Intent intent = new Intent(this, CamActivity.class);

        startActivity(intent.setFlags(    // Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }
}

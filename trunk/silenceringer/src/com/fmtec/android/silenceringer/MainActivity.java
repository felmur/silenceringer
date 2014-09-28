/*
 * SilenceRinger
 * (c) 2014 by Felice Murolo
 * Salerno - Italy
 * eMail: linuxboy@fel.hopto.org
 * 
 * Released under LGPL License
 * See here for details: http://www.gnu.org/licenses/lgpl-3.0.txt
 * 
 * This code shows how to mute the phone when you receive a call 
 * from someone who is not in the contact list.
 * The code does not just turn off the sound and vibration when
 * a call comes in, but takes account of any audio configuration
 * different from the default, saving values ​​of sound and
 * vibration prior to muting the phone.
 *  
 */
package com.fmtec.android.silenceringer;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.fmtec.android.silenceringer.R;
import android.content.SharedPreferences;


public class MainActivity extends Activity {
	static String TAG="SilenceRinger-MainActivity";
	ToggleButton tb_enable;
	CheckBox cb_ex_contacts;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        tb_enable = (ToggleButton) findViewById(R.id.tb_enable);
        tb_enable.setOnCheckedChangeListener(tb_enable_click);
        cb_ex_contacts=(CheckBox) findViewById(R.id.cb_ex_contacts);
        cb_ex_contacts.setOnCheckedChangeListener(cb_click);
        
        loadPref();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    // click sul pulstante enable/disable servizio
    OnCheckedChangeListener tb_enable_click = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton v,boolean s) {
			Log.d(TAG,"Enable Service = "+s);
		}
	};

	// gestisce i click sui vari CheckBox
	OnCheckedChangeListener cb_click = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton v,boolean s) {
			switch(v.getId()) {
			case R.id.cb_ex_contacts:
				Log.d(TAG,"CheckBox Exclude Contacts = "+s);
				break;
			}
		}
	};
	
	protected void loadPref() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		tb_enable.setChecked(prefs.getBoolean("ServiceEnabled", false));
  	  	Log.d(TAG, "Getting boolean ServiceEnabled: "+tb_enable.isChecked());
		cb_ex_contacts.setChecked(prefs.getBoolean("ExcludeContacts", false));
  	  	Log.d(TAG, "Getting boolean ExcludeContacts: "+cb_ex_contacts.isChecked());
	}
	
	protected void savePref(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("ServiceEnabled",tb_enable.isChecked());
		editor.putBoolean("ExcludeContacts",cb_ex_contacts.isChecked());
		editor.commit();
		Log.d(TAG, "Preferences saved");
	}

	@Override
    public void onStop() {
    	super.onStop();
    	savePref();
    	Log.d(TAG,"OnStop");
    }
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.d(TAG,"OnDestroy");
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Log.d(TAG,"OnResume");
    }

	
}

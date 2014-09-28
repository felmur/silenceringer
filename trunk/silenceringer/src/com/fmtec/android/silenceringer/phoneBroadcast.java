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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class phoneBroadcast extends BroadcastReceiver {
	private static final String TAG = "phoneBroadCast";
	private boolean enabled,ex_contacts,ring,already_done=false;
	// valori di default per audio e vibrate
	private int audio, vibrate;
	
	public phoneBroadcast() {
		Log.d(TAG,"Sono dentro il phoneBroadCast");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d(TAG,"broadcastManager Receive");
		TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		PhoneStateListener psl = new PhoneStateListener();
		telephony.listen(psl, PhoneStateListener.LISTEN_CALL_STATE);
		AudioManager am = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		enabled=prefs.getBoolean("ServiceEnabled", false);
		ex_contacts=prefs.getBoolean("ExcludeContacts", false);
		Log.d(TAG,"ServiceEnabled: "+enabled);
		Log.d(TAG,"ExcludeContacts: "+ex_contacts);
		
		// ring is true if phone must ring, false otherwise. Default is FALSE (phone doesn't ring for all the calls).
		ring=false;
		
		switch(telephony.getCallState()) {
		case TelephonyManager.CALL_STATE_RINGING:
			Log.d(TAG,"Call State Ringing");
			if (enabled) {
				
				Bundle bundle = intent.getExtras();
				String phoneNr="";
				if (bundle != null)
				{
					phoneNr=bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
				}
				if (phoneNr==null) phoneNr="";
				Log.d(TAG, "phoneNr: <"+phoneNr+">");
				
				// if you have checked the "exclude contacts" combobox, we going to check if the number
				// is one of your Phone Contact. If it is, ring value goes to true.
				if (ex_contacts) {
					// tira fuori tutti i numeri dai contatti
					Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
					while (phones.moveToNext())
					{
						String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
						String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						phoneNumber = phoneNumber.replaceAll("-", "");
						phoneNumber = phoneNumber.replaceAll(" ", "");
						phoneNumber = phoneNumber.trim();
						if (phoneNr.endsWith(phoneNumber)) {
							Log.d(TAG,"Found excluded contact: "+name+" - "+phoneNumber);
							ring=true;
							break;
						}
					}
					phones.close();
				}
				
				// get audio & vibrate value only on first ringing and before silencing phone, and save it into SharedPreferences
				if (!already_done) {
					audio = am.getRingerMode();
					vibrate = am.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
					editor.putInt("AudioValue", audio);
					editor.putInt("VibrateValue", vibrate);
					editor.commit();
					Log.d(TAG,"Audio: "+audio+", Vibrate: "+vibrate);
					already_done=true;
				}
				// silence ring if ring value is false
				if (!ring) {
					am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
					am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					Log.d(TAG,"Ringer set to OFF");
				}
			}
			break;
			
		case TelephonyManager.CALL_STATE_IDLE:
			Log.d(TAG,"Call State Idle");
			// restore previous value of audio & vibrate
			if (enabled) {
				audio=prefs.getInt("AudioValue", AudioManager.RINGER_MODE_NORMAL);
				vibrate=prefs.getInt("VibrateValue", AudioManager.VIBRATE_SETTING_ONLY_SILENT);
				Log.d(TAG,"-Audio: "+audio+", -Vibrate: "+vibrate);
				am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, vibrate);
				am.setRingerMode(audio);
				Log.d(TAG,"Ringer ON");
			}
			already_done=false;
			break;
		}
	}
}


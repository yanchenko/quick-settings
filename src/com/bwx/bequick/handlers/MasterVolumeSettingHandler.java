/*
 * Copyright (C) 2010 beworx.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bwx.bequick.handlers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.bwx.bequick.Constants;
import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.RangeSetting;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

public class MasterVolumeSettingHandler extends SettingHandler {

	//private static final String TAG = "MasterVolumeSettingHandler";
	private AudioManager mManager;
	
	// cache
	private BroadcastReceiver mVolumeReceiver;
	private IntentFilter mFilter;
	
	class VolumeChangedReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			updateSettingState();
		}
	}
	
	public MasterVolumeSettingHandler(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) throws Exception {
		mActivity = activity;
		AudioManager manager = mManager;
		if (manager == null) {
			manager = (AudioManager) mActivity.getSystemService(Activity.AUDIO_SERVICE);
			mManager = manager;
		}
		updateSettingState();
		
		// register volume receiver
		BroadcastReceiver receiver = mVolumeReceiver;
		IntentFilter filter = mFilter;
		if (receiver == null) {
			mVolumeReceiver = receiver = new VolumeChangedReceiver();
			mFilter = filter = new IntentFilter(Constants.ACTION_VOLUME_UPDATED);
		}
		activity.registerReceiver(receiver, filter);
	}

	private void updateSettingState() {
		
		AudioManager manager = mManager;
		
		// get max value for state
		int v1 = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int v2 = manager.getStreamVolume(AudioManager.STREAM_RING);
		int v3 = manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
		int v4 = manager.getStreamVolume(AudioManager.STREAM_ALARM);
		int value = Math.max(v1, v2 * 2 -1);
		value = Math.max(value, v3 * 2 - 1);
		value = Math.max(value, v4 * 2 -1);
		
		RangeSetting setting = (RangeSetting) mSetting;
		setting.value = value;
		setting.descr = mActivity.getString(R.string.txt_master_volume_desc, v2, v3, v1, v4);
		setting.updateView();
		
	}
	
	@Override
	public void deactivate() {
		mActivity.unregisterReceiver(mVolumeReceiver);
	}

	@Override
	public void onSelected(int buttonIndex) {
		// do nothing
	}

	@Override
	public void onSwitched(boolean isSwitched) {
		// do nothing
	}

	@Override
	public void onValueChanged(int value) {

		RangeSetting setting = (RangeSetting) mSetting;
		setting.value = value;
		
		AudioManager manager = mManager;
		
		int value7;
		switch (value) {
			case 0:
			case 1:
				value7 = value; // first two values are the same
				break;
			case 13:
				value7 = 6;
				break;
			default:
				value7 = Math.round(value * 0.5f); // volume with max=7, value7 should always be louder then media volume
				value7 = Math.min(value7, 7);
				break;
		}
		
		//Log.d(TAG, "set volume, " + value + ", " + value7);
		
		manager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
		manager.setStreamVolume(AudioManager.STREAM_RING, value7, 0);
		manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, value7, 0);
		manager.setStreamVolume(AudioManager.STREAM_ALARM, value7, AudioManager.FLAG_PLAY_SOUND);
		
		if (manager.getRingerMode() == AudioManager.RINGER_MODE_SILENT & value7 > 0) {
			manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		} else if (manager.getRingerMode() != AudioManager.RINGER_MODE_SILENT && value7 == 0) {
			manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		}

		setting.descr = mActivity.getString(R.string.txt_master_volume_desc, value7, value7, value, value7);
		setting.updateView();
		
	}
	
}


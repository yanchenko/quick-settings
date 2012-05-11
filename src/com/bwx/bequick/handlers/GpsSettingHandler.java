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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

public class GpsSettingHandler extends SettingHandler {

	private static final String TAG = "qs.gps";
	
	public GpsSettingHandler(Setting setting) {
		super(setting);
	}

	private void updateSetting(boolean gpsEnabled) {
		Setting setting = mSetting;
		setting.descr = mActivity.getString(gpsEnabled ? R.string.txt_status_turned_on : R.string.txt_status_turned_off);
		setting.checked = gpsEnabled;
		setting.updateView();
	}
	
	@Override
	public void activate(MainSettingsActivity activity) {
		mActivity = activity;
		LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		updateSetting(manager.isProviderEnabled(LocationManager.GPS_PROVIDER));
	}

	@Override
	public void deactivate() {
		// do nothing
	}

	@Override
	public void onSelected(int buttonIndex) {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		mActivity.startActivity(intent);
	}

	@Override
	public void onSwitched(boolean isSwitched) {
		try {
			// try to change settings on a rooted phone
			switchGps(isSwitched);
			updateSetting(isSwitched);
		} catch (Exception e) {
			Log.e(TAG, "", e);
			// this is not a rooted phone - just shortcut to a setting page
			onSelected(0);
		}
	}

	private void switchGps(boolean isSwitched) {
		
		final ContentResolver resolver = mActivity.getContentResolver();
		final String allowedProviders = Settings.Secure.getString(resolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		String newAllowedProviders = null;
		
		int index = allowedProviders.indexOf(LocationManager.GPS_PROVIDER);
		if (isSwitched) {
			
			// add provider to the list
			if (index == -1) {
				// GPS is off and we need to add GPS provider
				newAllowedProviders = allowedProviders;
				if (allowedProviders.length() != 0) {
					newAllowedProviders += ',';
				}
				newAllowedProviders += LocationManager.GPS_PROVIDER;
			} // else provider is already in the list
			
		} else {
			
			// remove provider from the list
			if (index > -1) { // provider is in the list
				newAllowedProviders = allowedProviders.substring(0, index);
				int nextCommaIndex = allowedProviders.indexOf(',', index);
				if (nextCommaIndex > -1) {
					newAllowedProviders += allowedProviders.substring(nextCommaIndex + 1);
				}
				if (newAllowedProviders.endsWith(",")) {
					newAllowedProviders = newAllowedProviders.substring(0, newAllowedProviders.length() - 1);
				}
			}
		}

		if (newAllowedProviders != null) {
			Settings.Secure.putString(resolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newAllowedProviders);
			Log.d(TAG, "switched: " + isSwitched + ", allowed providers: " + newAllowedProviders);
		}
		
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing, not supported
	}
	
}

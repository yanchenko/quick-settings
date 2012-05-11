package com.bwx.bequick.handlers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Toast;

import com.bwx.bequick.MainSettingsActivity;
import com.bwx.bequick.R;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingHandler;

public class MobileDataSettingHandler2 extends SettingHandler {

	private static final String TAG = "qs.md";
	
	private static class TelephonyManagerExt {
		
		private final ConnectivityManager mConnManager;
		
		public TelephonyManagerExt(Context context) {
			mConnManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		}
		
		public boolean setMobileDataEnabled(boolean enabled) {
			return enabled; // TODO fixme
		}
		
		public NetworkInfo getMobileDataInfo() {
			return mConnManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		}
		
		public NetworkInfo getWiFiInfo() {
			return mConnManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				updateState(info.getState());
			}
		}
	};
	
	private TelephonyManagerExt mTelephonyManager;
	private int mTryCounter;
	
	public MobileDataSettingHandler2(Setting setting) {
		super(setting);
	}

	@Override
	public void activate(MainSettingsActivity activity) throws Exception {
		mActivity = activity;
		mTelephonyManager = new TelephonyManagerExt(activity);
		activity.registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		updateState(mTelephonyManager.getMobileDataInfo().getState());
	}

	@Override
	public void deactivate() {
		mActivity.unregisterReceiver(mReceiver);
	}

	@Override
	public void onSelected(int buttonIndex) {
		Intent intent = new Intent(Intent.ACTION_VIEW); intent.setClassName("com.android.phone", "com.android.phone.Settings");
		mActivity.startActivitiesSafely(intent, new Intent(Settings.ACTION_WIRELESS_SETTINGS));
	}

	@Override
	public void onSwitched(boolean enable) {

		boolean mobileDataAllowed = Settings.Secure.getInt(mActivity.getContentResolver(), "mobile_data", 1) == 1;
		if (enable && !mobileDataAllowed) {
			// cannot switch - system Mobile Data should be enabled first
			Toast.makeText(mActivity, R.string.txt_enable_data_access_first, Toast.LENGTH_LONG).show();
			onSelected(0); // open system settings
			return;
		}
		
		boolean wifiConnected = mTelephonyManager.getWiFiInfo().getState() == State.CONNECTED;
		if (enable && wifiConnected) {
			// cannot switch - WiFi should be disabled first
			WifiManager wifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
			wifiManager.setWifiEnabled(false);
		}
		
		mTelephonyManager.setMobileDataEnabled(enable);
		if (enable) {
			if (++mTryCounter > 2) {
				// show 2G hint
				Toast.makeText(mActivity, R.string.msg_2g_hint, Toast.LENGTH_LONG).show();
				if (mTryCounter > 3) {
					onSelected(0); // open system settings
				}
			} else {
				Toast.makeText(mActivity, R.string.msg_enabling_mobile_data, Toast.LENGTH_SHORT).show();
			}
		}
		
		updateState(enable ? mTelephonyManager.getMobileDataInfo().getState() : State.DISCONNECTING);
	}

	@Override
	public void onValueChanged(int value) {
		// do nothing
	}

	void updateState(State state) {
		
		if (state != State.DISCONNECTED) {
			mTryCounter = 0;
		}
		
		Setting s = mSetting; int resId;
		switch (state) {
			case CONNECTED: s.checked = true; s.enabled = true; resId = R.string.txt_net_status_connected; break;
			case CONNECTING: s.checked = false; s.enabled = false; resId = R.string.txt_net_status_connecting; break;
			case DISCONNECTED: s.checked = false; s.enabled = true; resId = R.string.txt_net_status_disconnected; break;
			case DISCONNECTING: s.checked = true; s.enabled = false; resId = R.string.txt_net_status_disconnecting; break;
			default: s.checked = s.enabled = false; resId = R.string.txt_status_unknown; break;
		}
		s.descr = getString(resId);
		s.updateView();
	}
	
}

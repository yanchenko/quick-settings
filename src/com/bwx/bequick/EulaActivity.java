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

package com.bwx.bequick;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

public class EulaActivity extends Activity {

	private AlertDialog mDialog;

	@Override
	protected void onResume() {
		super.onResume();

    	View view = getLayoutInflater().inflate(R.layout.eula, null);
    	TextView textView = (TextView) view.findViewById(R.id.text1);
    	textView.setText(readEula());
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		mDialog = builder.setTitle(R.string.txt_eula).setIcon(android.R.drawable.ic_dialog_info).setCancelable(true)
				.setPositiveButton(R.string.btn_accept, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						onEulaAccepted(true);
					}
				}).setNegativeButton(R.string.btn_decline, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						onEulaAccepted(false);
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						onEulaAccepted(false);
					}
				}).setView(view).create();
		mDialog.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mDialog.dismiss();
	}
	
	private void onEulaAccepted(boolean accepted) {
		if (accepted) {
			// update settings
			SharedPreferences prefs = ((SettingsApplication)getApplication()).getPreferences();
			prefs.edit().putBoolean(Constants.PREF_EULA_ACCEPTED, true).commit();
			// show quick settings
            Intent intent = new Intent(this, ShowSettingsActivity.class);
            startActivity(intent); 					
		}
		finish();
	}

	private String readEula() {
		StringBuilder result = new StringBuilder();
		InputStream is = getResources().openRawResource(R.raw.eula);
		try {
			InputStreamReader reader = new InputStreamReader(is, "UTF-8");
			char[] buf = new char[1024*4];
			int length = reader.read(buf);
			while (length != -1) {
				result.append(buf, 0, length);
				length = reader.read(buf);
			}
			return result.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (IOException ioe) {
			}
		}
	}

}

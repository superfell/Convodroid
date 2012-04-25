// Copyright (c) 2012 Simon Fell
//
// Permission is hereby granted, free of charge, to any person obtaining a 
// copy of this software and associated documentation files (the "Software"), 
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense, 
// and/or sell copies of the Software, and to permit persons to whom the 
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included 
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
// THE SOFTWARE.
//
package com.pocketsoap.convodroid;

import android.accounts.*;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.*;
import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
import com.salesforce.androidsdk.ui.*;
import com.salesforce.androidsdk.ui.OAuthWebviewHelper.OAuthWebviewHelperEvents;

/**
 * This is our LoginActivity, we can't use the one in the SDK directly because we need to extend SherlockActivity
 * 
 * @author @superfell
 */
public class LoginActivity extends SherlockActivity implements OAuthWebviewHelperEvents {

	private final int SETTINGS_REQUEST_CODE = 42;
	
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.login);
		loginOptions = LoginOptions.fromBundle(getIntent().getExtras());
		oauthHelper = new OAuthWebviewHelper(this, loginOptions, (WebView)findViewById(R.id.oauth_webview), state) {
			@Override
			protected String buildAccountName(String username) {
				return username;
			}
		};
		oauthHelper.loadLoginPage();
		
        accountAuthenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        if (accountAuthenticatorResponse != null) 
            accountAuthenticatorResponse.onRequestContinued();
	}
	
	private OAuthWebviewHelper oauthHelper;
	private LoginOptions loginOptions;
	// AccountAuthenticator support, normally you'd get this from AccountAuthenticatorActivity, but we can't use that
	// because we need to extends SherlockActivity to get the actionbar support.
    private AccountAuthenticatorResponse accountAuthenticatorResponse;
    private Bundle resultBundle;
    
	/// OAuthWebviewHelper callbacks
	
	@Override
	public void loadingLoginPage(String loginUrl) {
		Uri u = Uri.parse(loginUrl);
		getSupportActionBar().setSubtitle(u.getHost());
	}

	@Override
	public void onLoadingProgress(int totalProgress) {
		setSupportProgress(totalProgress);
	}

	@Override
	public void onIndeterminateProgress(boolean show) {
		setSupportProgressBarIndeterminateVisibility(show);
	}

	@Override
	public void onAccountAuthenticatorResult(Bundle authResult) {
		resultBundle = authResult;
	}

	@Override
	public void finish() {
        if (accountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (resultBundle != null) {
                accountAuthenticatorResponse.onResult(resultBundle);
            } else {
                accountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
            accountAuthenticatorResponse = null;
        }
        super.finish();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/// Menu/Actionbar
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				Intent i = new Intent(this, ServerPickerActivity.class);
			    startActivityForResult(i, SETTINGS_REQUEST_CODE);

			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}
	
	/**
	 * Called when ServerPickerActivity completes.
	 * Reload login page.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SETTINGS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			oauthHelper.loadLoginPage();
		} else {
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	}
}

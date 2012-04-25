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

import android.app.Activity;
import android.webkit.*;

import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.security.Encryptor;
import com.salesforce.androidsdk.ui.SalesforceR;

/**
 * @author @superfell
 */
public class ConvodroidApp extends ForceApp {

	private static final SalesforceR r = new SalesforceRImpl();
	
	@Override
	public void onCreate() {
		super.onCreate();
		// ensure the cookiesync manager is created for when the webview tries to use it.
		CookieSyncManager.createInstance(this);
	}

	@Override
	public int getLockTimeoutMinutes() {
		return 0;
	}

	@Override
	public Class<? extends Activity> getLoginActivityClass() {
		return LoginActivity.class;
	}

	@Override
	public Class<? extends Activity> getMainActivityClass() {
		return ConversationListActivity.class;
	}

	@Override
	public SalesforceR getSalesforceR() {
		return r;
	}
	
	@Override
	protected String getKey(String name) {
		return Encryptor.hash(name, "@superfell");// TODO, what exactly is this used for?
	}
}

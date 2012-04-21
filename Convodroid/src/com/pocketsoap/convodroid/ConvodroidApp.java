/**
 * 
 */
package com.pocketsoap.convodroid;

import android.app.Activity;

import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.security.Encryptor;
import com.salesforce.androidsdk.ui.SalesforceR;

/**
 * @author @superfell
 */
public class ConvodroidApp extends ForceApp {

	private static final SalesforceR r = new SalesforceRImpl();
	
	@Override
	public int getLockTimeoutMinutes() {
		return 0;
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
		return Encryptor.hash(name, "@superfell");// TODO, what exacly is this used for?
	}
}

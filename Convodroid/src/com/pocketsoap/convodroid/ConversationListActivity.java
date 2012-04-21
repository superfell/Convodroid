/**
 * 
 * 
 */
package com.pocketsoap.convodroid;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
import com.salesforce.androidsdk.rest.ClientManager.RestClientCallback;
import com.salesforce.androidsdk.rest.RestClient;

/**
 * Shows the list of conversations.
 * 
 * @author superfell
 */
public class ConversationListActivity extends SherlockActivity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convo_list);
    }
    
	@Override 
	public void onResume() {
		super.onResume();
		
		// Hide everything until we are logged in
		// findViewById(R.id.root).setVisibility(View.INVISIBLE);
		
		// Login options
		String accountType = ForceApp.APP.getAccountType();
    	LoginOptions loginOptions = new LoginOptions(
    			null, // login host is chosen by user through the server picker 
    			ForceApp.APP.getPasscodeHash(),
    			getString(R.string.oauth_callback_url),
    			getString(R.string.oauth_client_id),
    			new String[] {"api"});
		
		// Get a rest client
		new ClientManager(this, accountType, loginOptions).getRestClient(this, new RestClientCallback() {
			@Override
			public void authenticatedRestClient(RestClient client) {
				if (client == null) {
					ForceApp.APP.logout(ConversationListActivity.this);
					return;
				}
				
				// Show everything
				//findViewById(R.id.root).setVisibility(View.VISIBLE);

				// Show welcome
				//((TextView) findViewById(R.id.welcome_text)).setText(getString(R.string.welcome, client.getClientInfo().username));
				
			}
		});
	}
}
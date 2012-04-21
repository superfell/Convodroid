/**
 * 
 */
package com.pocketsoap.convodroid;

import java.io.IOException;

import org.apache.http.ParseException;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
import com.salesforce.androidsdk.rest.ClientManager.RestClientCallback;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestRequest.RestMethod;
import com.salesforce.androidsdk.rest.RestResponse;


/**
 * @author superfell
 */
public class ConversationListFragment extends SherlockFragment implements RestClientCallback {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.convo_list_f, container, false);
		return v;
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
		new ClientManager(getActivity(), accountType, loginOptions).getRestClient(getActivity(), this);
	}

	@Override
	public void authenticatedRestClient(RestClient client) {
		if (client == null) {
			ForceApp.APP.logout(getActivity());
			return;
		}
		RestRequest rr = new RestRequest(RestMethod.GET, "/services/data/v24.0/chatter/users/me/conversations", null);
		client.sendAsync(rr, new AsyncRequestCallback() {

			@Override
			public void onSuccess(RestResponse response) {
				try {
					Log.i("http", response.asString());
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Exception exception) {
			}
		});
	}

	
}

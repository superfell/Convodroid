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


import org.codehaus.jackson.type.TypeReference;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.*;
import com.pocketsoap.convodroid.data.ConversationSummaryPage;
import com.pocketsoap.convodroid.loaders.JsonLoader;
import com.pocketsoap.convodroid.photos.ImageLoader;
import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
import com.salesforce.androidsdk.rest.ClientManager.RestClientCallback;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestRequest.RestMethod;


/**
 * @author superfell
 */
public class ConversationListFragment extends SherlockListFragment implements RestClientCallback, LoaderCallbacks<ConversationSummaryPage> {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setHasOptionsMenu(true);
	}
	
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

	private ImageLoader imageLoader;
	private RestClient restClient;
	private SummaryAdapter adapter;
	
	@Override
	public void authenticatedRestClient(final RestClient client) {
		if (client == null) {
			ForceApp.APP.logout(getActivity());
			return;
		}
		imageLoader = new ImageLoader(getActivity(), client);
		restClient = client;
		this.getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<ConversationSummaryPage> onCreateLoader(int arg0, Bundle arg1) {
		RestRequest req = new RestRequest(RestMethod.GET, "/services/data/v24.0/chatter/users/me/conversations", null);
		return new JsonLoader<ConversationSummaryPage>(getActivity(), restClient, req, new TypeReference<ConversationSummaryPage>() {} );
	}

	@Override
	public void onLoadFinished(Loader<ConversationSummaryPage> arg0, ConversationSummaryPage page) {
		if (adapter == null) {
			adapter = new SummaryAdapter(getActivity(), imageLoader, page.conversations);
			setListAdapter(adapter);
		} else {
			adapter.addPage(page);
		}
	}

	@Override
	public void onLoaderReset(Loader<ConversationSummaryPage> arg0) {
	}
	
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.summary, menu);
    }
	

    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.action_refresh:
    			refresh();
    			return true;
    			
    		case R.id.action_post:
    			createPost();
    			return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    private void refresh() {
    	getLoaderManager().restartLoader(0, null, this);
    }
    
    private void createPost() {
    }

}

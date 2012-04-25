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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.animation.*;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.*;
import com.pocketsoap.convodroid.photos.ImageLoader;
import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.ClientManager.*;
import com.salesforce.androidsdk.rest.*;

/**
 * Base fragment class with common operations in.
 * 
 * @author @superfell
 */
public class ConversationFragment extends SherlockListFragment implements RestClientCallback {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		
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
	
	protected ImageLoader imageLoader;
	protected RestClient restClient;

	@Override
	public void authenticatedRestClient(final RestClient client) {
		if (client == null) {
			ForceApp.APP.logout(getActivity());
			return;
		}
		imageLoader = ((ConvodroidApp)getActivity().getApplication()).getImageLoader(client);
		restClient = client;
		// Note you have to start the animation first because sometimes the loader already has the data
		// and so the onLoadFinished gets called before the call to initLoader returns.
		startRefreshAnimation();
		initLoader();
	}

	// called when we're ready to start fetching data.
	protected void initLoader() { }
	
	private MenuItem refreshItem;
	private ImageView refreshView;	// view used when we're animating the actionbar refresh icon.
	private Animation refreshAnimation;
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		refreshItem = menu.findItem(R.id.action_refresh);
    }

	protected void clearRefreshView() {
		refreshView = null;
	}
	
	/** start animating the refresh icon in the action bar */
	protected void startRefreshAnimation() {
		if (refreshView == null) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			refreshView = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);
		}
		if (refreshAnimation == null) {
			refreshAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.refresh);
			refreshAnimation.setRepeatCount(Animation.INFINITE);
		}
		if (refreshItem != null) {
			refreshView.startAnimation(refreshAnimation);
			refreshItem.setActionView(refreshView);
		}
	}
	
	/** stop animating the refresh icon in the action bar */
	protected void stopRefreshAnimation() {
		if (refreshItem != null) {
			if (refreshItem.getActionView() != null)
				refreshItem.getActionView().clearAnimation();
			refreshItem.setActionView(null);
		}
	}
}

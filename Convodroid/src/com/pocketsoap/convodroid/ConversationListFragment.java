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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.*;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.pocketsoap.convodroid.data.*;
import com.pocketsoap.convodroid.http.ChatterRequests;
import com.pocketsoap.convodroid.loaders.JsonLoader;
import com.salesforce.androidsdk.rest.RestRequest;


/**
 * @author superfell
 */
public class ConversationListFragment extends ConversationFragment implements LoaderCallbacks<ConversationSummaryPage> {

	private static final int REQUEST_CODE_REFRESH_ON_RETURN = 42;
	private static final int LOADER_INITIAL_LIST = 0;
	private static final int LOADER_PAGE = 1;
	private static final String ARG_URL = "url";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		clearRefreshView();
		View v = inflater.inflate(R.layout.convo_list_f, container, false);
		return v;
	}

	private SummaryAdapter adapter;
	private More moreFooter;
	
	@Override
	protected void initLoader() {
		getLoaderManager().initLoader(LOADER_INITIAL_LIST, null, this);
	}

	@Override
	public Loader<ConversationSummaryPage> onCreateLoader(int loaderId, Bundle args) {
		RestRequest req = loaderId == LOADER_INITIAL_LIST ? ChatterRequests.conversationSummary() : ChatterRequests.conversationSummaryPage(args.getString(ARG_URL));
		return new JsonLoader<ConversationSummaryPage>(getActivity(), restClient, req, new TypeReference<ConversationSummaryPage>() {} );
	}

	@Override
	public void onLoadFinished(Loader<ConversationSummaryPage> arg0, ConversationSummaryPage page) {
		if (adapter == null) {
			createMoreFooter(page.nextPageUrl);
			adapter = new SummaryAdapter(getActivity(), imageLoader, restClient.getClientInfo().userId, page.conversations);
			setListAdapter(adapter);
		} else {
			adapter.addPage(page);
			updateMoreFooter(page.nextPageUrl);
		}
		stopRefreshAnimation();
	}

	@Override
	public void onLoaderReset(Loader<ConversationSummaryPage> arg0) {
	}
	
	private void createMoreFooter(String nextPageUrl) {
		moreFooter = new More(LayoutInflater.from(getActivity()).inflate(R.layout.more, getListView(), false));
		updateMoreFooter(nextPageUrl);
		getListView().addFooterView(moreFooter.getContainerView(), null, true);
	}
	
	private void updateMoreFooter(String nextPageUrl) {
		moreFooter.setVisible(nextPageUrl != null);
		moreFooter.setNextPageUrl(nextPageUrl);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (v == moreFooter.getContainerView()) {
			Bundle args = new Bundle();
			args.putString(ARG_URL, moreFooter.getNextPageUrl());
			getLoaderManager().restartLoader(LOADER_PAGE, args, this); 
		} else {
			ConversationSummary s = adapter.getItem(position);
			Intent i = ConversationDetailActivity.getIntent(getActivity(), s.url);
			startActivityForResult(i, REQUEST_CODE_REFRESH_ON_RETURN);
		}
	}

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.summary, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
    	startRefreshAnimation();
    	getLoaderManager().restartLoader(LOADER_INITIAL_LIST, null, this);
    }
    
    private void createPost() {
    	Intent i = new Intent(getActivity(), AuthorMessageActivity.class);
    	startActivityForResult(i, REQUEST_CODE_REFRESH_ON_RETURN);
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_REFRESH_ON_RETURN && resultCode == Activity.RESULT_OK)
			refresh();
	}
    
    
}

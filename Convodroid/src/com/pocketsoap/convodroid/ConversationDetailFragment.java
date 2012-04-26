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
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.pocketsoap.convodroid.data.*;
import com.pocketsoap.convodroid.http.ChatterRequests;
import com.pocketsoap.convodroid.loaders.JsonLoader;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.*;


/**
 * @author @superfell
 */
public class ConversationDetailFragment extends ConversationFragment implements LoaderCallbacks<ConversationDetail>, OnClickListener {
	
	static final String EXTRA_DETAIL_URL = "detail_url";
	
	private static final int LOADER_DETAILS = 0;
	private static final int LOADER_DETAILS_PAGE = 1;
	private static final int LOADER_POST_REPLY = 2;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		clearRefreshView();
		View v = inflater.inflate(R.layout.convo_list_f, container, false);
		return v;
	}

	private DetailAdapter adapter;
	private EditText replyText;
	private Button sendButton;
	private More moreHeader;
	
	@Override
	protected void initLoader() {
		getLoaderManager().initLoader(LOADER_DETAILS, getArguments(), this);
	}

	@Override
	public Loader<ConversationDetail> onCreateLoader(int id, Bundle args) {
		RestRequest req = ChatterRequests.conversationDetail(args.getString(EXTRA_DETAIL_URL));
		return new JsonLoader<ConversationDetail>(getActivity(), restClient, req, new TypeReference<ConversationDetail>() {} );
	}

	/** Adds a footer view that lets the user add a reply to the conversation */
	private void addReplyFooter(ConversationDetail details) {
		View reply = LayoutInflater.from(getActivity()).inflate(R.layout.reply, getListView(), false);
		sendButton = (Button) reply.findViewById(R.id.send_button);
		sendButton.setOnClickListener(this);
		replyText = (EditText) reply.findViewById(R.id.msg_body);
		User me = details.memberWithId(restClient.getClientInfo().userId);
		if (me != null) {
			ImageView photo = (ImageView)reply.findViewById(R.id.photo);
			imageLoader.asyncLoadImage(me.photo.smallPhotoUrl, photo);
		}
		getListView().addFooterView(reply, null, true);
	}

	private void addMoreHeader(String nextPageUrl) {
		moreHeader = new More(LayoutInflater.from(getActivity()).inflate(R.layout.more, getListView(), false));
		getListView().addHeaderView(moreHeader.getContainerView(), null, true);
		updateMoreHeader(nextPageUrl);
	}

	private void updateMoreHeader(String nextPageUrl) {
		moreHeader.setVisible(nextPageUrl != null);
		moreHeader.setNextPageUrl(nextPageUrl);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (v == moreHeader.getContainerView()) {
			startRefreshAnimation();
			Bundle args = new Bundle();
			args.putString(EXTRA_DETAIL_URL, moreHeader.getNextPageUrl());
			getLoaderManager().restartLoader(LOADER_DETAILS_PAGE, args, this);
		}
	}

	@Override
	public void onLoadFinished(Loader<ConversationDetail> loader, ConversationDetail details) {
		if (adapter == null) {
			addMoreHeader(details.messages.nextPageUrl);
			addReplyFooter(details);
			adapter = new DetailAdapter(getActivity(), imageLoader, restClient.getClientInfo().userId, details);
			setListAdapter(adapter);
			getListView().smoothScrollToPosition(getListView().getCount()-1);
			if (!details.read) markRead(details);
		} else {
			adapter.addMessages(details);
			updateMoreHeader(details.messages.nextPageUrl);
		}
		stopRefreshAnimation();
	}

	@Override
	public void onLoaderReset(Loader<ConversationDetail> loader) {
	}
	
	private void markRead(final ConversationDetail cd) {
		RestRequest req = ChatterRequests.markConversationRead(cd.conversationUrl);
		restClient.sendAsync(req, new AsyncRequestCallback() {

			@Override
			public void onSuccess(RestResponse response) {
				Log.v("Convodroid", "Marked conversation as read " + cd.conversationUrl);
				getActivity().setResult(Activity.RESULT_OK);
			}

			@Override
			public void onError(Exception exception) {
				Log.w("Convodroid", "Error marking conversation as read", exception);
			}
		});
	}

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.action_refresh:
    			refresh();
    			return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    private void refresh() {
    	startRefreshAnimation();
    	getLoaderManager().restartLoader(LOADER_DETAILS, getArguments(), this);
    }

	@Override
	public void onClick(View v) {
		if (replyText.getText().toString().trim().length() == 0) return;
		Log.i("Convodroid", "send reply " + replyText.getText());
		startRefreshAnimation();
		sendButton.setEnabled(false);
		replyText.setEnabled(false);
		String body = replyText.getText().toString();
		String inReplyTo = adapter.getItem(adapter.getCount()-1).id;
		NewMessage m = new NewMessage(body, inReplyTo);
		final RestRequest req = ChatterRequests.postMessage(m);
		getLoaderManager().restartLoader(LOADER_POST_REPLY, null, new LoaderCallbacks<Message>() {

			@Override
			public Loader<Message> onCreateLoader(int arg0, Bundle arg1) {
				return new JsonLoader<Message>(getActivity(), restClient, req, new TypeReference<Message>() {} );
			}

			@Override
			public void onLoadFinished(Loader<Message> arg0, Message newMsg) {
				if (newMsg != null)
					adapter.add(newMsg);
				sendDone();
			}

			@Override
			public void onLoaderReset(Loader<Message> arg0) {
			}
			
		});
	}
	
	private void sendDone() {
		stopRefreshAnimation();
		sendButton.setEnabled(true);
		replyText.setEnabled(true);
		replyText.setText("");
		getActivity().setResult(Activity.RESULT_OK);
	}
}

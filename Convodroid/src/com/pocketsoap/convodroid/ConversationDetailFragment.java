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

import java.io.IOException;

import org.apache.http.ParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.type.TypeReference;

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
import com.pocketsoap.convodroid.http.JsonEntity;
import com.pocketsoap.convodroid.loaders.JsonLoader;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.*;
import com.salesforce.androidsdk.rest.RestRequest.RestMethod;


/**
 * @author @superfell
 */
public class ConversationDetailFragment extends ConversationFragment implements LoaderCallbacks<ConversationDetail>, OnClickListener {
	
	static final String EXTRA_DETAIL_URL = "detail_url";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		clearRefreshView();
		View v = inflater.inflate(R.layout.convo_list_f, container, false);
		return v;
	}

	private DetailAdapter adapter;
	private EditText replyText;
	
	@Override
	protected void initLoader() {
		getLoaderManager().initLoader(0, getArguments(), this);
	}

	@Override
	public Loader<ConversationDetail> onCreateLoader(int id, Bundle args) {
		RestRequest req = new RestRequest(RestMethod.GET, args.getString(EXTRA_DETAIL_URL), null);
		return new JsonLoader<ConversationDetail>(getActivity(), restClient, req, new TypeReference<ConversationDetail>() {} );
	}

	private void addReplyFooter(ConversationDetail details) {
		View reply = LayoutInflater.from(getActivity()).inflate(R.layout.reply, getListView(), false);
		Button b = (Button) reply.findViewById(R.id.send_button);
		b.setOnClickListener(this);
		replyText = (EditText) reply.findViewById(R.id.msg_body);
		User me = details.memberWithId(restClient.getClientInfo().userId);
		if (me != null) {
			ImageView photo = (ImageView)reply.findViewById(R.id.photo);
			imageLoader.asyncLoadImage(me.photo.smallPhotoUrl, photo);
		}
		getListView().addFooterView(reply, null, true);
	}
	
	@Override
	public void onLoadFinished(Loader<ConversationDetail> loader, ConversationDetail details) {
		if (adapter == null) {
			addReplyFooter(details);
			adapter = new DetailAdapter(getActivity(), imageLoader, restClient.getClientInfo().userId, details.messages.reverseOrderMessages());
			setListAdapter(adapter);
		} else {
			adapter.addMessages(details);
		}
		stopRefreshAnimation();
	}

	@Override
	public void onLoaderReset(Loader<ConversationDetail> loader) {
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
    	getLoaderManager().restartLoader(0, getArguments(), this);
    }

	@Override
	public void onClick(View v) {
		Log.i("Convodroid", "send reply " + replyText.getText());
		startRefreshAnimation();
		String body = replyText.getText().toString();
		String inReplyTo = adapter.getItem(adapter.getCount()-1).id;
		NewMessage m = new NewMessage(body, inReplyTo);
		try {
			RestRequest req = new RestRequest(RestMethod.POST, "/services/data/v24.0/chatter/users/me/messages", new JsonEntity(m));
			restClient.sendAsync(req, new AsyncRequestCallback() {

				@Override
				public void onSuccess(RestResponse response) {
					try {
						Log.i("Convodroid", "new msg response " + response.getStatusCode());
						ObjectMapper m = new ObjectMapper();
						m.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
						Message newMsg = m.readValue(response.getHttpResponse().getEntity().getContent(), new TypeReference<Message>() {} );
						adapter.add(newMsg);
						
					} catch (ParseException e) {
						Log.e("Convodroid", "boom", e);
					} catch (IOException e) {
						Log.e("Convodroid", "boom", e);
					}
					stopRefreshAnimation();
				}

				@Override
				public void onError(Exception exception) {
					Log.i("Convodroid", "error creating post");
					stopRefreshAnimation();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

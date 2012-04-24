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
import java.util.*;

import android.app.Activity;
import android.os.Bundle;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.actionbarsherlock.app.SherlockFragment;
import com.pocketsoap.convodroid.data.*;
import com.pocketsoap.convodroid.http.*;
import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.*;
import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
import com.salesforce.androidsdk.rest.ClientManager.RestClientCallback;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest.RestMethod;

/**
 * @author @superfell
 *
 */
public class AuthorMessageFragment extends SherlockFragment implements OnClickListener, RestClientCallback, TextWatcher {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.author, container, false);
		sendButton = (Button)v.findViewById(R.id.send_button);
		sendButton.setOnClickListener(this);
		sendButton.setEnabled(false);
		recipientText = (MultiAutoCompleteTextView)v.findViewById(R.id.recipient_name);
		recipientText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		recipientText.setEnabled(false);
		recipientText.addTextChangedListener(this);
		messageText = (EditText)v.findViewById(R.id.msg_body);
		messageText.addTextChangedListener(this);
		return v;
	}

	private UserSearchAdapter userAdapter;
	private Button sendButton;
	private MultiAutoCompleteTextView recipientText;
	private EditText messageText;
	private RestClient client;
	
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
	
	@Override
	public void authenticatedRestClient(RestClient client) {
		if (userAdapter == null) {
			userAdapter = new UserSearchAdapter(getActivity(), android.R.layout.simple_list_item_1, client);
			recipientText.setAdapter(userAdapter);
			recipientText.setEnabled(true);
		}
		this.client = client;
	}

	@Override
	public void onClick(View v) {
		Log.i("Convodroid", "send " + messageText.getText() + " to " + recipientText.getText());
		Editable r = recipientText.getText();
		sendButton.setEnabled(false);
		UserSpan [] users = r.getSpans(0, r.length(), UserSpan.class);
		List<String> recipients = new ArrayList<String>(users.length);
		for (UserSpan us : users)
			recipients.add(us.user.id);
		NewMessage m = new NewMessage();
		m.recipients = recipients;
		m.body = messageText.getText().toString();
		RestRequest req = ChatterRequests.postMessage(m);
		client.sendAsync(req, new AsyncRequestCallback() {

			@Override
			public void onSuccess(RestResponse response) {
				try {
					Log.i("Convodroid", "post new response " + response.getStatusCode() + " "  + response.asString());
				} catch (IOException e) {
					Log.i("Convodroid", "could create message", e);
				}
				getActivity().setResult(Activity.RESULT_OK);
				getActivity().finish();
			}

			@Override
			public void onError(Exception e) {
				Log.i("Convodroid", "couldn't create message", e);
				updateSendButtonEnabled();
			}
		});
	}
	
	static class UserSpan {
		
		UserSpan(User u) {
			assert u != null;
			this.user = u;
		}
		
		final User user;
	}
	
	private void updateSendButtonEnabled() {
		sendButton.setEnabled(recipientText.getText().length() > 0 && messageText.getText().length() > 0);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		updateSendButtonEnabled();
	}
}

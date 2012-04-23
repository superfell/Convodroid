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

import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.text.*;
import android.text.style.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.actionbarsherlock.app.SherlockFragment;
import com.pocketsoap.convodroid.data.*;
import com.pocketsoap.convodroid.http.JsonEntity;
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

	private UserAdapter userAdapter;
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
			userAdapter = new UserAdapter(getActivity(), android.R.layout.simple_list_item_1, client);
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
		RestRequest req;
		try {
			req = new RestRequest(RestMethod.POST, "/services/data/v24.0/chatter/users/me/messages", new JsonEntity(m));
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
		} catch (IOException e) {
			Log.i("Convodroid", "couldn't create message", e);
			updateSendButtonEnabled();
		}
	}
	
	private static class UserSpan {
		
		UserSpan(User u) {
			assert u != null;
			this.user = u;
		}
		
		final User user;
	}
	
	private static class UserAdapter extends ArrayAdapter<User> {

		public UserAdapter(Context context, int textViewResourceId, RestClient client) {
			super(context, textViewResourceId);
			this.client = client;
			this.inf = LayoutInflater.from(context);
		}

		private final LayoutInflater inf;
		private final RestClient client;
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inf.inflate(android.R.layout.simple_list_item_2, parent, false);
			}
			User u = getItem(position);
			((TextView)convertView.findViewById(android.R.id.text1)).setText(u.name);
			((TextView)convertView.findViewById(android.R.id.text2)).setText(u.title);
			return convertView;
		}
		
		@Override
		public Filter getFilter() {
			return searchFilter;
		}

		private Filter searchFilter = new Filter() {

			@Override
			public CharSequence convertResultToString(Object resultValue) {
				SpannableString ss = new SpannableString(((User)resultValue).name);
				ss.setSpan(new UserSpan((User)resultValue), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				ss.setSpan(new StyleSpan(Typeface.BOLD), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				ss.setSpan(new ForegroundColorSpan(Color.BLUE), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				return ss;
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				Log.i("Convodroid", "performFiltering " + constraint);
				if (constraint == null) return null;
				try {
					String path = "/services/data/v24.0/chatter/users?q=" + Uri.encode(constraint.toString());
					Log.i("Convodroid", "GET " + path);
					RestResponse res = client.sendSync(RestMethod.GET, path, null);
					ObjectMapper m = new ObjectMapper();
					m.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					UserPage up = m.readValue(res.getHttpResponse().getEntity().getContent(), UserPage.class);
					Log.i("Convodroid", "got " + up.users.size() + " users returned for " + constraint);
					FilterResults results = new FilterResults();
					results.count = up.users.size();
					results.values = up;
					return results;
					
				} catch (IOException e) {
					Log.i("Convodroid", "user search failed ", e);
					return new FilterResults();
				}
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				clear();
				if (results != null) {
					UserPage up = (UserPage)results.values;
					if (up.users != null) {
						for (User u : ((UserPage)results.values).users)
							add(u);
					}
				}
			}
		};
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

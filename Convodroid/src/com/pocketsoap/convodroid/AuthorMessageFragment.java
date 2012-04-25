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
import android.graphics.*;
import android.os.Bundle;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.actionbarsherlock.app.SherlockFragment;
import com.pocketsoap.convodroid.data.*;
import com.pocketsoap.convodroid.http.ChatterRequests;
import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.*;
import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
import com.salesforce.androidsdk.rest.ClientManager.RestClientCallback;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;

/**
 * @author @superfell
 *
 */
public class AuthorMessageFragment extends SherlockFragment implements OnClickListener, RestClientCallback {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.author, container, false);
		sendButton = (Button)v.findViewById(R.id.send_button);
		sendButton.setOnClickListener(this);
		sendButton.setEnabled(false);
		recipientText = (AutoCompleteTextView)v.findViewById(R.id.recipient_name);
		recipientText.setEnabled(false);
		recipientText.addTextChangedListener(new CompletionTextWatcher());
		recipients = (TextView)v.findViewById(R.id.recipients);
		recipients.setMovementMethod(LinkMovementMethod.getInstance());
		messageText = (EditText)v.findViewById(R.id.msg_body);
		messageText.addTextChangedListener(new EnablingTextWatcher());
		return v;
	}

	private UserSearchAdapter userAdapter;
	private Button sendButton;
	private AutoCompleteTextView recipientText;	// current recipient being entered/selected.
	private TextView recipients;	// list of actually selected recipients.
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
			userAdapter = new UserSearchAdapter(getActivity(), client);
			recipientText.setAdapter(userAdapter);
			recipientText.setEnabled(true);
		}
		this.client = client;
	}

	@Override
	public void onClick(View v) {
		Log.i("Convodroid", "send " + messageText.getText() + " to " + recipients.getText());
		Spannable r = (Spannable)recipients.getText();
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
		sendButton.setEnabled(recipients.getText().length() > 0 && messageText.getText().length() > 0);
	}
	
	// updated the enabled state of the end button after a text change.
	private class EnablingTextWatcher implements TextWatcher {
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
	
	// this watches for the text to have a userId span in it (from the auto completion selection) and move it to the selected recipients list.
	private class CompletionTextWatcher extends EnablingTextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			UserSpan [] userSpans = s.getSpans(0, s.length(), UserSpan.class);
			if (userSpans != null && userSpans.length > 0) {
				if (recipients.length() > 0) recipients.append("   ");
				SpannableString u = new SpannableString(s.toString() + " ");
				int len = u.length();
				u.setSpan(userSpans[0], 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				u.setSpan(new StyleSpan(Typeface.BOLD), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				u.setSpan(new ForegroundColorSpan(Color.BLUE), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				u.setSpan(new ImageSpan(getActivity(), R.drawable.remove, ImageSpan.ALIGN_BOTTOM), len-1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				u.setSpan(new RemoveItemClickableSpan(userSpans[0]), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				recipients.append(u);
				s.clear();
			}
			super.afterTextChanged(s);
		}
	}
	
	private class RemoveItemClickableSpan extends ClickableSpan {

		RemoveItemClickableSpan(UserSpan user) {
			this.user = user;
		}
		
		private final UserSpan user;
		
		@Override
	    public void updateDrawState(TextPaint ds) {
			// we don't want to change the text appearance.
		}
		
		@Override
		public void onClick(View widget) {
			TextView tv = (TextView)widget;
			Spanned span = (Spanned)tv.getText();
			int start = span.getSpanStart(user);
			if (start == -1) return;
			int end = span.getSpanEnd(user);
			tv.getEditableText().delete(start,  end);
		}
	}
}

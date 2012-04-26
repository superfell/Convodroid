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
package com.pocketsoap.convodroid.http;

import java.io.*;
import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.pocketsoap.convodroid.data.NewMessage;
import com.salesforce.androidsdk.rest.*;
import com.salesforce.androidsdk.rest.RestRequest.RestMethod;

public class ChatterRequests {

	public static RestRequest conversationSummary() {
		return conversationSummaryPage("/services/data/v24.0/chatter/users/me/conversations");
	}

	public static RestRequest conversationSummaryPage(String url) {
		return new RestRequest(RestMethod.GET, url, null, HTTP_HEADERS); 
	}

	public static RestRequest conversationDetail(String detailUrl) {
		return new RestRequest(RestMethod.GET, detailUrl, null, HTTP_HEADERS);
	}
	
	public static RestRequest postMessage(NewMessage msg) {
		try {
			return new RestRequest(RestMethod.POST, "/services/data/v24.0/chatter/users/me/messages", new JsonEntity(msg), HTTP_HEADERS);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static RestRequest markConversationRead(String conversationUrl) {
		try {
			UrlEncodedFormEntity form = new UrlEncodedFormEntity(Arrays.asList(new NameValuePair [] { new BasicNameValuePair("read", "true") }), "UTF-8");
			return new RestRequest(RestMethod.POST, conversationUrl + "/mark-read", form, HTTP_HEADERS);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}	
	}
	
	public static RestRequest image(String imageUrl) {
		return new RestRequest(RestMethod.GET, imageUrl, null);
	}
	
	private static final Map<String, String> HTTP_HEADERS;
	
	static {
		Map<String, String> h = new HashMap<String, String>();
		h.put("X-Chatter-Entity-Encoding", "false");
		HTTP_HEADERS = Collections.unmodifiableMap(h);
	}
}

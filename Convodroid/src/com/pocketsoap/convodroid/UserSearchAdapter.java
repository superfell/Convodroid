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
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.*;

import android.content.Context;
import android.net.Uri;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.pocketsoap.convodroid.AuthorMessageFragment.UserSpan;
import com.pocketsoap.convodroid.data.*;
import com.salesforce.androidsdk.rest.*;
import com.salesforce.androidsdk.rest.RestRequest.RestMethod;

/**
 * This is an adapter that can be used to do user search/filtering (say for an auto complete text view).
 * 
 * @author @superfell
 */
class UserSearchAdapter extends ArrayAdapter<User> {

	UserSearchAdapter(Context context, RestClient client) {
		super(context, 0);
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

		private final ConcurrentHashMap<String, FilterResults> lookupCache = new ConcurrentHashMap<String, FilterResults>();

		@Override
		public CharSequence convertResultToString(Object resultValue) {
			SpannableString ss = new SpannableString(((User)resultValue).name);
			ss.setSpan(new UserSpan((User)resultValue), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			return ss;
		}

		private FilterResults makeResults(UserPage p) {
			FilterResults r = new FilterResults();
			r.count = p.users.size();
			r.values = p;
			return r;
		}
		
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			if (constraint == null) return null;
			String filterText = constraint.toString();
			Log.i("Convodroid", "performFiltering " + filterText);
			FilterResults cached = lookupCache.get(filterText);
			if (cached != null) return cached;
			try {
				String path = "/services/data/v24.0/chatter/users?q=" + Uri.encode(constraint.toString());
				Log.i("Convodroid", "GET " + path);
				RestResponse res = client.sendSync(RestMethod.GET, path, null);
				ObjectMapper m = new ObjectMapper();
				m.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				UserPage up = m.readValue(res.getHttpResponse().getEntity().getContent(), UserPage.class);
				Log.i("Convodroid", "got " + up.users.size() + " users returned for " + constraint);
				FilterResults results = makeResults(up);
				lookupCache.put(filterText, results);
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
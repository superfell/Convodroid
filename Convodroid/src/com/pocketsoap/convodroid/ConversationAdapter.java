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

import java.util.*;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.pocketsoap.convodroid.photos.ImageLoader;

abstract class ConversationAdapter<T> extends ArrayAdapter<T> {

	public ConversationAdapter(Context context, ImageLoader imgLoader, String myUserId, List<T> items) {
		// Note that we explicitly copy the starting data because when we call
		// clear later we don't want to clear the actual list that the caller
		// passed us.
		super(context, 0, new ArrayList<T>(items));
		this.inf = LayoutInflater.from(context);
		this.imageLoader = imgLoader;
		this.myUserId = myUserId;
	}
	
	protected final LayoutInflater inf;
	protected final ImageLoader imageLoader;
	protected final String myUserId;
	
	protected static class Holder {
		Holder(View v) {
			this.photo = (ImageView) v.findViewById(R.id.photo);
			this.timestamp = (TextView) v.findViewById(R.id.timestamp);
			this.from = (TextView) v.findViewById(R.id.from);
			this.text = (TextView) v.findViewById(R.id.msg_body);
		}
		
		protected ImageView photo;
		protected TextView timestamp, from, text;
	}
	
	protected abstract int getLayoutResourceForPosition(int position);
	
	protected Holder onNewHolder(Holder h) {
		return h;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inf.inflate(getLayoutResourceForPosition(position), parent, false);
			convertView.setTag(onNewHolder(new Holder(convertView)));
		}
		bindRow(convertView, (Holder)convertView.getTag(), getItem(position));
		return convertView;
	}
	
	protected abstract void bindRow(View view, Holder viewHolder, T item);
	
	protected void addAll(List<T> items, boolean clearExisting) {
		if (clearExisting)
			clear();
		for (T i : items)	// no addAll in API v8
			add(i);
	}

}

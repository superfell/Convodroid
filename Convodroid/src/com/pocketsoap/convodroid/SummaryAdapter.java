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

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.View;

import com.pocketsoap.convodroid.data.*;
import com.pocketsoap.convodroid.photos.ImageLoader;

/**
 * ListAdapter to show the conversation summary list.
 *  
 * @author @superfell
 **/
class SummaryAdapter extends ConversationAdapter<ConversationSummary> {

	public SummaryAdapter(Context context, ImageLoader imgLoader, String myUserId, List<ConversationSummary> items) {
		super(context, imgLoader, myUserId, items);
	}
	
	@Override
	protected int getLayoutResourceForPosition(int position) {
		return R.layout.summary_row;
	}

	@Override
	protected void bindRow(View view, Holder viewHolder, ConversationSummary item) {
		view.setBackgroundColor(item.read ? Color.WHITE : Color.argb(128, 225, 225, 255));
		if (myUserId.equals(item.latestMessage.sender.id)) {
			if (item.latestMessage.recipients.size() > 0)
				viewHolder.from.setText(item.latestMessage.recipients.get(0).name);
			else
				viewHolder.from.setText("Who the hell knows!");
		} else {
			viewHolder.from.setText(item.latestMessage.sender.name);
		}
		viewHolder.text.setText(item.latestMessage.body.text);
		CharSequence ts = DateUtils.getRelativeTimeSpanString(item.latestMessage.sentDate.getTimeInMillis());
		viewHolder.timestamp.setText(ts);
		imageLoader.asyncLoadImage(item.latestMessage.sender.photo.smallPhotoUrl, viewHolder.photo);
	}
	
	void addPage(ConversationSummaryPage page) {
		addAll(page.conversations, page.currentPageUrl.endsWith("/conversations"));
	}
}
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

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;

import com.pocketsoap.convodroid.data.*;
import com.pocketsoap.convodroid.photos.ImageLoader;

/**
 * @author @superfell
 *
 */
public class DetailAdapter extends ConversationAdapter<Message> {

	public DetailAdapter(Context context, ImageLoader imgLoader, String myUserId, ConversationDetail detail) {
		super(context, imgLoader, myUserId, detail.messages.reverseOrderMessages());
		this.detail = detail;
	}
	
	private ConversationDetail detail;
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).isSender(myUserId) ? 0 : 1;
	}

	@Override
	protected int getLayoutResourceForPosition(int position) {
		return getItem(position).isSender(myUserId) ? R.layout.row_image_right : R.layout.row_image_left;
	}

	@Override
	protected void bindRow(View view, Holder viewHolder, Message item) {
		viewHolder.from.setText(item.sender.name);
		viewHolder.text.setText(item.body.text);
		CharSequence ts = DateUtils.getRelativeTimeSpanString(item.sentDate.getTimeInMillis());
		viewHolder.timestamp.setText(ts);
		imageLoader.asyncLoadImage(item.sender.photo.smallPhotoUrl, viewHolder.photo);
	}
	
	void addMessages(ConversationDetail cd) {
		boolean isFirstPage = cd.messages.currentPageUrl.equals(detail.messages.currentPageUrl);
		if (isFirstPage) {
			// update the first/primary page.
			detail = cd;
		}
		addAll(cd.messages.reverseOrderMessages(), isFirstPage);
	}
}

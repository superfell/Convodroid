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

import android.view.View;

/**
 * Manages a more .... header or footer row.
 * 
 * @author @superfell
 */
class More {

	More(View container) {
		this.container = container;
		this.moreText = container.findViewById(R.id.more_text);
	}
	
	private final View container, moreText;
	private String nextPageUrl;
	
	void setNextPageUrl(String url) {
		nextPageUrl = url;
	}
	
	String getNextPageUrl() {
		return nextPageUrl;
	}
	
	void setVisible(boolean show) {
		moreText.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	
	void hide() {
		moreText.setVisibility(View.GONE);
	}
	
	void show() {
		moreText.setVisibility(View.VISIBLE);
	}
	
	View getContainerView() {
		return container;
	}
}

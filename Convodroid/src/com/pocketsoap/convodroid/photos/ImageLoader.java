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

package com.pocketsoap.convodroid.photos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.*;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.pocketsoap.convodroid.http.ChatterRequests;
import com.salesforce.androidsdk.rest.*;

/**
 * This class helps with async loading of images from the API and displaying
 * them, it handles fetching them in the background, caching etc.
 *  
 * @author @superfell
 **/
public class ImageLoader {

	public ImageLoader(Context ctx, RestClient client) {
		this.context = ctx;
		this.client = client;
	}
	
	private RestClient client;
	private final Context context;
	private final ConcurrentHashMap<String, Bitmap> cache = new ConcurrentHashMap<String, Bitmap>();
	
	public synchronized void setClient(RestClient c) {
		this.client = c;
	}
	
	private synchronized RestClient getClient() {
		return this.client;
	}
	
	public void flush() {
		cache.clear();
	}
	
	// if we get a multiple requests for the same url which we haven't yet cached, we only want to fetch it once.
	// so this keeps track of the inflight requests that we're still waiting on results for, and the list of imageViews
	// that are waiting on that image.
	private final ConcurrentHashMap<String, List<ImageView>> inflight = new ConcurrentHashMap<String, List<ImageView>>();
	
	/** Async fetches the imageUrl contents, and sets them in the imageView
	 *  handles checking that the imageview has not being recycled for a different
	 *  row/image in the interim.
	 *  
	 *  If the image is already available in memory, then its set synchronously.
	 *  
	 *  The tag object is set/read on the ImageView to handle row re-use, don't try to
	 *  use the ImageView Tag!
	 *  
	 * @param imageUrl
	 * @param iv
	 */
	public void asyncLoadImage(final String imageUrl, final ImageView iv) {
		Bitmap bm = cache.get(imageUrl);
		if (bm != null) {
			Log.v("Convodroid", "Using cached copy of image for " + imageUrl);
			iv.setImageBitmap(bm);
			return;
		}
		iv.setTag(imageUrl);
		List<ImageView> waiters = new ArrayList<ImageView>(4);
		waiters.add(iv);
		List<ImageView> existingWaiters = inflight.putIfAbsent(imageUrl, waiters);
		if (existingWaiters != null) {
			Log.v("Convodroid", "request already inflight, adding to waiters list for " + imageUrl);
			existingWaiters.add(iv);	// there was already a waiters list, add it to that one instead.
			return;
		}
		RestRequest req = ChatterRequests.image(imageUrl);
		Log.v("Convodroid", "starting request for GET " + req.getPath());
		ImageFetchTask task = new ImageFetchTask(getClient(), new ImageFetchTaskCallback() {

			@Override
			public void onSuccess(Bitmap bm) {
				List<ImageView> waiters = inflight.remove(imageUrl);
				if (bm != null) {
					cache.putIfAbsent(imageUrl, bm);
					if (waiters != null) {
						for (ImageView iv : waiters) {
							if (iv.getTag().equals(imageUrl)) {
								iv.setImageBitmap(bm);
							}
						}
					}
				}
			}

			@Override
			public void onError(Exception exception) {
				Log.v("Convodroid", "got error for url " + imageUrl + " : " + exception.getMessage());
				inflight.remove(imageUrl);
			}
		});
		task.execute(req);
	}
	
	private static abstract class ImageFetchTaskCallback {
		abstract void onSuccess(Bitmap bm);
		abstract void onError(Exception ex);
	}
	
	private static class ImageFetchTask extends AsyncTask<RestRequest, Void, Bitmap> {

		ImageFetchTask(RestClient client, ImageFetchTaskCallback callback) {
			this.client = client;
			this.callback = callback;
		}
		
		private final RestClient client;
		private final ImageFetchTaskCallback callback;
		private Exception exception;
		
		@Override
		protected Bitmap doInBackground(RestRequest... params) {
			try {
				RestResponse response = client.sendSync(params[0]);
				Bitmap bm = BitmapFactory.decodeStream(response.getHttpResponse().getEntity().getContent());
				return bm;
			} catch (Exception ex) {
				exception = ex;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (exception != null) {
				callback.onError(exception);
			} else {
				callback.onSuccess(result);
			}
		}
	}
}

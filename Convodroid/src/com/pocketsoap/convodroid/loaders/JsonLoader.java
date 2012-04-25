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

package com.pocketsoap.convodroid.loaders;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.type.TypeReference;

import android.content.Context;
import android.util.Log;

import com.salesforce.androidsdk.rest.*;

/**
 * Loader that will make a REST API call and return the parsed json payload.
 * 
 * @author @superfell
 */
public class JsonLoader<ReturnType> extends AsyncLoader<ReturnType> {

	public JsonLoader(Context context, RestClient restClient, RestRequest req, TypeReference<ReturnType> typeRef) {
		super(context);
		this.client = restClient;
		this.request = req;
		this.typeReference = typeRef;
	}

	private final RestClient client;
	private final RestRequest request;
	private final TypeReference<ReturnType> typeReference;
	
	@Override
	public ReturnType loadInBackground() {
		Log.v("Convodroid", "JsonLoader::loadInBackground " + request.getMethod() + " " + request.getPath());
		try {
			RestResponse res = client.sendSync(request);
			Log.v("Convodroid", "JsonLoader:: got http response " + res.getStatusCode() + " for " + request.getPath());
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			return mapper.readValue(res.getHttpResponse().getEntity().getContent(), typeReference );
			
		} catch (JsonParseException e) {
			Log.w("Convodroid", "JsonLoader error", e);
		} catch (JsonMappingException e) {
			Log.w("Convodroid", "JsonLoader error", e);
		} catch (IllegalStateException e) {
			Log.w("Convodroid", "JsonLoader error", e);
		} catch (IOException e) {
			Log.w("Convodroid", "JsonLoader error", e);
		}
		return null;
	}
}

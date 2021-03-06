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

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.*;

/**
 * @author @superfell
 *
 */
public class JsonEntity extends StringEntity {

	public JsonEntity(Object toSerialize) throws JsonGenerationException, JsonMappingException, UnsupportedEncodingException, IOException {
		super(jsonify(toSerialize));
	}
	
	private static String jsonify(Object o) throws JsonGenerationException, JsonMappingException, IOException {
		return new ObjectMapper().writeValueAsString(o);
	}
	
	private JsonEntity(String s) throws UnsupportedEncodingException {
		super(s, "UTF-8");
	}

	@Override
	public Header getContentType() {
		return new BasicHeader("Content-Type", "application/json");
	}
}

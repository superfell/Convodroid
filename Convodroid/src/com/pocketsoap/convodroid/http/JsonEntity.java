/**
 * 
 */
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

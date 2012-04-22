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
package com.pocketsoap.convodroid.data;

import java.util.List;

import org.codehaus.jackson.map.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Represents a new message that was authored by the user and should be sent to the server.
 * 
 * @author @superfell
 */
@JsonSerialize(include=Inclusion.NON_NULL)
public class NewMessage {

	public NewMessage() {
	}
	
	public NewMessage(String body, String inReplyTo) {
		this.body = body;
		this.inReplyTo = inReplyTo;
	}
	
	public String body;
	
	public String inReplyTo;
	
	public List<String> recipients;
}

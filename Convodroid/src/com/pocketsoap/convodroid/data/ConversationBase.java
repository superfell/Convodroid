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

/**
 * @author @superfell
 *
 */
public class ConversationBase {

	public List<User> members;
	public boolean read;

	/** @return the User with this id from the members collection, or null if its not in the members collection */
	public User memberWithId(String userId) {
		for (User m : members) {
			if (userId.equals(m.id)) return m;
		}
		return null;
	}
	
	/** @returns the names of the members in this conversation, excluding the specified userId (typically the current user) */
	public String memberNames(String excludingUserId) {
		StringBuilder b = new StringBuilder(members.size() * 32);
		for (User m : members) {
			if (excludingUserId != null && excludingUserId.equals(m.id)) continue;
			if (b.length() > 0) b.append(", ");
			b.append(m.name);
		}
		return b.toString();
	}
}

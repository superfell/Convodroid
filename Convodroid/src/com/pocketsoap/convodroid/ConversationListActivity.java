/**
 * 
 * 
 */
package com.pocketsoap.convodroid;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Shows the list of conversations.
 * 
 * @author superfell
 */
public class ConversationListActivity extends SherlockFragmentActivity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convo_list);
    }

}
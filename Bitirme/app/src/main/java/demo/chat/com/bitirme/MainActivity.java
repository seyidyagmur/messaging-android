package demo.chat.com.bitirme;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    ChatFragment mChatFrag = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        initFragment("");
    }

    public void initFragment(String initMsg) {
         final FragmentTransaction ft = getFragmentManager().beginTransaction();
        if( mChatFrag == null ){
             mChatFrag = new ChatFragment();
        }
        ft.add(R.id.frag_chat, mChatFrag, "chat_frag");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, " onDestroy: nothing... ");
    }


}

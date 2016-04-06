package demo.chat.com.bitirme;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import demo.chat.com.bitirme.ChatBaglantisi.BaglantiDinleyici;


public class ChatFragment extends ListFragment implements BaglantiDinleyici {
	private static final String TAG = "ChatFrag";
	
	private ArrayList<String> mMessageList = new ArrayList<String>();
    private ArrayAdapter<String> mAdapter= null;
    
    private Handler mUpdateHandler;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View contentView = inflater.inflate(R.layout.chat_frag, container, false);  // no care whatever container is.
        final EditText inputEditText = (EditText)contentView.findViewById(R.id.edit_input);
        final Button sendBtn = (Button)contentView.findViewById(R.id.btn_send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                 System.out.println("canta=1");
				String inputMsg = inputEditText.getText().toString();
				inputEditText.setText("");
				ChatApplication application = (ChatApplication) getActivity().getApplication();
		    	application.connection.sendMessage(inputMsg);
			}
        });
        
        mAdapter = new ChatMessageAdapter(getActivity(), mMessageList);
        setListAdapter(mAdapter);
        
        mUpdateHandler = new Handler() {//ilk basta sınıf olusturuldu o yuzden yasam dongusunde cagrıldıgı zaman calisir.
            @Override
            public void handleMessage(Message msg) {
                System.out.println("canta=2");
                String chatLine = msg.getData().getString("msg");
                appendChatMessage(chatLine);
            }
        };
        System.out.println("canta=4");
        ChatApplication application = (ChatApplication) getActivity().getApplication();
        application.connection.setHandler(mUpdateHandler);
        application.connection.registerListener(this);
            
        return contentView;
    }
    
    @Override 
    public void onDestroyView(){ 
    	super.onDestroyView(); 
    	
    	ChatApplication application = (ChatApplication) getActivity().getApplication();
        application.connection.setHandler(null);
        application.connection.unregisterListener(this);
     }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if( mMessageList.size() > 0){
        	getListView().smoothScrollToPosition(mMessageList.size()-1);
        }
        
        setHasOptionsMenu(true);
     }
    

    public void appendChatMessage(String row) {
        System.out.println("canta=3");
    	mMessageList.add(row);
    	getListView().smoothScrollToPosition(mMessageList.size()-1);
    	mAdapter.notifyDataSetChanged();
    	return;
    }

    final class ChatMessageAdapter extends ArrayAdapter<String> {

    	public static final int VIEW_TYPE_MYMSG = 0;
    	public static final int VIEW_TYPE_INMSG = 1;
    	public static final int VIEW_TYPE_COUNT = 2;    // gonderen benim
    	private LayoutInflater mInflater;
    	
		public ChatMessageAdapter(Context context, List<String> objects){
			super(context, 0, objects);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
		
		@Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }
		
		@Override
        public int getItemViewType(int position) {
			String item = this.getItem(position);
			if(item.startsWith("Ben: ")){
				return VIEW_TYPE_MYMSG;
			} else {
				return VIEW_TYPE_INMSG;
			}
		}
		
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
            String item = this.getItem(position);
			boolean mymsg = false;
			
			if ( getItemViewType(position) == VIEW_TYPE_MYMSG){
				if( view == null ){
	            	view = mInflater.inflate(R.layout.chat_giden_mesaj, null);
	            }
				mymsg = true;
 			} else {
				if( view == null ){
	            	view = mInflater.inflate(R.layout.chat_gelen_mesaj, null);
	            }
 			}
			
            TextView sender = (TextView)view.findViewById(R.id.sender);
            
            TextView msgRow = (TextView)view.findViewById(R.id.msg_row);
            msgRow.setText(item);
            if( mymsg ){
            	msgRow.setBackgroundResource(R.color.my_msg_background);	
            }else{
            	msgRow.setBackgroundResource(R.color.in_msg_background);
            }
            
            TextView time = (TextView)view.findViewById(R.id.time);
            
            return view;
		}
    }

	@Override
	public void onConnectionReady() {

	}

	@Override
	public void onConnectionDown() {
 		getActivity().finish();
	}
}

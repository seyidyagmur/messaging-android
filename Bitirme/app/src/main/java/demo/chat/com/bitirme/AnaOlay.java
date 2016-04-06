package demo.chat.com.bitirme;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import demo.chat.com.bitirme.ChatBaglantisi.BaglantiDinleyici;

public class AnaOlay extends Activity implements AygitOlaylar, BaglantiDinleyici {

	public static final int SERVER_PORT = 10086;
	protected static final String TAG = "HOME";
	private WifiP2pManager mManager;
	private WifiP2pManager.Channel channel;
 	private final IntentFilter intentFilter = new IntentFilter();

	private Handler mUpdateHandler;
	private ChatBaglantisi mConnection;
	
	private AygitListeFragments listFragment;
	private AygitAyrintiFragments detailFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		listFragment = (AygitListeFragments) getFragmentManager().findFragmentById(R.id.frag_list);
		detailFragment = (AygitAyrintiFragments) getFragmentManager().findFragmentById(R.id.frag_detail);
		
		mUpdateHandler = new Handler() {
            @Override
	        public void handleMessage(Message msg) {
				System.out.println("baglanti=1");

			}
		};
 	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
 	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
		channel = mManager.initialize(this, getMainLooper(), new ChannelListener() {
			
			@Override
			public void onChannelDisconnected() {
				System.out.println("baglanti=2");
			}
		});
		
		receiver = new WiFiDirectBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case android.R.id.home:
          		return true;
            case R.id.atn_direct_enable:
                     startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                return true;
            case R.id.atn_direct_discover:
                 listFragment.onInitiateDiscovery();
                 discoverPeers();
                return true;
            case R.id.disconnect:
            	mConnection.tearDown();
            	return true;
            case R.id.about:
            	Toast.makeText(this, "Iskenderun Technical University", Toast.LENGTH_LONG).show();
           	return true;
            case R.id.help:
            	Toast.makeText(this, "Wi-fi yi açmalisiniz!", Toast.LENGTH_LONG).show();
            	return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private WiFiDirectBroadcastReceiver receiver;

    @Override
    protected void onResume() {
    	super.onResume();
		System.out.println("baglanti=3");

		discoverPeers();
    }
        @Override
    protected void onPause() {
     	super.onPause();
		System.out.println("baglanti=4");
	}

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(receiver);    	
    	if(mConnection != null) {
			System.out.println("baglanti=5");
			mConnection.unregisterListener(this);
    		mConnection.tearDown();
    	}
    }
    private void discoverPeers() {
		System.out.println("baglanti=6");
		mManager.discoverPeers(channel, new ActionListener() {
            @Override
            public void onSuccess() {
            	Toast.makeText(AnaOlay.this, "Tarama Baslatildi", Toast.LENGTH_SHORT).show();
 				System.out.println("baglanti=7");
			}
            @Override
            public void onFailure(int reasonCode) {
            	listFragment.clearPeers();
            	Toast.makeText(AnaOlay.this, "Baglanti Basarisiz ,Tekrar Deneyin.. ", Toast.LENGTH_SHORT).show();
            }
    });
    }
    
    private List peers = new ArrayList();
    private PeerListListener peerListListener = new PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
			System.out.println("baglanti=8");
            peers.clear();
            peers.addAll(peerList.getDeviceList());
            listFragment.onPeersAvailable(peers);
        }
    };
    
    private ConnectionInfoListener connectionListener = new ConnectionInfoListener() {

		@Override
		public void onConnectionInfoAvailable(WifiP2pInfo info) {
			System.out.println("baglanti=9");
			if(info.groupFormed) {
				if(mConnection != null) {
					mConnection.unregisterListener(AnaOlay.this);
				}
				mConnection = new ChatBaglantisi(mUpdateHandler, AnaOlay.this);
	    	    ChatApplication app = (ChatApplication) getApplication();
	    	    app.connection = mConnection;
	    	    mConnection.registerListener(AnaOlay.this);
	    	    
	    	    if (info.groupFormed && info.isGroupOwner) {
		      					//Gelen Baglantilar
		        } else if (info.groupFormed) {
		      		//Grup sahibi
		        	mConnection.connectToServer(info.groupOwnerAddress, SERVER_PORT);
		        }
			}
		}
	};
    
    public void connect() {
       if(peers.size() == 0) {
    		return;
    	}
    	
        WifiP2pDevice device = (WifiP2pDevice) peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(AnaOlay.this, "Baglanti basarisiz! Tekrar Deneyin!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("baglanti=10");

			String action = intent.getAction();
	        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
	            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
	            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

	            }
	        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

	            if (mManager != null) {
	                mManager.requestPeers(channel, peerListListener);
	            }

	        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

	        	if (mManager == null) {
	                return;
	            }

	            NetworkInfo networkInfo = (NetworkInfo) intent
	                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

	            if (networkInfo.isConnected()) {
	                mManager.requestConnectionInfo(channel, connectionListener);
	            }

	        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
	            listFragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
	                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
	        }
		}
    }


	@Override
	public void showDetails(WifiP2pDevice device) {
		System.out.println("baglanti=11");

		detailFragment.showDetails(device);
	}

	@Override
	public void cancelDisconnect() {
		System.out.println("baglanti=12");

	}

	@Override
	public void connect(WifiP2pConfig config) {
		System.out.println("baglanti=13");

		mManager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(AnaOlay.this, "Baglanti Basarisiz! Tekrar Deneyin!",
                        Toast.LENGTH_SHORT).show();
            }
        });
	}

	@Override
	public void disconnect() {
 		System.out.println("baglanti=14");

		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				detailFragment.resetViews();
			}
		});
		
		mConnection.tearDown();
		mManager.removeGroup(channel, new ActionListener() {
			
			@Override
			public void onSuccess() {

			}
			
			@Override
			public void onFailure(int paramInt) {

				detailFragment.getView().setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void onConnectionReady() {
		System.out.println("baglanti=15");

		Intent intent = new Intent(this, MainActivity.class);
    	this.startActivity(intent);
	}



	@Override
	public void onConnectionDown() {
		System.out.println("baglanti=16");
		disconnect();
	}
}



package demo.chat.com.bitirme;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class AygitListeFragments extends ListFragment {

	private static final String TAG = "PTP_ListFrag";
	
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    ProgressDialog progressDialog = null;
    View mContentView = null;
    private WifiP2pDevice device;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
         this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.aygitlar, peers));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.aygit_listesi, null);
        return mContentView;
    }


    public WifiP2pDevice getDevice() {
        return device;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        ((AygitOlaylar) getActivity()).showDetails(device);
    }


    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items;

        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.aygitlar, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(Util.getDeviceStatus(device.status));
                }
             }
            return v;
        }
    }

    public void updateThisDevice(WifiP2pDevice device) {
    	TextView nameview = (TextView) mContentView.findViewById(R.id.my_name);
    	TextView statusview = (TextView) mContentView.findViewById(R.id.my_status);
    	
    	if ( device != null) {
 	    	this.device = device;
	        nameview.setText(device.deviceName);
	        statusview.setText(Util.getDeviceStatus(device.status));
    	} else if (this.device != null ){
    		nameview.setText(this.device.deviceName);
	        statusview.setText("Wifi Directi tekrar acmayin deneyin");
    	}
    }
    public void onPeersAvailable(List<WifiP2pDevice> peerList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList);
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
             return;
        }
    }

    public void clearPeers() {
    	getActivity().runOnUiThread(new Runnable() {
    		@Override public void run() {
    			if (progressDialog != null && progressDialog.isShowing()) {
    	            progressDialog.dismiss();
    	        }
    	        peers.clear();
    	        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    	        Toast.makeText(getActivity(), "Baglanti Koptu! tekrar deneyin!", Toast.LENGTH_LONG).show();
    		}
    	});
    	
    }
    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "Cikmak icin Bosluga tiklayin!", "Esler Araniyor!", true,
                true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
        });
    }

}

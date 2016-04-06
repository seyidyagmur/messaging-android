

package demo.chat.com.bitirme;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ChatBaglantisi {

	public interface BaglantiDinleyici{
		public void onConnectionReady();
		public void onConnectionDown();
	}

    private Handler mUpdateHandler;
    private ChatServer mChatServer;
    private ChatClient mChatClient;
    private Context mContext;
    private List<BaglantiDinleyici> mListeners = new ArrayList<BaglantiDinleyici>();

    private static final String TAG = "ChatConnection";

    private Socket mSocket;
    private int mPort = -1;
    private boolean isReady = false;

    public ChatBaglantisi(Handler handler, Context context) {
        mUpdateHandler = handler;
        mChatServer = new ChatServer(handler);
        mContext = context;
    }
    
    public void tearDown() {
        mChatServer.tearDown();
        mChatClient.tearDown();
        isReady = false;
        mContext = null;
    }
    
    public void registerListener(BaglantiDinleyici listener) {
    	if(!mListeners.contains(listener)) {
    		mListeners.add(listener);
    	}
    }
    
    public void unregisterListener(BaglantiDinleyici listener) {
    	if(mListeners.contains(listener)) {
    		mListeners.remove(listener);
    	}
    }
    
    private void onConnectionReady() {
    	for(BaglantiDinleyici listener: mListeners) {
            System.out.println("baglanti=17");

            listener.onConnectionReady();
    	}
    }
     private void onConnectionDown() {
    	for(BaglantiDinleyici listener: mListeners) {
            System.out.println("baglanti=18");

            listener.onConnectionDown();
    	}
    }
    
    public void setHandler(Handler handler) {
    	mUpdateHandler = handler;
    }

    public void connectToServer(InetAddress address, int port) {
        mChatClient = new ChatClient(address, port);
    }

    public void sendMessage(String msg) {
        if (mChatClient != null) {
            mChatClient.sendMessage(msg);
        }
    }
    
    public int getLocalPort() {
        return mPort;
    }
    
    public void setLocalPort(int port) {
        mPort = port;
    }
    
    public boolean isReady() {
    	return isReady;
    }

    public synchronized void updateMessages(String msg, boolean local) {

        if (local) {
            msg = "Ben: " + msg;
        } else {
            msg = "O: " + msg;
        }

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        if(mUpdateHandler != null) {
        	mUpdateHandler.sendMessage(message);
        }

    }

    private synchronized void setSocket(Socket socket) {
         if (socket == null) {
         }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                     e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }

    private class ChatServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;

        public ChatServer(Handler handler) {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }

        public void tearDown() {
            mThread.interrupt();
            try {
            	if(mServerSocket != null) {
            		mServerSocket.close();
            	}
            } catch (IOException ioe) {
             }
        }

        class ServerThread implements Runnable {

            @Override
            public void run() {

                try {
                    mServerSocket = new ServerSocket(AnaOlay.SERVER_PORT);
                    setLocalPort(mServerSocket.getLocalPort());
                    
                    while (!Thread.currentThread().isInterrupted()) {
                         setSocket(mServerSocket.accept());
                         if (mChatClient == null) {
                            int port = mSocket.getPort();
                            InetAddress address = mSocket.getInetAddress();
                            connectToServer(address, port);
                        }
                    }
                } catch (IOException e) {
                     e.printStackTrace();
                }
            }
        }
    }

    private class ChatClient {

        private InetAddress mAddress;
        private int PORT;

        private final String CLIENT_TAG = "ChatClient";

        private Thread mSendThread;
        private Thread mRecThread;

        public ChatClient(InetAddress address, int port) {

            this.mAddress = address;
            this.PORT = port;

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        class SendingThread implements Runnable {

            BlockingQueue<String> mMessageQueue;
            private int QUEUE_CAPACITY = 10;

            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            }

            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress, PORT));
                    } else {
                    }

                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();
                    System.out.println("baglanti=19");
                    onConnectionReady();//buradaki onConnectionReady metodunu cagiriyor. cagrılan metodda homeactivity sınıfındaki
                    //implement edilen onConnectionReady metodunu cagırıyor
                    System.out.println("baglanti=20");
                    isReady = true;
                } catch (UnknownHostException e) {
                } catch (IOException e) {
                }

                while (true) {
                    try {
                        String msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }

        class ReceivingThread implements Runnable {

            @Override
            public void run() {

                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            mSocket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {

                        String messageStr = null;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            updateMessages(messageStr, false);
                        } else {
                            break;
                        }
                    }
                    input.close();

                    onConnectionDown();
                } catch (IOException e) {
                }
            }
        }

        public void tearDown() {
            try {
            	Socket socket = getSocket();
                if (socket != null) {
                	socket.close();
                }
            } catch (IOException ioe) {
            }
        }

        public void sendMessage(String msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                } else if (socket.getOutputStream() == null) {
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                out.println(msg);
                out.flush();
                updateMessages(msg, true);
            } catch (UnknownHostException e) {
            } catch (IOException e) {
            } catch (Exception e) {
            }
        }
    }
}

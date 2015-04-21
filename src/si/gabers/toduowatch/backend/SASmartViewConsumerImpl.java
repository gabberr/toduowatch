/*    
 * Copyright (c) 2014 Samsung Electronics Co., Ltd.   
 * All rights reserved.   
 *   
 * Redistribution and use in source and binary forms, with or without   
 * modification, are permitted provided that the following conditions are   
 * met:   
 *   
 *     * Redistributions of source code must retain the above copyright   
 *        notice, this list of conditions and the following disclaimer.  
 *     * Redistributions in binary form must reproduce the above  
 *       copyright notice, this list of conditions and the following disclaimer  
 *       in the documentation and/or other materials provided with the  
 *       distribution.  
 *     * Neither the name of Samsung Electronics Co., Ltd. nor the names of its  
 *       contributors may be used to endorse or promote products derived from  
 *       this software without specific prior written permission.  
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS  
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT  
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR  
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT  
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,  
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT  
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,  
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY  
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT  
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE  
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package si.gabers.toduowatch.backend;

import java.io.IOException;
import java.util.ArrayList;

import si.gabers.toduodata.model.InterfaceAdapter;
import si.gabers.toduodata.model.ItemIF;
import si.gabers.toduodata.model.ItemRootElement;
import android.content.Intent;
import android.content.pm.Signature;
import android.os.Binder;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

/**
 * @author s.amit
 * 
 */
public class SASmartViewConsumerImpl extends SAAgent {

	public static final String TAG = "SmartViewConsumerService";

	RootItemDataReceiver mRootItemDataReceiverRegistered;

	public static final int RESULT_FAILURE = 0; // these enums have to freezed
	public static final int RESULT_SUCCESS = 1;
	public static final int REASON_OK = 0;
	public static final int REASON_BITMAP_ENCODING_FAILURE = 1;
	public static final int REASON_IMAGE_ID_INVALID = 2;
	public static final int GALLERY_CHANNEL_ID = 105; // XML file provided the
														// info
	// public static final int SERVICE_CONNECTION_RESULT_OK = 0;

	public static final String ACTION_ADD_DEVICE = "android.appcessory.device.ADD_DEVICE";
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_INT_VALUE = 3;
	public static final int MSG_SET_STRING_VALUE_CHANNEL1 = 4;
	public static final int MSG_SET_STRING_VALUE_CHANNEL2 = 5;
	public static final int MSG_CONNECTION_CLOSE = 8;
	public static final int MSG_SHOW_PROGRESS = 9;
	public static final int MSG_START_SMARTVIEW = 10;
	public static final int MSG_STOP_SMARTVIEW = 11;
	public static final int MSG_SHOW_NOTIFICATION = 12;

	public static final String APPCESSORY_EXTRA_PEER_AGENT = "com.appcessory.peer.agent";
	public static final String APPCESSORY_ACTION_PEER_AGENT = "com.appcessory.action.peeragent";

	private SASocket mConnectionHandler;
	private SA mAccessory;

	/**
	 * @author s.amit
	 * 
	 */
	public SASmartViewConsumerImpl() {

		super(TAG, SAGalleryConsumerConnection.class);
	}

	/**
	 * @author s.amit
	 * 
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "OnCreate");

		mAccessory = new SA();
		try {
			mAccessory.initialize(this);
			boolean isFeatureEnabled = mAccessory
					.isFeatureEnabled(SA.DEVICE_ACCESSORY);
		} catch (SsdkUnsupportedException e) {
			if (processUnsupportedException(e) == true) {
				return;
			}
		} catch (Exception e1) {
			Log.e(TAG, "Cannot initialize SAccessory package.");
			e1.printStackTrace();
			/*
			 * Your application can not use Samsung Accessory SDK. You
			 * application should work smoothly without using this SDK, or you
			 * may want to notify user and close your app gracefully (release
			 * resources, stop Service threads, close UI thread, etc.)
			 */

			stopSelf();
		}

	}

	private boolean processUnsupportedException(SsdkUnsupportedException e) {
		e.printStackTrace();
		int errType = e.getType();
		if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED
				|| errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
			Log.e(TAG, "This device does not support SAccessory.");
			/*
			 * Your application can not use Samsung Accessory SDK. You
			 * application should work smoothly without using this SDK, or you
			 * may want to notify user and close your app gracefully (release
			 * resources, stop Service threads, close UI thread, etc.)
			 */
			stopSelf();
		} else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
			Log.e(TAG, "You need to install SAccessory package"
					+ " to use this application.");
		} else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
			Log.e(TAG, "You need to update SAccessory package"
					+ " to use this application.");
		} else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
			Log.e(TAG,
					"We recommend that you update your"
							+ " Samsung Accessory software before using this application.");
			return false;
		}
		return true;
	}

	/**
	 * @author s.amit
	 * 
	 */
	@Override
	public void onLowMemory() {
		Log.e(TAG, "onLowMemory  has been hit better to do  graceful  exit now");
		// Toast.makeText(getBaseContext(), "!!!onLowMemory!!!",
		// Toast.LENGTH_LONG)
		// .show();
		closeConnection();
		super.onLowMemory();
	}

	/**
	 * 
	 * @param uPeerAgent
	 * @param result
	 */
	@Override
	protected void onFindPeerAgentResponse(SAPeerAgent uRemoteAgent, int result) {

		Log.i(TAG, "onFindPeerAgentResponse: Enter");
		if (result == PEER_AGENT_FOUND) {
			if (mRootItemDataReceiverRegistered != null) {
				Log.i(TAG,
						"onFindPeerAgentResponse: Received now trying to send same to  activity");
				mRootItemDataReceiverRegistered.onPeerFound(uRemoteAgent);
			} else {
				Log.e(TAG, "NO acitivity registered with service  yet ");
			}
		} else {
			Log.e(TAG, "Peer agents are not found ");
		}

	}

	/**
	 * @author s.amit
	 * 
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service Stopped.");

	}

	/**
	 * 
	 * @param error
	 * @param errorCode
	 */
	@Override
	protected void onError(String error, int errorCode) {
		// TODO Auto-generated method stub
		Log.e(TAG, "ERROR: " + errorCode + ": " + error);
	}

	/**
	 * 
	 * @param uThisConnection
	 * @param result
	 */
	@Override
	protected void onServiceConnectionResponse(SASocket uThisConnection,
			int iConnResult) {
		Log.d(TAG, "onServiceConnectionResponse : Enter");
		if (iConnResult == CONNECTION_SUCCESS) {// SERVICE_CONNECTION_RESULT_OK
			Log.d(TAG, "onServiceConnectionResponse : Sucess");
			this.mConnectionHandler = uThisConnection;
			// String toastString = R.string.ConnectionEstablishedMsg
			// + uThisConnection.getRemotePeerId();
			Toast.makeText(getBaseContext(), "Connection established",
					Toast.LENGTH_LONG).show();
		} else {
			Log.e(TAG, "Service connection establishment failed");
		}
	}

	/**
	 * 
	 * @param channelId
	 * @param data
	 */
	private void onDataAvailableonChannel(long channelId, String data) {

		// Log.i(TAG, "incoming data on channel = " + channelId +
		// ": from peer ="
		// + connectedPeerId);
		// if (data.contains(Model.THUMBNAIL_LIST_RESP)) {
		//
		// Log.i(TAG, "get_thumbnails_response size " + data.length());
		// handleThumbnails(data);
		// } else if (data.contains(Model.DOWNSCALE_IMG_RESP)) {
		//
		// Log.i(TAG,
		// "downscaled  image  response arrive!! size = "
		// + data.length());
		// handleDownscaledImage(data);
		// } else {
		// Log.e(TAG, "onDataAvailableonChannel: Unknown jSon PDU received");
		// }
		// ArrayList<MainItemListModel> items = (ArrayList<MainItemListModel>)
		// data;
		// Gson gson = new Gson();

		// handleMainItemListData(data);
		Gson gson = new GsonBuilder().registerTypeAdapter(ItemIF.class,
				new InterfaceAdapter<ItemIF>()).create();
		ItemRootElement irt = new ItemRootElement();
		irt = gson.fromJson(data, ItemRootElement.class);

		mRootItemDataReceiverRegistered.onItemListReceived(irt);

		// String s = (String) data;
		// Log.i("sddssdssd",""+items.toString());

	}

	private void handleMainItemListData(String data) {
		// mImageListReceiverRegistered.onItemListReceived(items);

	}

	/**
	 * 
	 * @author amit.s5
	 * 
	 */
	public interface RootItemDataReceiver {
		// void onThumbnailsReceived(List<ImageStructure> uList);

		// void onImageReceived(ImageStructure image);

		void onItemListReceived(ItemRootElement root);

		void onPeerFound(SAPeerAgent uRemoteAgent);

		void onServiceConnectionLost(int errorcode);
	};

	/**
	 * 
	 * @author amit.s5
	 * 
	 */
	public boolean registerImageReciever(
			RootItemDataReceiver uRootItemDataReceiver) {

		mRootItemDataReceiverRegistered = uRootItemDataReceiver;
		return true;
	}

	/**
     * 
     *
     */
	public boolean closeConnection() {

		if (mConnectionHandler != null) {
			mConnectionHandler.close();

			mConnectionHandler = null;
		} else {
			Log.e(TAG, "closeConnection: Called when no connection");
		}
		return true;
	}

	/**
	 * 
	 * @param peerAgent
	 * @return boolean
	 */
	public boolean establishConnection(SAPeerAgent peerAgent) {

		if (peerAgent != null) {
			Log.i(TAG, "Making peer connection");
			requestServiceConnection(peerAgent);
			return true;
		}
		return false;

	}

	/**
	 * 
	 * @author amit.s5
	 * 
	 */
	public void findPeers() {
		Log.d(TAG, "UI  trigger to   find avialable  peers");
		findPeerAgents();
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * 
	 * @author amit.s5
	 * 
	 */
	public class LocalBinder extends Binder {
		public SASmartViewConsumerImpl getService() {
			return SASmartViewConsumerImpl.this;
		}
	}

	/**
	 * 
	 * @author amit.s5
	 * 
	 */
	@Override
	public IBinder onBind(Intent intent) {

		return mBinder;
	}

	// service connection helper inner class

	/**
	 * 
	 * @author amit.s5
	 * 
	 */
	public class SAGalleryConsumerConnection extends SASocket {

		public static final String TAG = "SAGalleryConsumerConnection";

		public SAGalleryConsumerConnection() {
			super(SAGalleryConsumerConnection.class.getName());
		}

		/**
		 * 
		 * @param channelId
		 * @param data
		 */
		@Override
		public void onReceive(int channelId, byte[] data) {
			Log.i(TAG, "onReceive ENTER channel = " + channelId);
			final String strToUpdateUI = new String(data);
			onDataAvailableonChannel(channelId, // getRemotePeerId()
					strToUpdateUI);

		}

		// @Override
		// public void onSpaceAvailable(int channelId) {
		// Log.v(TAG, "onSpaceAvailable: "+ channelId);
		// }

		/**
		 * 
		 * @param channelId
		 * @param errorString
		 * @param error
		 */
		@Override
		public void onError(int channelId, String errorString, int error) {
			Log.e(TAG, "Connection is not alive ERROR: " + errorString + "  "
					+ error);
		}

		/**
		 * 
		 * @param errorCode
		 */
		@Override
		public void onServiceConnectionLost(int errorCode) {
			mConnectionHandler = null;
			Log.e(TAG, "onServiceConectionLost  for peer with error code ="
					+ errorCode);
			mRootItemDataReceiverRegistered.onServiceConnectionLost(errorCode);

		}

	}

	/**
	 * 
	 * @param connectedPeerId
	 */
	public void sendListMsg(String data) {

		// Log.d(TAG, "sendTbListMsg : Enter");
		// final TBListRespMsg uRMessage = new TBListRespMsg(mResult, mReason,
		// mTb.size(), mTb);
		String uJsonStringToSend = data;
		// try {
		// uJsonStringToSend = uRMessage.toJSON().toString();
		// } catch (final JSONException e) {
		//
		// Log.e(TAG, "sendThumbnails() Cannot convert json to string");
		// e.printStackTrace();
		// }
		Log.d(TAG, "tb rsp msg size = " + uJsonStringToSend.length());
		if (mConnectionHandler != null) {

			try {

				mConnectionHandler.send(GALLERY_CHANNEL_ID, data.getBytes());
			} catch (final IOException e) {
				Log.e(TAG, "I/O Error occured while send");
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void onPeerAgentUpdated(SAPeerAgent peerAgent, int result) {
		if (result == PEER_AGENT_AVAILABLE) {
			Log.i(TAG, "onPeerUpdated: PeerFound");
			authenticatePeerAgent(peerAgent);
		} else {
			Log.i(TAG, "onPeerUpdated: with result code:" + result);
		}
	}

	@Override
	protected void onAuthenticationResponse(SAPeerAgent peerAgent,
			Signature[] signatures, int code) {
		if (code == 0) {
			requestServiceConnection(peerAgent);
		}
	}
}

package si.gabers.toduowatch;

import java.util.ArrayList;
import java.util.List;

import si.gabers.toduodata.model.InterfaceAdapter;
import si.gabers.toduodata.model.ItemIF;
import si.gabers.toduodata.model.ItemRootElement;
import si.gabers.toduodata.model.MainActivityItemList;
import si.gabers.toduodata.model.TextItem;
import si.gabers.toduowatch.backend.SASmartViewConsumerImpl;
import si.gabers.toduowatch.backend.SASmartViewConsumerImpl.LocalBinder;
import si.gabers.toduowatch.backend.SASmartViewConsumerImpl.RootItemDataReceiver;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import at.fhooe.automate.logger.android.services.workflow.Workflow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

public class MainActivity extends FragmentActivity implements
		RootItemDataReceiver, ActionBar.TabListener {

	MainListsPagerAdapter mMainListPagerAdapter;
	// public static ArrayList<MainItemListModel> items;
	public static ItemRootElement root;
	ViewPager mViewPager;
	GestureDetector gestureDetector;

	public static final String TAG = "SmartViewConsumerActivity";

	public static final int MSG_MAINITEMIST_RECEIVED = 1986;
	public static final int MSG_IMAGE_RECEIVED = 1987;

	SASmartViewConsumerImpl mBackendService = null;
	List<String> mDTBListReceived = null;
	String mImage = null;
	String mDownscaledImage = "";
	boolean mIsBound = false;
	boolean mReConnect = false;
	// SAPeerAgent mPeerAgent;
	public static final int MSG_INITIATE_CONNECTION = 6;
	// private static Object mListLock = new Object();

	private final ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mBackendService = binder.getService();
			mBackendService.registerImageReciever(MainActivity.this);
			mIsBound = true;
			mBackendService.findPeers();
			Log.i(TAG,
					"ToDuoWatch Service attached to "
							+ className.getClassName());
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
			Log.e(TAG, "ToDuo Service Disconnected");
			mBackendService = null;
			mIsBound = false;
		}
	};

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		root = new ItemRootElement();

		root.items = new ArrayList<MainActivityItemList>();

		MainActivityItemList list1 = new MainActivityItemList("List 1");
		list1.addItem(new TextItem("Andrej", "sda"));
		list1.addItem(new TextItem("Miha", "sds"));

		MainActivityItemList list2 = new MainActivityItemList("List 2");
		list2.addItem(new TextItem("Ana", "saa"));
		list2.addItem(new TextItem("Tina", "sdsds"));

		root.items.add(list1);
		root.items.add(list2);

		// Create the adapter that will return a fragment for each of the three
		// primary sections
		// of the app.
		mMainListPagerAdapter = new MainListsPagerAdapter(
				getSupportFragmentManager());

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

		// Specify that the Home/Up button should not be enabled, since there is
		// no hierarchical
		// parent.
		actionBar.setHomeButtonEnabled(false);

		// Specify that we will be displaying tabs in the action bar.
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the
		// user swipes between sections.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mMainListPagerAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between different app sections, select
						// the corresponding tab.
						// We can also use ActionBar.Tab#select() to do this if
						// we have a reference to the
						// Tab.
						// actionBar.setSelectedNavigationItem(position);
					}
				});

		mViewPager.setCurrentItem(0);

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mMainListPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter.
			// Also specify this Activity object, which implements the
			// TabListener interface, as the
			// listener for when this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mMainListPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		doBindServiceToConsumerService();

		// if (savedInstanceState == null) {
		// getFragmentManager().beginTransaction()
		// .add(R.id.container, new PlaceholderFragment()).commit();
		// }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {

			syncToPhone();
			// this.finish();
			// System.exit(0);

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy");
		closeConnection();
		doUnbindService();

		super.onDestroy();

	}

	void doBindServiceToConsumerService() {
		Log.i(TAG, "doBindServiceToConsumerService");
		Context c = this;
		mIsBound = bindService(new Intent(c, SASmartViewConsumerImpl.class),
				mConnection, Context.BIND_AUTO_CREATE);

	}

	/**
	 * 
	 * @author amit.s5
	 * 
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d(TAG, "On resume started :" + mReConnect);
		Workflow.prepareAndCommitState("MainActivity", "MainActivity");

		if (mReConnect == true && mBackendService != null) {
			mBackendService.findPeers();
		}
		super.onResume();
	}

	/**
	 * 
	 * @author amit.s5
	 * 
	 */
	void doUnbindService() {
		if (mIsBound == true) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	/**
	 * 
	 * @author amit.s5
	 * 
	 */
	void closeConnection() {
		if (mIsBound == true) {
			mBackendService.closeConnection();

		}
	}

	/**
	 * 
	 * @param uRemoteAgent
	 */
	@Override
	public void onPeerFound(SAPeerAgent uRemoteAgent) {

		Log.d(TAG, "onPeerFound enter");
		if (uRemoteAgent != null) {
			if (mIsBound = true) {
				Log.d(TAG, "peer  agent is found  and  try to  connect");
				mBackendService.establishConnection(uRemoteAgent);
			} else
				Log.d(TAG, "Service not bound !!!");
		} else {
			Log.d(TAG, "no peers  are  present  tell the UI");
			Toast.makeText(getApplicationContext(), "no peers found",
					Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * 
	 * @param errorcode
	 */
	@Override
	public void onServiceConnectionLost(int errorcode) {
		Log.d(TAG, "onServiceConnectionLost  enter with error value "
				+ errorcode);
		if (errorcode == SASocket.CONNECTION_LOST_DEVICE_DETACHED)
			mReConnect = true;
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if (msg.what == MSG_MAINITEMIST_RECEIVED) {
				Log.i(TAG, "Received Thumbnails now updating UX");
				mMainListPagerAdapter.notifyDataSetChanged();
				// clearThumbnails();
				// handleReceivedThumbnails();
			} else if (msg.what == MSG_IMAGE_RECEIVED) {
				Log.i(TAG, "Received downscaled image now updating UX");

				// handleRecievedImage();

			}
		}
	};

	@Override
	public void onItemListReceived(ItemRootElement _root) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onMainItemListReceived Enter");
		root = _root;
		Message msg = Message.obtain();
		msg.what = MSG_MAINITEMIST_RECEIVED;
		mHandler.sendMessage(msg);

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		mViewPager.setCurrentItem(tab.getPosition());

	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	public class MainListsPagerAdapter extends FragmentPagerAdapter {

		public MainListsPagerAdapter(FragmentManager fm) {
			super(fm);

		}

		@Override
		public Fragment getItem(int i) {

			Bundle args = new Bundle();

			MainListFragment fragment = new MainListFragment();
			fragment.setRetainInstance(true);
			args.putInt("i", i);
			fragment.setArguments(args);
			return fragment;

		}

		@Override
		public int getCount() {
			return root.items.size();
		}

		@Override
		public int getItemPosition(Object object) {
			int index = root.items.indexOf(object);
			if (index == -1)
				return POSITION_NONE;
			else
				return index;
		}
	}

	public static class MainListFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);

			Bundle args = getArguments();
			final int i = (int) args.get("i");

			TextView tv = (TextView) rootView.findViewById(R.id.textView1);
			tv.setText(root.items.get(i).toString());
			rootView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ItemList.listId = i;
					startActivity(new Intent(getActivity(), ItemList.class));

				}
			});

			return rootView;
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onStop");
		closeConnection();
		doUnbindService();
		super.onStop();
	}

	public void syncToPhone() {

		Gson gson = new GsonBuilder()
				.registerTypeAdapter(ItemIF.class,
						new InterfaceAdapter<ItemIF>())
				.excludeFieldsWithoutExposeAnnotation().create();

		String json = gson.toJson(root).toString();
		mBackendService.sendListMsg(json);

	}

}

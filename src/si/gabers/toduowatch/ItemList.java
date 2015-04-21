package si.gabers.toduowatch;

import java.util.ArrayList;
import java.util.List;

import si.gabers.toduodata.model.ItemIF;
import si.gabers.toduodata.model.ItemRootElement;
import si.gabers.toduowatch.backend.SASmartViewConsumerImpl;
import si.gabers.toduowatch.backend.SASmartViewConsumerImpl.RootItemDataReceiver;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.accessory.SAPeerAgent;

public class ItemList extends FragmentActivity implements RootItemDataReceiver,
		ActionBar.TabListener {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	MainListPagerAdapter mMainListPagerAdapter;

	ViewPager mViewPager;

	public static final String TAG = "SmartViewConsumerActivity";

	// public static final int MSG_THUMBNAIL_RECEIVED = 1986;
	// public static final int MSG_IMAGE_RECEIVED = 1987;

	SASmartViewConsumerImpl mBackendService = null;
	List<String> mDTBListReceived = null;
	// String mImage = null;
	// String mDownscaledImage = "";
	boolean mIsBound = false;
	// boolean mReConnect = false;
	// SAPeerAgent mPeerAgent;
	public static final int MSG_INITIATE_CONNECTION = 6;
	private static Object mListLock = new Object();

	public static int listId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getActionBar().setTitle(MainActivity.root.items.get(listId).toString());

		// Create the adapter that will return a fragment for each of the three
		// primary sections
		// of the app.
		mMainListPagerAdapter = new MainListPagerAdapter(
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

	}

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
			Toast.makeText(getApplicationContext(), "No peers found",
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onServiceConnectionLost(int errorcode) {
		// TODO Auto-generated method stub

	}

	public class MainListPagerAdapter extends FragmentPagerAdapter {

		public MainListPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {

			Bundle args = new Bundle();
			ItemListFragment fragment = new ItemListFragment();
			fragment.setRetainInstance(true);
			args.putInt("i", i);
			fragment.setArguments(args);
			return fragment;

		}

		@Override
		public int getCount() {
			return MainActivity.root.items.get(listId).getItemList().size();
		}

		@Override
		public int getItemPosition(Object object) {
			int index = MainActivity.root.items.get(listId).getItemList()
					.indexOf(object);
			if (index == -1)
				return POSITION_NONE;
			else
				return index;
		}

	}

	public static class ItemListFragment extends Fragment implements
			OnCheckedChangeListener {

		int greenLight = Color.parseColor("#c5e26d");
		int redLight = Color.parseColor("#ff9494");

		View mRootView;
		CheckBox mCheckbox;
		TextView mCheckedStatus;
		ImageView mImageView;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			mRootView = inflater.inflate(R.layout.textitem_layout, container,
					false);
			Bundle args = getArguments();
			int i = (int) args.get("i");

			mCheckbox = (CheckBox) mRootView.findViewById(R.id.checkBox1);
			mCheckedStatus = (TextView) mRootView
					.findViewById(R.id.textViewCounter);

			// if (MainActivity.root.items.get(listId).getItemList().get(i)
			// .isImageItem()) {
			// mRootView = inflater.inflate(R.layout.imageitem_layout,
			// container, false);
			// mImageView = (ImageView) mRootView
			// .findViewById(R.id.imageView1);
			//
			// ImageItem imgitm = (ImageItem) MainActivity.root.items
			// .get(listId).getItemList().get(i);
			// imgitm.decodeBitmap();
			// mImageView.setImageBitmap(imgitm.getImage());
			// mImageView.setOnClickListener(new View.OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// mCheckbox.setChecked(!mCheckbox.isChecked());
			//
			// }
			// });
			//
			// mCheckbox = (CheckBox) mRootView.findViewById(R.id.checkBox1);
			// mCheckedStatus = (TextView) mRootView
			// .findViewById(R.id.textViewCounter);
			// }

			mCheckbox.setTag(i);

			boolean isTicked = MainActivity.root.items.get(listId)
					.getItemList().get(i).isTicked();
			mCheckbox.setChecked(isTicked);
			mCheckbox.setOnCheckedChangeListener(this);

			mCheckbox.setText(MainActivity.root.items.get(listId).getItemList()
					.get(i).toString());
			updateCheckedStatus();
			if (isTicked)
				mRootView.setBackgroundColor(greenLight);
			else
				mRootView.setBackgroundColor(redLight);
			return mRootView;
		}

		@Override
		public void onCheckedChanged(CompoundButton chbox, boolean arg1) {
			int id = (int) chbox.getTag();
			MainActivity.root.items.get(listId).getItemList().get(id)
					.setTicked(arg1);

			setBackground(arg1);
			updateCheckedStatus();

		}

		public void setBackground(boolean checked) {

			if (checked) {
				mRootView.setBackgroundColor(greenLight);

			} else {
				mRootView.setBackgroundColor(redLight);

			}
		}

		/**
		 * Counter for displaying how many items are checked
		 */
		public void updateCheckedStatus() {
			int count = 0;
			ArrayList<ItemIF> list = MainActivity.root.items.get(listId)
					.getItemList();
			for (ItemIF item : list) {

				if (item.isTicked())
					count++;

			}
			String s = String.format("%d/%d", count, list.size());
			mCheckedStatus.setText(s);
		}
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemListReceived(ItemRootElement root) {
		// TODO Auto-generated method stub

	}

	public interface ItemDataChangedListener {
		public void itemRootModified();
	}
}

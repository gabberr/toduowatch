package si.gabers.toduowatch;

import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.List;

import com.samsung.android.sdk.accessory.SAPeerAgent;

import si.gabers.toduowatch.backend.SASmartViewConsumerImpl;
import si.gabers.toduowatch.backend.SASmartViewConsumerImpl.ImageListReceiver;
import si.gabers.toduo.model.*;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;


public class ItemList extends FragmentActivity implements ImageListReceiver, ActionBar.TabListener{
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    MainListPagerAdapter mMainListPagerAdapter;

    ViewPager mViewPager;

	public static final String TAG = "SmartViewConsumerActivity";

	public static final int MSG_THUMBNAIL_RECEIVED = 1986;
	public static final int MSG_IMAGE_RECEIVED = 1987;

	SASmartViewConsumerImpl mBackendService = null;
	List<String> mDTBListReceived = null;
	String mImage = null;
	String mDownscaledImage = "";
	boolean mIsBound = false;
	boolean mReConnect = false;
	//SAPeerAgent mPeerAgent;
	public static final int MSG_INITIATE_CONNECTION = 6;
	private static Object mListLock = new Object();
	
	public static int listId = 0;
	

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
			getActionBar().setTitle( MainActivity.root.items.get(listId).toString()); 
		    
		    
		    // Create the adapter that will return a fragment for each of the three primary sections
	        // of the app.
	        mMainListPagerAdapter = new MainListPagerAdapter(getSupportFragmentManager());

	        // Set up the action bar.
	        final ActionBar actionBar = getActionBar();

	        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
	        // parent.
	        actionBar.setHomeButtonEnabled(false);

	        // Specify that we will be displaying tabs in the action bar.
//	        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

	        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
	        // user swipes between sections.
	        mViewPager = (ViewPager) findViewById(R.id.pager);
	        mViewPager.setAdapter(mMainListPagerAdapter);
	        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
	            @Override
	            public void onPageSelected(int position) {
	                // When swiping between different app sections, select the corresponding tab.
	                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
	                // Tab.
//	                actionBar.setSelectedNavigationItem(position);
	            }
	        });
	        
	        mViewPager.setCurrentItem(0);

	        // For each of the sections in the app, add a tab to the action bar.
	        for (int i = 0; i < mMainListPagerAdapter.getCount(); i++) {
	            // Create a tab with text corresponding to the page title defined by the adapter.
	            // Also specify this Activity object, which implements the TabListener interface, as the
	            // listener for when this tab is selected.
	            actionBar.addTab(
	                    actionBar.newTab()
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
    			ItemListFragment fragment =  new ItemListFragment();
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
	        public CharSequence getPageTitle(int position) {
	        	
	        	
	        	String title = "";
	        	switch(position){
	        		case 0:
	        			title = "No";
	        			break;
	        		case 1:
	        			title = "Spring Break Trip";
	        			break;
	        		case 2:
	        			title = "Yes";
	        			break;
	        	}
	            return title;
	        }
	        @Override
	        public int getItemPosition (Object object)
	        {
	          int index = MainActivity.root.items.get(listId).getItemList().indexOf (object);
	          if (index == -1)
	            return POSITION_NONE;
	          else
	            return index;
	        }
	        
	        

	    }
	 
	 public  static class ItemListFragment extends Fragment{
		 
			public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener
			{

			@Override
			 public boolean onDoubleTapEvent(MotionEvent e)
			 {
			    Log.i(TAG, "onDoubleTapEvent");
			    return true;
			    }

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				// TODO Auto-generated method stub
				Log.i(TAG, "onDoubleTap");
				return super.onDoubleTap(e);
			}
			
			
			}
		 
		 
	        @Override
	        public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                Bundle savedInstanceState) {

	        	View rootView = inflater.inflate(R.layout.activity_item, container, false);
	            Bundle args = getArguments();
	            int i = (int) args.get("i");
	            
	            CheckBox cb = (CheckBox)rootView.findViewById(R.id.checkBox1);
	            
	            if( MainActivity.root.items.get(listId).getItemList().get(i).isImageItem())
	            {
	            	rootView = inflater.inflate(R.layout.imageitem_layout, container, false);
	            	ImageView iv = (ImageView) rootView.findViewById(R.id.imageView1);
	            	
	            	ImageItemList imgitm = (ImageItemList) MainActivity.root.items.get(listId).getItemList().get(i);
	            	imgitm.decodeBitmap();
	            	iv.setImageBitmap(imgitm.getImage());
	            	cb = (CheckBox)rootView.findViewById(R.id.checkBox1);
	            }
	            
	            
	            cb.setTag(i);
	            
	            boolean isTicked = MainActivity.root.items.get(listId).getItemList().get(i).isTicked();
	            cb.setChecked(isTicked);
	            
	            
	            cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton chx, boolean arg1) {
						int id = (int) chx.getTag();
						MainActivity.root.items.get(listId).getItemList().get(id).setTicked(arg1);
//						updateUI(rootView);
					}

				
				});
	            cb.setText( MainActivity.root.items.get(listId).getItemList().get(i).toString() );
	            
	            
	            
	            return rootView;
	        }
	        void updateUI(View rootView) {
	        	TextView tv = (TextView) rootView.findViewById(R.id.textView1);
	        	
	        	
	        }

			
	    }

	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}

	public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}

	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemListReceived(ItemRootElement root) {
		// TODO Auto-generated method stub
		
	}

}

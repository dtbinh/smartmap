package ch.epfl.smartmap.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.background.Notifications;
import ch.epfl.smartmap.background.Notifications.NotificationListener;
import ch.epfl.smartmap.background.UpdateService;
import ch.epfl.smartmap.cache.DatabaseHelper;
import ch.epfl.smartmap.cache.Displayable;
import ch.epfl.smartmap.cache.MockSearchEngine;
import ch.epfl.smartmap.cache.SearchEngine;
import ch.epfl.smartmap.cache.User;
import ch.epfl.smartmap.gui.SearchLayout;
import ch.epfl.smartmap.gui.SideMenu;
import ch.epfl.smartmap.gui.SlidingPanel;
import ch.epfl.smartmap.gui.Utils;
import ch.epfl.smartmap.map.DefaultEventMarkerDisplayer;
import ch.epfl.smartmap.map.DefaultZoomManager;
import ch.epfl.smartmap.map.EventMarkerDisplayer;
import ch.epfl.smartmap.map.FriendMarkerDisplayer;
import ch.epfl.smartmap.map.ProfilePictureFriendMarkerDisplayer;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * This Activity displays the core features of the App. It displays the map and
 * the whole menu.
 * 
 * @author jfperren
 * @author SpicyCH
 * @author agpmilli
 */

public class MainActivity extends FragmentActivity {

    /**
     * Types of Menu that can be displayed on this activity
     * 
     * @author jfperren
     */
    private enum MenuTheme {
        MAP,
        SEARCH,
        ITEM;
    }

    private static final String TAG = "MAIN_ACTIVITY";
    private static final int LOCATION_UPDATE_TIMEOUT = 10000;
    private static final int GOOGLE_PLAY_REQUEST_CODE = 10;
    private static final int LOCATION_UPDATE_DISTANCE = 10;

    private static final String CITY_NAME = "CITY_NAME";
    private static final int MENU_ITEM_SEARCHBAR_INDEX = 0;
    private static final int MENU_ITEM_MYLOCATION_INDEX = 1;
    private static final int MENU_ITEM_CLOSE_SEARCH_INDEX = 2;
    private static final int MENU_ITEM_OPEN_INFO_INDEX = 3;

    private static final int MENU_ITEM_CLOSE_INFO_INDEX = 4;

    private ActionBar mActionBar;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private DatabaseHelper mDbHelper;
    private SideMenu mSideMenu;
    private GoogleMap mGoogleMap;
    private FriendMarkerDisplayer mFriendMarkerDisplayer;
    private EventMarkerDisplayer mEventMarkerDisplayer;
    private DefaultZoomManager mMapZoomer;
    private SupportMapFragment mFragmentMap;
    private SearchEngine mSearchEngine;
    private Menu mMenu;
    private MenuTheme mMenuTheme;
    private Displayable mCurrentItem;
    private Context mContext;
    private LayerDrawable mIcon;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mFriendMarkerDisplayer.updateMarkers(MainActivity.this.getContext(), mGoogleMap,
                MainActivity.this.getVisibleUsers(mDbHelper.getAllUsers()));
        }

    };

    /**
     * Close Information Panel if open
     */
    public void closeInformationPanel() {
        mMenu.getItem(MENU_ITEM_OPEN_INFO_INDEX).setVisible(true);
        mMenu.getItem(MENU_ITEM_CLOSE_INFO_INDEX).setVisible(false);
        final SlidingPanel mInformationPanel = (SlidingPanel) this.findViewById(R.id.information_panel);

        mInformationPanel.close();
    }

    /**
     * Close Information Panel if open
     */
    public void closeInformationPanel(MenuItem mi) {
        this.closeInformationPanel();
    }

    /**
     * Create a notification that appear in the notifications tab
     */
    public void createNotification(View view) {
        // Notifications.createAddNotification(view, this);
    }

    /**
     * Display the map with the current location
     */
    public void displayMap() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getBaseContext());
        // Showing status
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are
            // not available
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, GOOGLE_PLAY_REQUEST_CODE);
            dialog.show();
        } else {
            // Google Play Services are available.
            // Getting reference to the SupportMapFragment of activity_main.xml
            mFragmentMap = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
            // Getting GoogleMap object from the fragment
            mGoogleMap = mFragmentMap.getMap();
            // Enabling MyLocation Layer of Google Map
            // mGoogleMap.setMyLocationEnabled(true);
        }
    }

    public Context getContext() {
        return this;
    }

    private List<User> getVisibleUsers(LongSparseArray<User> usersSparseArray) {
        List<User> visibleUsers = new ArrayList<User>();
        for (int i = 0; i < usersSparseArray.size(); i++) {
            User user = usersSparseArray.valueAt(i);
            if (user.isVisible()) {
                visibleUsers.add(user);
            }
        }
        return visibleUsers;
    }

    public void initializeMarkers() {
        mEventMarkerDisplayer = new DefaultEventMarkerDisplayer();
        mEventMarkerDisplayer.setMarkersToMaps(this, mGoogleMap, mDbHelper.getAllEvents());
        mFriendMarkerDisplayer = new ProfilePictureFriendMarkerDisplayer();
        mFriendMarkerDisplayer.setMarkersToMaps(this, mGoogleMap,
            this.getVisibleUsers(mDbHelper.getAllUsers()));
        mMapZoomer = new DefaultZoomManager(mFragmentMap);
        Log.i(TAG, "before enter to zoom according");
        List<Marker> allMarkers = new ArrayList<Marker>(mFriendMarkerDisplayer.getDisplayedMarkers());
        allMarkers.addAll(mEventMarkerDisplayer.getDisplayedMarkers());
        Intent startingIntent = this.getIntent();
        if (startingIntent.getParcelableExtra("location") == null) {
            mMapZoomer.zoomAccordingToMarkers(mGoogleMap, allMarkers);
        }
    }

    @Override
    public void onBackPressed() {
        switch (mMenuTheme) {
            case MAP:
                super.onBackPressed();
                break;
            case SEARCH:
            case ITEM:
                this.setMainMenu(null);
                break;
            default:
                assert false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // starting the background service
        this.startService(new Intent(this, UpdateService.class));

        // TODO resolve main activity test ?
        mDbHelper = DatabaseHelper.initialize(this.getApplicationContext());

        mActionBar = this.getActionBar();

        // Set action bar color to main color
        mActionBar.setBackgroundDrawable(new ColorDrawable(this.getResources().getColor(R.color.main_blue)));

        // Makes the logo clickable (clicking it opens side menu)
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeAsUpIndicator(this.getResources().getDrawable(R.drawable.ic_drawer));

        mMenuTheme = MenuTheme.MAP;

        // Get needed Views
        final SearchLayout mSearchLayout = (SearchLayout) this.findViewById(R.id.search_layout);
        mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) this.findViewById(R.id.left_drawer_listView);

        mSideMenu = new SideMenu(this.getContext());
        mSideMenu.initializeDrawerLayout();

        // mSearchEngine = new
        // MockSearchEngine(this.getVisibleUsers(mDbHelper.getAllUsers()));
        mSearchEngine = new MockSearchEngine();
        mSearchLayout.setSearchEngine(mSearchEngine);
        mContext = this.getContext();

        if (savedInstanceState == null) {
            this.displayMap();
        }

        if (mGoogleMap != null) {
            this.initializeMarkers();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.main, menu);

        // Get the notifications MenuItem and
        // its LayerDrawable (layer-list)
        MenuItem item = menu.findItem(R.id.action_notifications);
        mIcon = (LayerDrawable) item.getIcon();

        // Update LayerDrawable's BadgeDrawable
        Utils.setBadgeCount(this, mIcon,
            Notifications.getNumberOfEventNotification() + Notifications.getNumberOfFriendNotification());

        Notifications.addNotificationListener(new NotificationListener() {
            @Override
            public void onNewNotification() {
                // Update LayerDrawable's BadgeDrawable
                Utils.setBadgeCount(mContext, mIcon, Notifications.getNumberOfEventNotification()
                    + Notifications.getNumberOfFriendNotification());
            }
        });
        mMenu = menu;
        // Get Views
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView mSearchView = (SearchView) searchItem.getActionView();
        final SearchLayout mSearchLayout = (SearchLayout) this.findViewById(R.id.search_layout);
        final SlidingPanel mSearchPanel = (SlidingPanel) this.findViewById(R.id.search_panel);

        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // Give the query results to searchLayout
                mSearchLayout.setSearchQuery(mSearchView.getQuery().toString());
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                return false;
            }
        });

        mSearchView.setOnSearchClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Sliding Panel and Displays the main search view
                String query = mSearchView.getQuery().toString();
                mSearchPanel.open();
                mSearchLayout.resetView(query);
                MainActivity.this.setSearchMenu();
            }
        });

        // Configure the search info and add any event listeners
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        // Handle click on notifications button
        if (item.getItemId() == R.id.action_notifications) {
            Notifications.setNumberOfUnreadEventNotification(0);
            Notifications.setNumberOfUnreadFriendNotification(0);
            Intent pNotifIntent = new Intent(mContext, NotificationsActivity.class);
            this.startActivity(pNotifIntent);
            return true;
        }

        // Handle clicks on home button
        if (id == android.R.id.home) {
            if (mDrawerList.isShown()) {
                Log.d("TAG", "Close side menu");
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                Log.d("TAG", "Open side menu");
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.unregisterReceiver(mBroadcastReceiver);
        // stopService(mUpdateServiceIntent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // startService(mUpdateServiceIntent);
        this.registerReceiver(mBroadcastReceiver, new IntentFilter(UpdateService.BROADCAST_POS));

        // get Intent that started this Activity
        Intent startingIntent = this.getIntent();
        // get the value of the user string
        Location eventLocation = startingIntent.getParcelableExtra("location");
        if (eventLocation != null) {
            mMapZoomer.zoomOnLocation(eventLocation, mGoogleMap);
            eventLocation = null;
        }

        // @author SpicyCH
        mGoogleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Intent result = new Intent(MainActivity.this.getContext(), AddEventActivity.class);
                Bundle extras = new Bundle();
                Geocoder geocoder = new Geocoder(MainActivity.this.getContext(), Locale.getDefault());
                String cityName = "";
                List<Address> addresses;
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses.size() > 0) {
                        // Makes sure that an address is associated to the
                        // coordinates, the user could have
                        // long
                        // clicked in the middle of the sea after all :)
                        cityName = addresses.get(0).getLocality();
                    }
                } catch (IOException e) {
                }
                if (cityName == null) {
                    // If google couldn't retrieve the city name, we use the
                    // country name instead
                    try {
                        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        if (addresses.size() > 0) {
                            cityName = addresses.get(0).getCountryName();
                        }
                    } catch (IOException e) {
                    }
                }
                extras.putString(CITY_NAME, cityName);
                extras.putParcelable(LOCATION_SERVICE, latLng);
                result.putExtras(extras);
                if (MainActivity.this.getIntent().getBooleanExtra("pickLocationForEvent", false)) {
                    // Return the result to the calling activity
                    // (AddEventActivity)
                    MainActivity.this.setResult(RESULT_OK, result);
                    MainActivity.this.finish();
                } else {
                    // The user was in MainActivity and long clicked to create
                    // an event
                    MainActivity.this.startActivity(result);
                    MainActivity.this.finish();
                }
            }
        });
    }

    /**
     * Opens Information Activity (called from MenuItem on Item view)
     * 
     * @author jfperren
     */
    public void openInformationActivity(MenuItem mi) {
        Intent intent = new Intent(this, InformationActivity.class);
        intent.putExtra("CURRENT_DISPLAYABLE", (Parcelable) mCurrentItem);
        this.startActivity(intent);
        this.finish();
    }

    /**
     * Open Information Panel if closed
     */
    public void openInformationPanel() {
        mMenu.getItem(MENU_ITEM_OPEN_INFO_INDEX).setVisible(false);
        mMenu.getItem(MENU_ITEM_CLOSE_INFO_INDEX).setVisible(true);

        final SlidingPanel mInformationPanel = (SlidingPanel) this.findViewById(R.id.information_panel);

        mInformationPanel.open();
    }

    /**
     * Open Information Panel if closed
     */
    public void openInformationPanel(MenuItem mi) {
        this.openInformationPanel();
    }

    /**
     * Computes the changes needed when a query is sent.
     * 
     * @param friend
     */
    public void performQuery(Displayable item) {
        // Get Views
        final MenuItem mSearchView = mMenu.findItem(R.id.action_search);
        final SlidingPanel mSearchPanel = (SlidingPanel) this.findViewById(R.id.search_panel);
        // Close search interface
        mSearchPanel.close();
        mSearchView.collapseActionView();
        // Focus on Friend
        mMapZoomer.zoomOnLocation(item.getLocation(), mGoogleMap);
        this.setItemMenu(item);
        // Add query to the searchEngine
        // mSearchEngine.getHistory().addEntryy(item, new Date());
    }

    /**
     * <<<<<<< HEAD
     * Sets the view for Item Focus, this means - Write name / Display photo on
     * ActionBar - Sets ActionMenu
     * for Item
     * =======
     * Sets the view for Item Focus, this means - Write name / Display photo on
     * ActionBar - Sets ActionMenu for Item
     * >>>>>>> service-2
     * Focus
     * 
     * @param item
     *            Item to be displayed
     */
    public void setItemMenu(Displayable item) {
        mMenu.getItem(MENU_ITEM_MYLOCATION_INDEX).setVisible(false);
        mMenu.getItem(MENU_ITEM_CLOSE_SEARCH_INDEX).setVisible(false);
        mMenu.getItem(MENU_ITEM_OPEN_INFO_INDEX).setVisible(true);
        mMenu.getItem(MENU_ITEM_CLOSE_INFO_INDEX).setVisible(false);

        ActionBar mActionBar = this.getActionBar();
        mActionBar.setTitle(item.getName());
        mActionBar.setSubtitle(item.getShortInfos());
        mActionBar.setIcon(new BitmapDrawable(this.getResources(), item.getPicture(this)));
        mCurrentItem = item;
        mMenuTheme = MenuTheme.ITEM;
    }

    /**
     * Sets the main Menu of the Activity
     */
    public void setMainMenu() {
        final SlidingPanel mSearchPanel = (SlidingPanel) this.findViewById(R.id.search_panel);
        mSearchPanel.close();
        ActionBar mActionBar = this.getActionBar();
        mActionBar.setTitle(R.string.app_name);
        mActionBar.setSubtitle(null);
        mActionBar.setIcon(R.drawable.ic_launcher);
        mMenu.getItem(MENU_ITEM_SEARCHBAR_INDEX).collapseActionView();
        mMenu.getItem(MENU_ITEM_MYLOCATION_INDEX).setVisible(true);
        mMenu.getItem(MENU_ITEM_CLOSE_SEARCH_INDEX).setVisible(false);
        mMenu.getItem(MENU_ITEM_OPEN_INFO_INDEX).setVisible(false);
        mMenu.getItem(MENU_ITEM_CLOSE_INFO_INDEX).setVisible(false);
        mMenuTheme = MenuTheme.MAP;
    }

    public void setMainMenu(MenuItem mi) {
        this.setMainMenu();
    }

    /**
     * Sets the Menu that should be used when using Search Panel
     */
    public void setSearchMenu() {
        final SlidingPanel mSearchPanel = (SlidingPanel) this.findViewById(R.id.search_panel);
        mSearchPanel.open();
        ActionBar mActionBar = this.getActionBar();
        mActionBar.setTitle(R.string.app_name);
        mActionBar.setSubtitle(null);
        mActionBar.setIcon(R.drawable.ic_launcher);

        mMenu.getItem(MENU_ITEM_MYLOCATION_INDEX).setVisible(false);
        mMenu.getItem(MENU_ITEM_CLOSE_SEARCH_INDEX).setVisible(true);
        mMenu.getItem(MENU_ITEM_OPEN_INFO_INDEX).setVisible(false);
        mMenu.getItem(MENU_ITEM_CLOSE_INFO_INDEX).setVisible(false);
        mMenuTheme = MenuTheme.SEARCH;
    }

}
package ch.epfl.smartmap.activities;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.cache.DatabaseHelper;
import ch.epfl.smartmap.cache.Event;
import ch.epfl.smartmap.cache.UserEvent;
import ch.epfl.smartmap.gui.EventsListItemAdapter;

/**
 * This activity shows the events and offers to filter them.
 *
 * @author SpicyCH
 *
 */
public class ShowEventsActivity extends ListActivity {

    private final static String TAG = ShowEventsActivity.class.getSimpleName();

    private final static double EARTH_RADIUS_KM = 6378.1;
    private final static int SEEK_BAR_MIN_VALUE = 2;
    private final static int ONE_HUNDRED = 100;
    private static final long LOCATION_REFRESH_TIME = 10000;
    private static final long LOCATION_MIN_DISTANCE = 15;

    private SeekBar mSeekBar;
    private TextView mShowKilometers;
    private CheckBox mNearMeCheckBox;

    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    private Context mContext;

    private DatabaseHelper mDbHelper;

    private boolean mMyEventsChecked;
    private boolean mOngoingChecked;
    private boolean mNearMeChecked;

    private List<Event> mEventsList;
    private List<Event> mCurrentList;
    private static String mMyName = "Robich";
    private Location mMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_events);

        // Makes the logo clickable (clicking it returns to previous activity)
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mMyLocation = new Location("GPS + NETWORK");
        /*
         * mMyLocation.setLatitude(46.509300); mMyLocation.setLongitude(6.661600);
         */
        mMyLocation.setLatitude(0);
        mMyLocation.setLongitude(0);
        mContext = getApplicationContext();

        mLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location loc) {
                mMyLocation.set(loc);
                updateCurrentList();
                mNearMeCheckBox.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Since we were able to fetch the user's position, we reenable the function associated to this
                        // View
                        onCheckboxClicked(v);
                    }

                });
                Log.i(TAG, "User's location updated");
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_MIN_DISTANCE, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_MIN_DISTANCE, mLocationListener);

        mMyEventsChecked = false;
        mOngoingChecked = false;
        mNearMeChecked = false;

        // We only make this checkbox available as soon as we get the user's position
        mNearMeCheckBox = (CheckBox) findViewById(R.id.ShowEventsCheckBoxNearMe);
        mNearMeCheckBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(
                        getApplicationContext(),
                        "Your position hasn't been retrieved yet. Make sure your GPS or your cellular network is "
                                + "turned on.", Toast.LENGTH_LONG).show();
            }
        });

        mShowKilometers = (TextView) findViewById(R.id.showEventKilometers);
        // By default, the seek bar is disabled. This is done programmatically
        // as android:enabled="false" doesn't work
        // out in xml
        mSeekBar = (SeekBar) findViewById(R.id.showEventSeekBar);
        mSeekBar.setEnabled(false);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.getProgress() < SEEK_BAR_MIN_VALUE) {
                    seekBar.setProgress(SEEK_BAR_MIN_VALUE);
                }
                mShowKilometers.setText(mSeekBar.getProgress() + " km");
                updateCurrentList();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });

        /*
         * GregorianCalendar timeE0 = new GregorianCalendar(); timeE0.add(GregorianCalendar.MINUTE, -5);
         * GregorianCalendar timeEndE0 = new GregorianCalendar(); timeEndE0.add(GregorianCalendar.HOUR_OF_DAY, 5);
         * Location lutry = new Location("Lutry"); lutry.setLatitude(46.506038); lutry.setLongitude(6.685314);
         *
         * UserEvent e0 = new UserEvent("Now Event", 2, "Robich", timeE0, timeEndE0, lutry); e0.setID(0);
         * e0.setPositionName("Lutry");
         *
         * GregorianCalendar timeE1 = new GregorianCalendar(); timeE1.add(GregorianCalendar.DAY_OF_YEAR, 5);
         * GregorianCalendar timeEndE1 = new GregorianCalendar(); timeEndE1.add(GregorianCalendar.DAY_OF_YEAR, 10);
         * Location lausanne = new Location("Lausanne"); lausanne.setLatitude(46.519962);
         * lausanne.setLongitude(6.633597);
         *
         * UserEvent e1 = new UserEvent("Swag party", 2, "Robich", timeE1, timeEndE1, lausanne); e1.setID(1);
         * e1.setPositionName("Lausanne");
         *
         * GregorianCalendar timeE2 = new GregorianCalendar(); timeE2.add(GregorianCalendar.HOUR_OF_DAY, 3);
         * GregorianCalendar timeEndE2 = new GregorianCalendar(); timeEndE2.add(GregorianCalendar.DAY_OF_YEAR, 2);
         * Location epfl = new Location("EPFL"); epfl.setLatitude(46.526120); epfl.setLongitude(6.563778);
         *
         * UserEvent e2 = new UserEvent("LOL Tournament", 1, "Alain", timeE2, timeEndE2, epfl); e2.setID(2);
         * e2.setPositionName("EPFL");
         *
         * GregorianCalendar timeE3 = new GregorianCalendar(); timeE3.add(GregorianCalendar.HOUR_OF_DAY, 1);
         * GregorianCalendar timeEndE3 = new GregorianCalendar(); timeEndE3.add(GregorianCalendar.HOUR, 5); Location
         * verbier = new Location("Verbier"); verbier.setLatitude(46.096076); verbier.setLongitude(7.228875);
         *
         * UserEvent e3 = new UserEvent("Freeride World Tour", 1, "Julien", timeE3, timeEndE3, verbier); e3.setID(3);
         * e3.setPositionName("Verbier"); String descrE3 =
         * "It�s a vertical free-verse poem on the mountain. It�s the ultimate expression of all that" +
         * "is fun and liberating about sliding on snow in wintertime."; e3.setDescription(descrE3);
         */

        mDbHelper = new DatabaseHelper(this);
        /*
         * mDbHelper.addEvent(e0); mDbHelper.addEvent(e1); mDbHelper.addEvent(e2); mDbHelper.addEvent(e3);
         */

        mEventsList = mDbHelper.getAllEvents();

        // Create custom Adapter and pass it to the Activity
        EventsListItemAdapter adapter = new EventsListItemAdapter(this, mEventsList, mMyLocation);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // This is needed to show an update of the events' list after having created an event
        updateCurrentList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.showEventsMenuNewEvent:
                Intent showEventIntent = new Intent(mContext, AddEventActivity.class);
                startActivity(showEventIntent);
            default:
                // No other menu items!
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Event event = (UserEvent) findViewById(position).getTag();

        String message = EventsListItemAdapter.setTextFromDate(event.getStartDate(), event.getEndDate(), "start")
                + " - " + EventsListItemAdapter.setTextFromDate(event.getStartDate(), event.getEndDate(), "end")
                + "\nCreated by " + event.getCreatorName() + "\n\n" + event.getDescription();

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(event.getName()
                + " @ "
                + event.getPositionName()
                + "\n"
                + distance(mMyLocation.getLatitude(), mMyLocation.getLongitude(), event.getLocation().getLatitude(),
                        event.getLocation().getLongitude()) + " km away");
        alertDialog.setMessage(message);
        final Activity activity = this;
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Show on the map", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                // TODO open event in map
                Toast.makeText(activity, "Opening event on the map...", Toast.LENGTH_SHORT).show();

                Intent showEventIntent = new Intent(mContext, MainActivity.class);
                showEventIntent.putExtra("location", event.getLocation());
                startActivity(showEventIntent);

            }
        });

        alertDialog.show();

        super.onListItemClick(l, v, position, id);
    }

    public void onCheckboxClicked(View v) {
        CheckBox checkBox = (CheckBox) v;

        switch (v.getId()) {
            case R.id.ShowEventsCheckBoxNearMe:
                if (checkBox.isChecked()) {
                    mNearMeChecked = true;
                    // Show the seek bar
                    mSeekBar.setEnabled(true);
                } else {
                    mNearMeChecked = false;
                    // Hide the seek bar
                    mSeekBar.setEnabled(false);
                }
                break;
            case R.id.ShowEventsCheckBoxMyEv:
                if (checkBox.isChecked()) {
                    mMyEventsChecked = true;
                } else {
                    mMyEventsChecked = false;
                }
                break;
            case R.id.ShowEventscheckBoxStatus:
                if (checkBox.isChecked()) {
                    mOngoingChecked = true;
                } else {
                    mOngoingChecked = false;
                }
                break;
            default:
                break;
        }

        updateCurrentList();
    }

    /**
     * This runs in O(n), can we do better?
     */
    private void updateCurrentList() {

        mEventsList = mDbHelper.getAllEvents();
        mCurrentList = new ArrayList<Event>();

        // Copy complete list into current list
        for (Event e : mEventsList) {
            mCurrentList.add(e);
        }

        for (Event e : mEventsList) {
            if (mMyEventsChecked) {
                if (!e.getCreatorName().equals(mMyName)) {
                    mCurrentList.remove(e);
                }
            }

            if (mOngoingChecked) {
                if (!e.getStartDate().before(new GregorianCalendar())) {
                    mCurrentList.remove(e);
                }
            }

            if (mNearMeChecked) {
                if (mMyLocation != null) {
                    double distanceMeEvent = distance(e.getLocation().getLatitude(), e.getLocation().getLongitude(),
                            mMyLocation.getLatitude(), mMyLocation.getLongitude());
                    String[] showKMContent = mShowKilometers.getText().toString().split(" ");
                    double distanceMax = Double.parseDouble(showKMContent[0]);
                    if (!(distanceMeEvent < distanceMax)) {
                        mCurrentList.remove(e);
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Your current location cannot be retrieved. Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        }

        EventsListItemAdapter adapter = new EventsListItemAdapter(this, mCurrentList, mMyLocation);
        setListAdapter(adapter);
    }

    /**
     * Computes the distance between two GPS locations (takes into consideration the earth radius), inspired by
     * wikipedia
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return the distance in km, rounded to 2 digits
     * @author SpicyCH
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double radLat1 = Math.toRadians(lat1);
        double radLong1 = Math.toRadians(lon1);
        double radLat2 = Math.toRadians(lat2);
        double radLong2 = Math.toRadians(lon2);

        double sec1 = Math.sin(radLat1) * Math.sin(radLat2);
        double dl = Math.abs(radLong1 - radLong2);
        double sec2 = Math.cos(radLat1) * Math.cos(radLat2);
        double centralAngle = Math.acos(sec1 + sec2 * Math.cos(dl));
        double distance = centralAngle * EARTH_RADIUS_KM;

        return Math.floor(distance * ONE_HUNDRED) / ONE_HUNDRED;
    }
}
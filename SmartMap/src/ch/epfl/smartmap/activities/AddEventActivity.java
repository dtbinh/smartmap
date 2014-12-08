package ch.epfl.smartmap.activities;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.background.ServiceContainer;
import ch.epfl.smartmap.background.SettingsManager;
import ch.epfl.smartmap.cache.ImmutableEvent;
import ch.epfl.smartmap.cache.PublicEvent;
import ch.epfl.smartmap.callbacks.NetworkRequestCallback;
import ch.epfl.smartmap.gui.DatePickerFragment;
import ch.epfl.smartmap.gui.TimePickerFragment;
import ch.epfl.smartmap.map.DefaultZoomManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * This activity lets the user create a new event.
 * 
 * @author SpicyCH
 * @author agpmilli
 */
public class AddEventActivity extends FragmentActivity {

    public static final String LOCATION_EXTRA = "LOCATION";

    private static final String TAG = AddEventActivity.class.getSimpleName();

    private static final int GOOGLE_PLAY_REQUEST_CODE = 10;

    static final int PICK_LOCATION_REQUEST = 1;
    private static final int ELEMENTS_HH_MM = 2;
    private static final int ELEMENTS_JJ_DD_YYYY = 3;

    private static final int INDEX_YEAR = 2;
    private static final int INDEX_MONTH = 1;
    private static final int INDEX_DAY = 0;

    private GoogleMap mGoogleMap;
    private SupportMapFragment mFragmentMap;
    private LatLng mEventPosition;
    private Context mContext;
    private Activity mActivity;
    private EditText mDescription;
    private EditText mEventName;
    private EditText mPickEndDate;
    private EditText mPickEndTime;
    private EditText mPickStartDate;
    private EditText mPickStartTime;
    private EditText mPlaceName;

    private TextWatcher mTextChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_add_event);

        // Makes the logo clickable (clicking it returns to previous activity)
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        this.getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.color.main_blue));

        this.initializeGUIComponents();

        mGoogleMap.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng position) {
                if (mEventPosition != null) {
                    mGoogleMap.clear();
                }
                Intent setLocationIntent = new Intent(AddEventActivity.this, SetLocationActivity.class);
                setLocationIntent.putExtra("eventPosition", mEventPosition);
                AddEventActivity.this.startActivityForResult(setLocationIntent, PICK_LOCATION_REQUEST);
            }
        });

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            LatLng latLng = extras.getParcelable(LOCATION_SERVICE);
            if ((latLng != null) && (Math.abs(latLng.latitude) > 0)) {
                // The user long clicked the map in MainActivity and wants to
                // create an event
                this.updateLocation(this.getIntent());
            }
        }
    }

    /**
     * Display the map with the current location
     */
    public void displayMap() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getBaseContext());
        // Showing status
        if (status != ConnectionResult.SUCCESS) {
            // Google Play Services are not available
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, GOOGLE_PLAY_REQUEST_CODE);
            dialog.show();
        } else {
            // Google Play Services are available.
            // Getting reference to the SupportMapFragment of activity_main.xml
            mFragmentMap = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.add_event_map);
            // Getting GoogleMap object from the fragment
            mGoogleMap = mFragmentMap.getMap();
            // Enabling MyLocation Layer of Google Map
            mGoogleMap.setMyLocationEnabled(true);

            mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

            mGoogleMap.addMarker(new MarkerOptions().position(mEventPosition));

            new DefaultZoomManager(mFragmentMap).zoomWithAnimation(new LatLng(ServiceContainer.getSettingsManager()
                    .getLocation().getLatitude(), ServiceContainer.getSettingsManager().getLocation().getLongitude()));

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_LOCATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                // All went smoothly, update location in this activity
                this.updateLocation(data);

            } else {
                // Google wasn't able to retrieve the location name associated
                // to the coordinates
                Toast.makeText(mContext, this.getString(R.string.add_event_toast_couldnt_retrieve_location),
                        Toast.LENGTH_LONG).show();
                mPlaceName.setText("");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.add_event, menu);
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
                this.finish();
                break;
            case R.id.addEventButtonCreateEvent:
                this.createEvent();
                break;
            default:
                // No other menu items!
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void pickLocation(View v) {

        // Hack to remove warning from SonarQube, use view for log.
        Log.d(TAG, "View " + v.getId() + " clicked");
        Toast.makeText(mContext, this.getString(R.string.add_event_toast_indication_long_click_map_to_create_event),
                Toast.LENGTH_LONG).show();

        Intent pickLocationIntent = new Intent(mContext, MainActivity.class);
        pickLocationIntent.putExtra("pickLocationForEvent", true);
        pickLocationIntent.setType(Context.LOCATION_SERVICE);
        this.startActivityForResult(pickLocationIntent, PICK_LOCATION_REQUEST);
    }

    /**
     * @return <code>true</code> if all the fields (event name, event dates, etc...) are legally set and the event is
     *         ready to be created.
     * @author SpicyCH
     */
    private boolean allFieldsSetByUser() {
        boolean validEndDateTime = this.isValidDate(mPickEndDate.getText().toString())
                && this.isValidTime(mPickEndTime.getText().toString());

        boolean validPosition = (mEventPosition.latitude != 0) && (mEventPosition.longitude != 0);

        return validEndDateTime && validPosition
                && (!"".equals(mPlaceName.getText().toString()) && !"".equals(mEventName.getText().toString()));
    }

    /**
     * Ensures the end of the event is after its start and end of the event is not in the past. Displays a toast and
     * reset the bad field set by the user if necessary.
     * 
     * @param startDate
     * @param startTime
     * @param endDate
     * @param endTime
     * @author SpicyCH
     */
    private void checkDatesValidity(EditText startDate, EditText startTime, EditText endDate, EditText endTime) {

        if (this.isValidDate(endDate.getText().toString()) && this.isValidTime(endTime.getText().toString())) {
            // The end of the event has been set by the user

            GregorianCalendar start = this.getDateFromTextFormat(startDate.getText().toString(), startTime.getText()
                    .toString());

            GregorianCalendar end = this.getDateFromTextFormat(endDate.getText().toString(), endTime.getText()
                    .toString());

            GregorianCalendar now = new GregorianCalendar();

            // Needed to let the user click the default time without errors.
            now.add(GregorianCalendar.MINUTE, -1);

            if (end.before(start)) {
                // The user is trying to create the end of the event before its
                // start!

                endDate.setText("");
                endTime.setText("");

                Toast.makeText(mContext, this.getString(R.string.add_event_toast_event_cannot_end_before_starting),
                        Toast.LENGTH_LONG).show();
            } else if (end.before(now)) {
                // The user is trying to create an event in the past

                endDate.setText("");
                endTime.setText("");

                Toast.makeText(mContext, this.getString(R.string.add_event_toast_event_end_cannot_be_in_past),
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * Creates an event from the user specified parameters.
     * 
     * @author SpicyCH
     */
    private void createEvent() {

        if (!this.allFieldsSetByUser()) {

            Toast.makeText(mContext, this.getString(R.string.add_event_toast_not_all_fields_set), Toast.LENGTH_SHORT)
                    .show();

            Log.d(TAG, "Couldn't create a new event because not all fields were set.\n" + "end date: "
                    + mPickEndDate.getText().toString() + "\n" + "end time: " + mPickEndTime.getText().toString()
                    + "\n" + "event name: " + mEventName.getText().toString() + "\n" + "event place name: "
                    + mPlaceName.getText().toString() + "\n" + "event lat/long: " + mEventPosition.latitude + "/"
                    + mEventPosition.longitude);
        } else {

            GregorianCalendar startDate = this.getDateFromTextFormat(mPickStartDate.getText().toString(),
                    mPickStartTime.getText().toString());
            GregorianCalendar endDate = this.getDateFromTextFormat(mPickEndDate.getText().toString(), mPickEndTime
                    .getText().toString());

            Location location = new Location("Location set by user");
            location.setLatitude(mEventPosition.latitude);
            location.setLongitude(mEventPosition.longitude);
            SettingsManager setMng = ServiceContainer.getSettingsManager();

            ImmutableEvent event = new ImmutableEvent(PublicEvent.NO_ID, mEventName.getText().toString(),
                    setMng.getUserId(), mDescription.getText().toString(), startDate, endDate, location, mPlaceName
                            .getText().toString(), PublicEvent.NO_PARTICIPANTS);

            ServiceContainer.getCache().createEvent(event, new NetworkRequestCallback() {

                @Override
                public void onFailure() {
                    AddEventActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,
                                    mContext.getString(R.string.add_event_toast_couldnt_create_event_server),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onSuccess() {
                    AddEventActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, mContext.getString(R.string.add_event_toast_event_created),
                                    Toast.LENGTH_SHORT).show();

                            mActivity.finish();
                        }
                    });
                }
            });
        }
    }

    /**
     * @param dayMonthYear
     *            a String like "16/09/1993"
     * @param hourMinute
     *            a String like "17:03"
     * @return a GregorianDate constructed from the given parameters
     * @author SpicyCH
     */
    private GregorianCalendar getDateFromTextFormat(String dayMonthYear, String hourMinute) {
        assert this.isValidDate(dayMonthYear) : "The string dayMonthYear isn't in the expected format";
        assert this.isValidTime(hourMinute) : "The string hourMinute isn't in the expected format";

        String[] s1 = dayMonthYear.split("/");
        String[] s2 = hourMinute.split(":");

        final int month = Integer.parseInt(s1[INDEX_MONTH]) - 1;

        return new GregorianCalendar(Integer.parseInt(s1[INDEX_YEAR]), month, Integer.parseInt(s1[INDEX_DAY]),
                Integer.parseInt(s2[0]), Integer.parseInt(s2[1]), 0);

    }

    /**
     * @author SpicyCH
     */
    private void initializeGUIComponents() {
        mActivity = this;
        mContext = this.getApplicationContext();
        mEventName = (EditText) this.findViewById(R.id.addEventEventName);
        mPickStartDate = (EditText) this.findViewById(R.id.addEventEventDate);
        mPickStartTime = (EditText) this.findViewById(R.id.addEventEventTime);
        mPickEndTime = (EditText) this.findViewById(R.id.addEventEndTime);
        mPickEndDate = (EditText) this.findViewById(R.id.addEventEndDate);
        mDescription = (EditText) this.findViewById(R.id.addEventDescription);
        mPlaceName = (EditText) this.findViewById(R.id.addEventPlaceName);

        mPlaceName.setFocusable(true);
        // Initialize mEventPosition to position of user
        mEventPosition = new LatLng(ServiceContainer.getSettingsManager().getLocation().getLatitude(), ServiceContainer
                .getSettingsManager().getLocation().getLongitude());

        mTextChangedListener = new DateChangedListener();

        mPickStartDate.addTextChangedListener(mTextChangedListener);
        mPickStartTime.addTextChangedListener(mTextChangedListener);
        mPickEndDate.addTextChangedListener(mTextChangedListener);
        mPickEndTime.addTextChangedListener(mTextChangedListener);

        GregorianCalendar now = new GregorianCalendar();

        mPickStartTime.setText(TimePickerFragment.formatForClock(now.get(Calendar.HOUR_OF_DAY)) + ":"
                + TimePickerFragment.formatForClock(now.get(Calendar.MINUTE)));

        mPickStartTime.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment(mPickStartTime);
                newFragment.show(AddEventActivity.this.getSupportFragmentManager(), "timePicker");
            }

        });

        mPickStartDate.setText(now.get(Calendar.DAY_OF_MONTH) + "/" + (now.get(Calendar.MONTH) + 1) + "/"
                + now.get(Calendar.YEAR));

        mPickStartDate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(mPickStartDate);
                newFragment.show(AddEventActivity.this.getSupportFragmentManager(), "datePicker");
            }
        });

        mPickEndDate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(mPickEndDate);
                newFragment.show(AddEventActivity.this.getSupportFragmentManager(), "datePicker");
            }
        });

        mPickEndTime.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment(mPickEndTime);
                newFragment.show(AddEventActivity.this.getSupportFragmentManager(), "timePicker");
            }
        });
        this.displayMap();
    }

    private boolean isValidDate(String s) {
        String[] sArray = s.split("/");
        return sArray.length == ELEMENTS_JJ_DD_YYYY;
    }

    private boolean isValidTime(String s) {
        String[] sArray = s.split(":");
        return sArray.length == ELEMENTS_HH_MM;
    }

    /**
     * @param data
     *            the intent containing the extras. The position (LatLgn) is retrieved from the getParcelable().
     * @author SpicyCH
     */
    private void updateLocation(Intent data) {
        Bundle extras = data.getExtras();

        mEventPosition = extras.getParcelable(LOCATION_EXTRA);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String cityName = "";
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(mEventPosition.latitude, mEventPosition.longitude, 1);
        } catch (IOException e) {
            Log.d(TAG, "Google couldn't retrieve any adresses fromt he coordinates: " + e);
        }
        if ((!addresses.isEmpty()) || ((cityName != null) && !cityName.isEmpty())) {
            cityName = addresses.get(0).getLocality();
            mPlaceName.setText(cityName);
            mGoogleMap.addMarker(new MarkerOptions().position(mEventPosition));
            new DefaultZoomManager(mFragmentMap).zoomWithAnimation(mEventPosition);
        } else {
            Toast.makeText(mContext, this.getString(R.string.add_event_toast_couldnt_retrieve_location_name),
                    Toast.LENGTH_LONG).show();
            mPlaceName.setText("");
        }
    }

    class DateChangedListener implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            // Remove the TextChangedListener to avoid useless calls
            // triggered by the following code
            mPickEndDate.removeTextChangedListener(mTextChangedListener);
            mPickStartDate.removeTextChangedListener(mTextChangedListener);

            AddEventActivity.this.checkDatesValidity(mPickStartDate, mPickStartTime, mPickEndDate, mPickEndTime);

            // Reset the TextChangedListener
            mPickEndDate.addTextChangedListener(mTextChangedListener);
            mPickStartDate.addTextChangedListener(mTextChangedListener);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Nothing
        }
    }
}
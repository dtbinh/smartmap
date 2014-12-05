package ch.epfl.smartmap.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Random;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.util.SystemUiHider;

/**
 * This full-screen activity displays the credits for SmartMap. The name of the developers are sorted in random order
 * and it uses the version name and version code defined in the android manifest.
 * 
 * @see SystemUiHider
 * @author SpicyCH
 */
public class AboutActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction before hiding the system
     * UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise, will show the system UI visibility upon
     * interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    private static final String TAG = AboutActivity.class.getSimpleName();

    /**
     * The year where the copyright begins.
     */
    private final String APP_BORN_YEAR = "2014";

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the system UI. This is to prevent the jarring
     * behavior of controls going away while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                AboutActivity.this.delayedHide(AUTO_HIDE_DELAY_MILLIS);
                view.performClick();
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();

    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    private TextView mVersion;
    private TextView mCopyright;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_about);

        // Hide action bar for true full screen. Only way to leave this activity is to physically use the pressback
        // button.
        this.getActionBar().hide();

        final View contentView = this.findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
            // Cached values.
            int mShortAnimTime;

            @Override
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
            public void onVisibilityChange(boolean visible) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    if (mShortAnimTime == 0) {
                        mShortAnimTime = AboutActivity.this.getResources().getInteger(
                                android.R.integer.config_shortAnimTime);
                    }
                }

                if (visible && AUTO_HIDE) {
                    // Schedule a hide().
                    AboutActivity.this.delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
            }
        });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        this.initializeGUI();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        this.delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Add each item of the list as a TextView children of the given LinearLayout.
     * 
     * @param teamMembers
     * @param linearLayout
     * 
     * @author SpicyCH
     */
    private void displayListInLayout(ArrayList<String> teamMembers, LinearLayout linearLayout) {
        for (String s : teamMembers) {
            TextView textView = new TextView(this.getApplicationContext());
            textView.setText(s);

            LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            linearLayout.addView(textView, lp);
        }
    }

    /**
     * Initilizes the GUI.
     * 
     * @author SpicyCH
     */
    private void initializeGUI() {
        mVersion = (TextView) this.findViewById(R.id.about_version);
        mCopyright = (TextView) this.findViewById(R.id.about_copyright);

        // Display version of this package (to change it, edit versionCode/versionName in the manifest
        try {
            PackageInfo manager = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            mVersion.setText(this.getString(R.string.about_version) + " " + manager.versionName + ", "
                    + this.getString(R.string.about_release) + " " + manager.versionCode);

        } catch (NameNotFoundException e) {
            Log.d(TAG, "Couldn't retrieve package version: " + e);
            mVersion.setText(this.getString(R.string.about_version) + " "
                    + this.getString(R.string.about_unkown_version));
        }

        // Display the copyright
        GregorianCalendar now = new GregorianCalendar();
        String year = Integer.toString(now.get(Calendar.YEAR));
        String copyrightMsg = "\u00a9 " + year;

        // Infinite copyright ((c) 2014 - YYYY)
        if (!year.equals(APP_BORN_YEAR)) {
            copyrightMsg = "\u00a9 " + this.APP_BORN_YEAR + " - " + year;
        }
        mCopyright.setText(copyrightMsg);

        // Display team members in a random order

        ArrayList<String> teamMembers = new ArrayList<String>();
        teamMembers.add("Robin Genolet");
        teamMembers.add("Julien Perrenoud");
        teamMembers.add("Matthieu Girod");
        teamMembers.add("Alain Milliet");
        teamMembers.add("Nicolas Ritter");
        teamMembers.add("Raphaël Steinmann");
        teamMembers.add("Hugo Sbai");
        teamMembers.add("Marion Sbai");

        Collections.shuffle(teamMembers, new Random(System.nanoTime()));

        LinearLayout teamMembersHolder = (LinearLayout) this.findViewById(R.id.about_team_members_holder);
        teamMembersHolder.setGravity(Gravity.CENTER);

        this.displayListInLayout(teamMembers, teamMembersHolder);

        // Display thanks list (in non-random order)
        ArrayList<String> thanksTo = new ArrayList<String>();
        thanksTo.add("Prof. James Larus");
        thanksTo.add("Călin Iorgulescu");
        thanksTo.add("Lukas Kellenberger");

        LinearLayout thanksHolder = (LinearLayout) this.findViewById(R.id.about_thanks_holder);
        thanksHolder.setGravity(Gravity.CENTER);

        this.displayListInLayout(thanksTo, thanksHolder);
    }
}
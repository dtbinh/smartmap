package ch.epfl.smartmap.test.activities;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.activities.AboutActivity;

import com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions;
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers;

/**
 * Tests that important informations are shown on AboutActivity.
 * 
 * @author SpicyCH
 */
public class AboutActivityTest extends ActivityInstrumentationTestCase2<AboutActivity> {

    private static final String TAG = AboutActivityTest.class.getSimpleName();
    private Context mContext;

    public AboutActivityTest() {
        super(AboutActivity.class);
    }

    // The standard JUnit 3 setUp method run for for every test
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.getActivity();
        mContext = this.getActivity().getApplicationContext();
    }

    public void testAppNameDisplayed() {
        String appName = mContext.getString(R.string.app_name);
        onView(ViewMatchers.withText(appName)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    public void testOurNamesDisplayed() {
        List<String> teamMembers = Arrays.asList(mContext.getResources().getStringArray(R.array.app_authors));

        for (String s : teamMembers) {
            onView(ViewMatchers.withText(s)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    public void testThanksListDisplayed() {
        List<String> thanksTo =
            Arrays.asList(mContext.getResources().getStringArray(R.array.app_special_thanks));

        for (String s : thanksTo) {
            onView(ViewMatchers.withText(s)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    public void testVersionNumberDisplayed() {
        String versionAndRelease = "fail";

        try {
            PackageInfo manager = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            versionAndRelease =
                mContext.getString(R.string.about_version) + " " + manager.versionName + "."
                    + manager.versionCode;

        } catch (NameNotFoundException e) {
            Log.e(TAG, "Name not found: " + e);
        }

        onView(withId(R.id.about_version)).check(
            ViewAssertions.matches(ViewMatchers.withText(versionAndRelease)));
    }

}

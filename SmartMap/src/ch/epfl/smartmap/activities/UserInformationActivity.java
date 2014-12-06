package ch.epfl.smartmap.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import ch.epfl.smartmap.R;
import ch.epfl.smartmap.background.SettingsManager;
import ch.epfl.smartmap.cache.Cache;
import ch.epfl.smartmap.cache.User;
import ch.epfl.smartmap.servercom.NetworkSmartMapClient;
import ch.epfl.smartmap.servercom.SmartMapClientException;

/**
 * Activity that shows full informations about a Displayable Object.
 * 
 * @author rbsteinm
 */
public class UserInformationActivity extends Activity {

	/**
	 * Asynchronous task that removes a friend from the users friendList both
	 * from the server and from the cache
	 * 
	 * @author rbsteinm
	 */
	private class RemoveFriend extends AsyncTask<Long, Void, String> {

		private final Handler mHandler = new Handler();

		@Override
		protected String doInBackground(Long... params) {
			String confirmString = "";
			try {
				NetworkSmartMapClient.getInstance().removeFriend(params[0]);
				confirmString = "You're no longer friend with " + mUser.getName();

				// remove friend from cache and update displayed list
				// TODO should not have to remove the friend from the cache too
				final long userId = params[0];
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Cache.getInstance().removeFriend(userId);
					}
				});

			} catch (SmartMapClientException e) {
				confirmString = "Network error, operation failed";
			}
			return confirmString;
		}

		@Override
		protected void onPostExecute(String confirmString) {
			Toast.makeText(UserInformationActivity.this, confirmString, Toast.LENGTH_LONG).show();
		}

	}

	@SuppressWarnings("unused")
	private static final String TAG = UserInformationActivity.class.getSimpleName();
	private User mUser;

	private int mDistanceToUser;
	private Switch mFollowSwitch;
	private Switch mBlockSwitch;
	private TextView mSubtitlesView;
	private TextView mNameView;
	private ImageView mPictureView;

	private TextView mDistanceView;
	// TODO replace this by mUser.isFollowing() and mUser.isBlocked() when implemented
	private boolean isFollowing;

	private final boolean isBlocked = false;

	public void displayDeleteConfirmationDialog(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("remove " + mUser.getName() + " from your friends?");

		// Add positive button
		builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				new RemoveFriend().execute(mUser.getId());
			}
		});

		// Add negative button
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		// display the AlertDialog
		builder.create().show();
	}

	public void followUnfollow(View view) {
		// TODO need methond setVisible
		// new FollowFriend().execute(mUser.getId());
	}

	@Override
	public void onBackPressed() {
		this.onNotificationOpen();
		this.finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_user_information);
		// Get views
		mPictureView = (ImageView) this.findViewById(R.id.user_info_picture);
		mNameView = (TextView) this.findViewById(R.id.user_info_name);
		mSubtitlesView = (TextView) this.findViewById(R.id.user_info_subtitles);
		mFollowSwitch = (Switch) this.findViewById(R.id.user_info_follow_switch);
		mBlockSwitch = (Switch) this.findViewById(R.id.user_info_blocking_switch);
		mDistanceView = (TextView) this.findViewById(R.id.user_info_distance);
		// Set actionbar color
		this.getActionBar().setBackgroundDrawable(
		    new ColorDrawable(this.getResources().getColor(R.color.main_blue)));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.user_information, menu);
		return true;
	}

	/**
	 * When this tab is open by a notification
	 */
	private void onNotificationOpen() {
		if (this.getIntent().getBooleanExtra("NOTIFICATION", false)) {
			this.startActivity(new Intent(this, MainActivity.class));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				this.onNotificationOpen();
				this.finish();
				break;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	// TODO remove this code
	/**
	 * @author rbsteinm
	 *         AsyncTask that follows/unfollows a friend when the user checks/unchecks
	 *         the checkBox
	 */
	/*
	 * private class FollowFriend extends AsyncTask<Long, Void, String> {
	 * @Override
	 * protected String doInBackground(Long... params) {
	 * String confirmString = "";
	 * try {
	 * if (mFollowSwitch.isChecked()) {
	 * NetworkSmartMapClient.getInstance().followFriend(mUser.getId());
	 * confirmString = "You're now following " + mUser.getName();
	 * } else {
	 * NetworkSmartMapClient.getInstance().unfollowFriend(mUser.getId());
	 * confirmString = "You're not following " + mUser.getName() + " anymore";
	 * }
	 * } catch (SmartMapClientException e) {
	 * confirmString = e.getMessage();
	 * }
	 * return confirmString;
	 * }
	 * @Override
	 * protected void onPostExecute(String confirmString) {
	 * Toast.makeText(UserInformationActivity.this, confirmString, Toast.LENGTH_SHORT).show();
	 * }
	 * }
	 */

	@Override
	protected void onResume() {
		super.onResume();
		// Get User & Database TODO set a listener on the user
		mUser = Cache.getInstance().getUserById(this.getIntent().getLongExtra("USER", User.NO_ID));
		mDistanceToUser = Math.round(SettingsManager.getInstance().getLocation()
		    .distanceTo(mUser.getLocation()));

		// Set Informations
		mNameView.setText(mUser.getName());
		mSubtitlesView.setText(mUser.getSubtitle());
		mPictureView.setImageBitmap(mUser.getImage());
		mFollowSwitch.setChecked(isFollowing);
		mBlockSwitch.setChecked(isBlocked);
		mDistanceView.setText(mDistanceToUser + " meters away from you");
		isFollowing = mUser.isVisible();
	}
}

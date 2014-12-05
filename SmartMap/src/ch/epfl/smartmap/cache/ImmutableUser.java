package ch.epfl.smartmap.cache;

import android.graphics.Bitmap;
import android.location.Location;

/**
 * Immutable implementation of User, which serves only as container purposes, therefore all set methods are
 * disabled.
 * 
 * @author jfperren
 */
public final class ImmutableUser {

    // User informations
    private final long mId;
    private final String mName;
    private final String mPhoneNumber;
    private final String mEmail;
    private final String mLocationString;
    private final Location mLocation;
    private final Bitmap mImage;
    private final boolean mIsBlocked;

    /**
     * Constructor, put {@code null} (or {@code User.NO_ID} for id) if you dont want the value to be taken
     * into account.
     * 
     * @param id
     * @param name
     * @param phoneNumber
     * @param email
     * @param location
     * @param locationString
     * @param image
     */
    public ImmutableUser(long id, String name, String phoneNumber, String email, Location location,
        String locationString, Bitmap image, boolean isBlocked) {

        mId = id;
        mName = name;
        mPhoneNumber = phoneNumber;
        mEmail = email;
        mLocation = location;
        mLocationString = locationString;
        mImage = image;
        mIsBlocked = isBlocked;
    }

    public String getEmail() {
        return mEmail;
    }

    public long getId() {
        return mId;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public Location getLocation() {
        return mLocation;
    }

    public String getLocationString() {
        return mLocationString;
    }

    public String getName() {
        return mName;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public Boolean isBlocked() {
        return mIsBlocked;
    }
}

package ch.epfl.smartmap.cache;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Objects that can be displayed on the bottom menu
 * 
 * @author ritterni
 */
public interface Displayable {

    /**
     * @return A name for the panel (e.g. the username, event name, etc.)
     */
    String getName();

    /**
     * @param context
     *            The application's context, needed to access the memory
     * @return The object's picture
     */
    Bitmap getPicture(Context context);

    /**
     * @return Text containing various information (description, last seen,
     *         etc.)
     */
    String getShortInfos();
}

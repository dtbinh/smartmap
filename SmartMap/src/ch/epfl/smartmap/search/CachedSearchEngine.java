package ch.epfl.smartmap.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.location.Location;
import android.os.AsyncTask;
import ch.epfl.smartmap.cache.Cache;
import ch.epfl.smartmap.cache.Displayable;
import ch.epfl.smartmap.cache.Event;
import ch.epfl.smartmap.cache.History;
import ch.epfl.smartmap.cache.ImmutableEvent;
import ch.epfl.smartmap.cache.ImmutableUser;
import ch.epfl.smartmap.cache.User;
import ch.epfl.smartmap.database.DatabaseHelper;
import ch.epfl.smartmap.servercom.NetworkSmartMapClient;
import ch.epfl.smartmap.servercom.SmartMapClientException;

/**
 * @author jfperren
 */
public final class CachedSearchEngine implements SearchEngine {

    private static final CachedSearchEngine ONE_INSTANCE = new CachedSearchEngine();

    private static final float LOCATION_DISTANCE_THRESHHOLD = 0;

    private final Map<String, Set<Long>> mPreviousOnlineStrangerSearches;
    private final List<Location> mPreviousOnlineEventSearchQueries;
    private final List<Set<Long>> mPreviousOnlineEventSearchResults;

    private CachedSearchEngine() {
        mPreviousOnlineStrangerSearches = new HashMap<String, Set<Long>>();

        mPreviousOnlineEventSearchQueries = new LinkedList<Location>();
        mPreviousOnlineEventSearchResults = new LinkedList<Set<Long>>();
    }

    /**
     * Search for a {@code Friend} with this id. For performance concerns, this method should be called in an
     * {@code AsyncTask}.
     * 
     * @param id
     *            long id of {@code Friend}
     * @return the {@code Friend} with given id, or {@code null} if there was no match.
     */
    public void findFriendById(final long id, final SearchRequestCallback<User> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Check for live instance
                User friend = Cache.getInstance().getFriend(id);

                if (friend != null) {
                    // Found in cache, return
                    callback.onResult(friend);
                    return null;
                } else {
                    // If not found, check in database
                    ImmutableUser databaseResult = DatabaseHelper.getInstance().getFriend(id);

                    if (databaseResult != null) {
                        // Match in database, put it in cache
                        Cache.getInstance().putFriend(databaseResult);
                        callback.onResult(Cache.getInstance().getFriend(id));
                        return null;
                    } else {
                        // If not found, check on the server
                        ImmutableUser networkResult;
                        try {
                            networkResult = NetworkSmartMapClient.getInstance().getUserInfo(id);
                        } catch (SmartMapClientException e) {
                            networkResult = null;
                        }

                        if (networkResult != null) {
                            // Match on server, put it in cache
                            Cache.getInstance().putFriend(networkResult);
                            callback.onResult(Cache.getInstance().getFriend(id));
                            return null;
                        } else {
                            callback.onNotFound();
                            return null;
                        }
                    }
                }
            }
        }.execute();
    }

    public void findFriendsByIds(final Set<Long> ids, final SearchRequestCallback callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Set<ImmutableUser> immutableResult = new HashSet<ImmutableUser>();
                Set<User> result = new HashSet<User>();

                for (long id : ids) {
                    // Check for live instance
                    User friend = Cache.getInstance().getFriend(id);

                    if (friend != null) {
                        // Found in cache, add to set of live instances
                        result.add(friend);
                    } else {
                        // If not found, check on the server
                        ImmutableUser networkResult;
                        try {
                            networkResult = NetworkSmartMapClient.getInstance().getUserInfo(id);
                        } catch (SmartMapClientException e) {
                            networkResult = null;
                        }

                        if (networkResult != null) {
                            // Match on server, put it in cache
                            immutableResult.add(networkResult);
                        }
                    }
                }

                // Get all results that weren't in cache and add them all at once (Avoid to send multiple
                // listener
                // calls)
                Cache.getInstance().putFriends(immutableResult);
                // Retrieve live instances from cache
                for (ImmutableUser user : immutableResult) {
                    result.add(Cache.getInstance().getFriend(user.getId()));
                }

                callback.onResult(result);
                return null;
            }
        }.execute();
    }

    /**
     * Search for a {@code Friend} with this id. For performance concerns, this method should be called in an
     * {@code AsyncTask}.
     * 
     * @param id
     *            long id of {@code Friend}
     * @return the {@code Friend} with given id, or {@code null} if there was no match.
     */
    public void findPublicEventById(final long id, final SearchRequestCallback<Event> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Check for live instance
                Event event = Cache.getInstance().getPublicEvent(id);

                if (event != null) {
                    // Found in cache, return
                    callback.onResult(event);
                    return null;
                } else {
                    // If not found, check in database
                    ImmutableEvent databaseResult = DatabaseHelper.getInstance().getEvent(id);

                    if (databaseResult != null) {
                        // Match in database, put it in cache
                        Cache.getInstance().putPublicEvent((databaseResult));
                        callback.onResult(Cache.getInstance().getPublicEvent(id));
                        return null;
                    } else {
                        // If not found, check on the server
                        ImmutableEvent networkResult;
                        try {
                            networkResult = NetworkSmartMapClient.getInstance().getEventInfo(id);
                        } catch (SmartMapClientException e) {
                            networkResult = null;
                        }

                        if (networkResult != null) {
                            // Match on server, put it in cache
                            Cache.getInstance().putPublicEvent(networkResult);
                            callback.onResult(Cache.getInstance().getPublicEvent(id));
                            return null;
                        } else {
                            // No match anywhere
                            callback.onNotFound();
                            return null;
                        }
                    }
                }
            }
        }.execute();
    }

    /**
     * Return an Event set containing a live instance for each id in the id set
     * 
     * @param ids
     * @return
     */
    public void findPublicEventByIds(final Set<Long> ids, final SearchRequestCallback<Set<Event>> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Set<ImmutableEvent> immutableResult = new HashSet<ImmutableEvent>();
                Set<Event> result = new HashSet<Event>();

                for (long id : ids) {
                    // Check for live instance
                    Event event = Cache.getInstance().getPublicEvent(id);

                    if (event != null) {
                        // Found in cache, add to set of live instances
                        result.add(event);
                    } else {
                        // If not found, check in database
                        ImmutableEvent databaseResult = DatabaseHelper.getInstance().getEvent(id);

                        if (databaseResult != null) {
                            immutableResult.add(databaseResult);
                        } else {
                            // If not found, check on the server
                            ImmutableEvent networkResult;
                            try {
                                networkResult = NetworkSmartMapClient.getInstance().getEventInfo(id);
                            } catch (SmartMapClientException e) {
                                networkResult = null;
                            }

                            if (networkResult != null) {
                                // Match on server, put it in cache
                                immutableResult.add(networkResult);
                            }
                        }
                    }
                }

                // Get all results that weren't in cache and add them all at once (Avoid to send multiple
                // listener
                // calls)
                Cache.getInstance().putPublicEvents(immutableResult);
                // Retrieve live instances from cache
                for (ImmutableEvent event : immutableResult) {
                    result.add(Cache.getInstance().getPublicEvent(event.getID()));
                }

                callback.onResult(result);
                return null;
            }
        }.execute();
    }

    /**
     * Search for a {@code Friend} with this id. For performance concerns, this method should be called in an
     * {@code AsyncTask}.
     * 
     * @param id
     *            long id of {@code Friend}
     * @return the {@code Friend} with given id, or {@code null} if there was no match.
     */
    public void findStrangerById(final long id, final SearchRequestCallback<User> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Check for live instance
                User stranger = Cache.getInstance().getStranger(id);

                if (stranger != null) {
                    // Found in cache, return
                    callback.onResult(stranger);
                    return null;
                } else {
                    // If not found, check in database
                    ImmutableUser databaseResult = DatabaseHelper.getInstance().getFriend(id);

                    if (databaseResult != null) {
                        // Match in database, put it in cache
                        Cache.getInstance().putFriend(databaseResult);
                        callback.onResult(Cache.getInstance().getStranger(id));
                        return null;
                    } else {
                        // If not found, check on the server
                        ImmutableUser networkResult;
                        try {
                            networkResult = NetworkSmartMapClient.getInstance().getUserInfo(id);
                            if (networkResult != null) {
                                // Match on server, put it in cache
                                Cache.getInstance().putFriend(networkResult);
                                callback.onResult(Cache.getInstance().getStranger(id));
                                return null;
                            } else {
                                // No match anywhere
                                callback.onNotFound();
                                return null;
                            }
                        } catch (SmartMapClientException e) {
                            callback.onNetworkError();
                            return null;
                        }
                    }
                }
            }
        }.execute();
    }

    public void findStrangerByQuery(final String query, final SearchRequestCallback<Set<User>> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Set<User> result = new HashSet<User>();

                if (mPreviousOnlineStrangerSearches.get(query) != null) {
                    // Fetch in cache
                    Set<Long> localResult = mPreviousOnlineStrangerSearches.get(query);
                    for (Long id : localResult) {
                        User cachedUser = Cache.getInstance().getStranger(id);
                        if (cachedUser != null) {
                            result.add(cachedUser);
                        }
                    }
                } else {
                    // Fetch online
                    Set<ImmutableUser> networkResult;
                    try {
                        networkResult =
                            new HashSet<ImmutableUser>(NetworkSmartMapClient.getInstance().findUsers(query));
                        for (ImmutableUser user : networkResult) {
                            User cachedUser = Cache.getInstance().getStranger(user.getId());
                            if (cachedUser != null) {
                                result.add(cachedUser);
                            }
                        }
                    } catch (SmartMapClientException e) {
                        callback.onNetworkError();
                    }
                }
                callback.onResult(result);
                return null;
            }
        }.execute();
    }

    /**
     * Search for a {@code Friend} with this id. For performance concerns, this method should be called in an
     * {@code AsyncTask}.
     * 
     * @param id
     *            long id of {@code Friend}
     * @return the {@code Friend} with given id, or {@code null} if there was no match.
     */
    public void findUserById(final long id, final SearchRequestCallback<User> callback) {
        // Check in cache
        User user = Cache.getInstance().getUser(id);
        if (user != null) {
            // Found
            callback.onResult(user);
        } else {
            this.findStrangerById(id, callback);
        }
    }

    public void findUserByIds(final Set<Long> ids, final SearchRequestCallback<Set<User>> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Set<ImmutableUser> immutableResult = new HashSet<ImmutableUser>();
                Set<User> result = new HashSet<User>();

                for (long id : ids) {
                    // Check for live instance
                    User stranger = Cache.getInstance().getStranger(id);

                    if (stranger != null) {
                        // Found in cache, add to set of live instances
                        result.add(stranger);
                    } else {
                        // If not found, check on the server
                        ImmutableUser networkResult;
                        try {
                            networkResult = NetworkSmartMapClient.getInstance().getUserInfo(id);
                        } catch (SmartMapClientException e) {
                            networkResult = null;
                        }

                        if (networkResult != null) {
                            // Match on server, put it in cache
                            immutableResult.add(networkResult);
                        }
                    }
                }

                Cache.getInstance().putStrangers(immutableResult);
                // Retrieve live instances from cache
                for (ImmutableUser user : immutableResult) {
                    result.add(Cache.getInstance().getStranger(user.getId()));
                }

                // Give results to the caller
                callback.onResult(result);
                return null;
            }
        }.execute();
    }

    public void getAllNearEvents(final Location location, final double radius,
        final SearchRequestCallback<Set<Event>> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Search ids of nearby
                Set<Long> resultIds = new HashSet<Long>();

                // Look in cache
                boolean foundInCache = false;
                for (int i = 0; i < mPreviousOnlineEventSearchQueries.size(); i++) {
                    if (mPreviousOnlineEventSearchQueries.get(i).distanceTo(location) < LOCATION_DISTANCE_THRESHHOLD) {
                        resultIds = mPreviousOnlineEventSearchResults.get(i);
                        foundInCache = true;
                    }
                }

                if (!foundInCache) {
                    // Search online
                    try {
                        resultIds =
                            new HashSet<Long>(NetworkSmartMapClient.getInstance().getPublicEvents(
                                location.getLatitude(), location.getLongitude(), radius));
                    } catch (SmartMapClientException e) {
                        callback.onNetworkError();
                    }
                }

                // Search events
                CachedSearchEngine.this.findPublicEventByIds(resultIds, callback);
                return null;
            }
        }.execute();
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.search.SearchEngine#getHistory()
     */
    @Override
    public History getHistory() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.smartmap.cache.SearchEngine#sendQuery(java.lang.String,
     * ch.epfl.smartmap.cache.SearchEngine.Type)
     */
    @Override
    public List<Displayable> sendQuery(String query, Type searchType) {
        query = query.toLowerCase(Locale.US);
        ArrayList<Displayable> results = new ArrayList<Displayable>();

        switch (searchType) {
            case ALL:
                results.addAll(this.sendQuery(query, Type.FRIENDS));
                results.addAll(this.sendQuery(query, Type.EVENTS));
                results.addAll(this.sendQuery(query, Type.TAGS));
                break;
            case FRIENDS:
                for (User f : Cache.getInstance().getAllFriends()) {
                    if (f.getName().toLowerCase(Locale.US).contains(query)) {
                        results.add(f);
                    }
                }
                break;
            case EVENTS:
                for (Event e : Cache.getInstance().getAllEvents()) {
                    if (e.getName().toLowerCase(Locale.US).contains(query)) {
                        results.add(e);
                    }
                }
                break;
            case TAGS:

                break;
            default:
                break;
        }
        return results;
    }

    public static CachedSearchEngine getInstance() {
        return ONE_INSTANCE;
    }
}

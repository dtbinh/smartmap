package ch.epfl.smartmap.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.os.AsyncTask;
import android.util.LongSparseArray;
import ch.epfl.smartmap.background.SettingsManager;
import ch.epfl.smartmap.database.DatabaseHelper;
import ch.epfl.smartmap.listeners.CacheListener;
import ch.epfl.smartmap.servercom.NetworkRequestCallback;
import ch.epfl.smartmap.servercom.NetworkSmartMapClient;
import ch.epfl.smartmap.servercom.SmartMapClient;
import ch.epfl.smartmap.servercom.SmartMapClientException;

/**
 * The Cache contains all instances of network objects that are used by the GUI. Therefore, every request to
 * find an
 * user or an event should go through it. It will automatically fill itself with the database on creation, and
 * then
 * updates the database as changes are made.
 * 
 * @author jfperren
 */
public class Cache {

    static final public String TAG = "Cache";

    // Unique instance
    private static final Cache ONE_INSTANCE = new Cache();

    // Other members of the data hierarchy
    private final DatabaseHelper mDatabaseHelper;
    private final NetworkSmartMapClient mNetworkClient;

    // Sets containing ids of all Friends and stored public events
    private final Set<Long> mFriendIds;
    private final Set<Long> mEventIds;
    private final Set<Long> mFilterIds;

    // SparseArrays containing live instances
    private final LongSparseArray<Event> mEventInstances;
    private final LongSparseArray<User> mUserInstances;
    private final LongSparseArray<Filter> mFilterInstances;

    // Listeners
    private final List<CacheListener> mListeners;

    private Cache() {
        // Init data hierarchy
        mDatabaseHelper = DatabaseHelper.getInstance();
        mNetworkClient = NetworkSmartMapClient.getInstance();

        // Init lists
        mFriendIds = new HashSet<Long>();
        mEventIds = new HashSet<Long>();
        mFilterIds = new HashSet<Long>();

        mEventInstances = new LongSparseArray<Event>();
        mUserInstances = new LongSparseArray<User>();
        mFilterInstances = new LongSparseArray<Filter>();

        mListeners = new LinkedList<CacheListener>();
    }

    // Removes all instances that are not used anymore
    public void cleanInstances() {

    }

    public void createEvent(final ImmutableEvent createdEvent, final NetworkRequestCallback callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    long eventId;
                    eventId = NetworkSmartMapClient.getInstance().createPublicEvent(createdEvent);
                    // Add ID to event
                    createdEvent.setId(eventId);
                    // Puts event in Cache
                    Cache.this.putPublicEvent(createdEvent);
                    callback.onSuccess();
                } catch (SmartMapClientException e) {
                    callback.onFailure();
                }
                return null;
            }
        }.execute();
    }

    public List<Event> getAllEvents() {
        List<Event> result = new ArrayList<Event>();
        for (long id : mEventIds) {
            Event event = mEventInstances.get(id);
            if (event != null) {
                result.add(event);
            } else {
                assert false;
            }
        }

        return result;
    }

    /**
     * @return a list containing all the user's Friends.
     */
    public List<User> getAllFriends() {
        List<User> allFriends = new ArrayList<User>();
        for (Long id : mFriendIds) {
            User friend = mUserInstances.get(id);
            if (friend != null) {
                allFriends.add(friend);
            } else {
                assert false;
            }
        }
        return allFriends;
    }

    /**
     * @return a list containing all the user's Going Events.
     */
    public List<Event> getAllGoingEvents() {
        List<Event> allGoingEvents = new ArrayList<Event>();
        long myId = SettingsManager.getInstance().getUserID();
        for (Long id : mEventIds) {
            Event event = mEventInstances.get(id);
            if ((event != null) && event.getParticipants().contains(myId)) {
                allGoingEvents.add(event);
            } else {
                assert false;
            }
        }
        return allGoingEvents;
    }

    public List<Displayable> getAllVisibleEvents() {
        List<Displayable> allVisibleEvents = new ArrayList<Displayable>();
        for (Long id : mEventIds) {
            Event event = mEventInstances.get(id);
            if ((event != null) && event.isVisible()) {
                allVisibleEvents.add(event);
            }
        }
        return allVisibleEvents;
    }

    public List<Displayable> getAllVisibleFriends() {
        List<Displayable> allVisibleUsers = new ArrayList<Displayable>();
        for (Long id : mFriendIds) {
            User user = mUserInstances.get(id);
            if ((user != null) && user.isVisible()) {
                allVisibleUsers.add(user);
            }
        }
        return allVisibleUsers;
    }

    public User getFriend(long id) {
        return mUserInstances.get(id);
    }

    public Set<User> getFriends(Set<Long> ids) {
        Set<User> friends = new HashSet<User>();
        for (long id : ids) {
            User friend = this.getStranger(id);
            if (friend != null) {
                friends.add(friend);
            }
        }
        return friends;
    }

    public Event getPublicEvent(long id) {
        return mEventInstances.get(id);
    }

    public Set<Event> getPublicEvents(Set<Long> ids) {
        Set<Event> events = new HashSet<Event>();
        for (long id : ids) {
            Event event = this.getPublicEvent(id);
            if (event != null) {
                events.add(event);
            }
        }
        return events;
    }

    public User getStranger(long id) {
        return mUserInstances.get(id);
    }

    public Set<User> getStrangers(Set<Long> ids) {
        Set<User> strangers = new HashSet<User>();
        for (long id : ids) {
            User stranger = this.getStranger(id);
            if (stranger != null) {
                strangers.add(stranger);
            }
        }
        return strangers;
    }

    public User getUser(long id) {
        User user = this.getFriend(id);
        if (user == null) {
            user = this.getStranger(id);
        }
        return user;
    }

    public Set<User> getUsers(Set<Long> ids) {
        Set<User> users = new HashSet<User>();
        for (long id : ids) {
            User user = this.getFriend(id);
            if (user == null) {
                user = this.getStranger(id);
            }
            if (user != null) {
                users.add(user);
            }
        }

        return users;
    }

    public void initFromDatabase(DatabaseHelper database) {
        // Clear previous values
        mEventInstances.clear();
        mUserInstances.clear();
        mUserInstances.clear();

        // Clear lists
        mFriendIds.clear();
        mEventIds.clear();

        // Initialize id Lists
        mFriendIds.addAll(database.getFriendIds());
        // mPublicEventIds.addAll(DatabaseHelper.getInstance().getEventIds());

        // Fill with database values
        for (long id : mFriendIds) {
            mUserInstances.put(id, new Friend(database.getFriend(id)));
        }
        for (long id : mEventIds) {
            mEventInstances.put(id, new PublicEvent(database.getEvent(id)));
        }

        // Notify listeners
        for (CacheListener listener : mListeners) {
            listener.onEventListUpdate();
            listener.onFriendListUpdate();
        }
    }

    public void putFilters(Set<ImmutableFilter> newFilters) {
        boolean isListModified = false;

        for (ImmutableFilter newFilter : newFilters) {
            if (mFilterInstances.get(newFilter.getId()) == null) {
                // Need to add it
                mFilterIds.add(newFilter.getId());
                mFilterInstances.put(newFilter.getId(), new Filter(newFilter));
                isListModified = true;
            } else {
                // Only update
                this.updateFilter(newFilter);
            }
        }

        // Notify listeners if needed
        if (isListModified) {
            for (CacheListener listener : mListeners) {
                listener.onFilterListUpdate();
            }
        }
    }

    /**
     * Add a Friend, and fill the cache with its informations.
     * 
     * @param id
     */
    public void putFriend(ImmutableUser newFriend) {
        Set<ImmutableUser> singleton = new HashSet<ImmutableUser>();
        singleton.add(newFriend);
        this.putFriends(singleton);
    }

    public void putFriends(Set<ImmutableUser> newFriends) {
        boolean isListModified = false;

        for (ImmutableUser newFriend : newFriends) {
            if (mUserInstances.get(newFriend.getId()) == null) {
                // Need to add it
                mFriendIds.add(newFriend.getId());
                mUserInstances.put(newFriend.getId(), new Friend(newFriend));
                isListModified = true;
            } else {
                // Only update
                this.updateFriend(newFriend);
            }
        }

        // Notify listeners if needed
        if (isListModified) {
            for (CacheListener listener : mListeners) {
                listener.onFriendListUpdate();
            }
        }
    }

    /**
     * Mark an Event as Going and fill the cache with its informations.
     * 
     * @param id
     */
    public void putPublicEvent(ImmutableEvent newEvent) {
        Set<ImmutableEvent> singleton = new HashSet<ImmutableEvent>();
        singleton.add(newEvent);
        this.putPublicEvents(singleton);
    }

    public void putPublicEvents(Set<ImmutableEvent> newEvents) {
        boolean isListModified = false;

        for (ImmutableEvent newEvent : newEvents) {
            if (mEventInstances.get(newEvent.getID()) == null) {
                // Need to add it
                Event event = new PublicEvent(newEvent);
                mEventInstances.put(newEvent.getID(), event);

                isListModified = true;
            } else {
                // Only update
                this.updateEvent(newEvent);
            }
        }

        // Notify listeners if needed
        if (isListModified) {
            for (CacheListener listener : mListeners) {
                listener.onEventListUpdate();
            }
        }
    }

    /**
     * Fill Cache with an unknown User's informations.
     * 
     * @param user
     */
    public void putStranger(ImmutableUser newStranger) {
        Set<ImmutableUser> singleton = new HashSet<ImmutableUser>();
        singleton.add(newStranger);
        this.putStrangers(singleton);
    }

    /**
     * Put Strangers in Cache to be reused later.
     * 
     * @param newStrangers
     */
    public void putStrangers(Set<ImmutableUser> newStrangers) {
        for (ImmutableUser newStranger : newStrangers) {
            if (mUserInstances.get(newStranger.getId()) == null) {
                // Need to add it
                mUserInstances.put(newStranger.getId(), new Friend(newStranger));
            } else {
                // Only update
                mUserInstances.get(newStranger.getId()).update(newStranger);
            }
        }
    }

    public void removeFriend(long id) {
        mFriendIds.remove(id);

        // Notify listeners
        for (CacheListener l : mListeners) {
            l.onFriendListUpdate();
        }
    }

    public void updateEvent(ImmutableEvent event) {
        // Check in cache
        Event cachedEvent = mEventInstances.get(event.getID());
        if (cachedEvent != null) {
            // In cache
            cachedEvent.update(event);
        }
    }

    public void updateEvents(Set<ImmutableUser> users) {
        for (ImmutableUser user : users) {
            this.updateFriend(user);
        }
    }

    public void updateFriend(ImmutableUser user) {
        // Check in cache
        User cachedFriend = mUserInstances.get(user.getId());
        if (cachedFriend != null) {
            // In cache
            cachedFriend.update(user);
        }
    }

    public void updateFriends(Set<ImmutableEvent> events) {
        for (ImmutableEvent event : events) {
            this.updateEvent(event);
        }
    }

    public void updateFromNetwork(SmartMapClient networkClient) throws SmartMapClientException {
        // TODO : Empty useless instances from Cache

        // Fetch friend ids
        HashSet<Long> newFriendIds = new HashSet<Long>(networkClient.getFriendsIds());

        // Remove friends that are no longer friends
        for (long id : mFriendIds) {
            if (!newFriendIds.contains(id)) {
                this.removeFriend(id);
            }
        }

        // Sets new friend ids
        mFriendIds.clear();
        mFriendIds.addAll(newFriendIds);

        // Update each friends
        for (long id : newFriendIds) {
            User friend = this.getFriend(id);

            // Get online values
            ImmutableUser onlineValues = networkClient.getUserInfo(id);
            if (friend != null) {
                // Simply update
                friend.update(onlineValues);
            } else {
                // Add friend
                this.putFriend(onlineValues);
            }
        }

        // TODO : Update Events
    }

    public void updateOwnEvent(final ImmutableEvent createdEvent, final NetworkRequestCallback callback) {
        // TODO :
    }

    public boolean updatePublicEvent(ImmutableEvent event) {
        // Check in cache
        Event cachedEvent = mEventInstances.get(event.getID());

        if (cachedEvent == null) {
            // Not in cache
            cachedEvent = new PublicEvent(event);
            mEventInstances.put(event.getID(), cachedEvent);
            return true;
        } else {
            // In cache
            cachedEvent.update(event);
            return false;
        }
    }

    public static Cache getInstance() {
        return ONE_INSTANCE;
    }
}
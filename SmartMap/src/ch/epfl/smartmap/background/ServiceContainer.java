package ch.epfl.smartmap.background;

import android.content.Context;
import ch.epfl.smartmap.cache.Cache;
import ch.epfl.smartmap.database.DatabaseHelper;
import ch.epfl.smartmap.database.DatabaseHelperInterface;
import ch.epfl.smartmap.search.CachedSearchEngine;
import ch.epfl.smartmap.servercom.NetworkSmartMapClient;
import ch.epfl.smartmap.servercom.SmartMapClient;

/**
 * This class is a container for the different services used by the SmartMap
 * app. It's member must be manually
 * set at
 * the starting of the app. It provides a common interface to get services and
 * thus simplifies future changes.
 * It also
 * allows to switch the returned instances for behavior modification and
 * testing.
 * 
 * @author Pamoi
 */
public final class ServiceContainer {
    private static SmartMapClient mNetworkClient;
    private static DatabaseHelperInterface mDBHelper;
    private static Cache mCache;
    private static CachedSearchEngine mSearchEngine;
    private static SettingsManager mSettingsManager;

    /**
     * Private constructor that hides implicit public one.
     */
    private ServiceContainer() {
        super();
    }

    /**
     * Initializes the services, overwriting any existing ones
     * 
     * @param context
     *            The app's context
     */
    public static void forceInitSmartMapServices(Context context) {
        setSettingsManager(new SettingsManager(context));
        setNetworkClient(new NetworkSmartMapClient());
        setDatabaseHelper(new DatabaseHelper(context));
        setCache(new Cache());
        setSearchEngine(new CachedSearchEngine());
    }

    /**
     * Get the cache service.
     * 
     * @return Cache
     */
    public static Cache getCache() {
        return mCache;
    }

    /**
     * Get the database helper service.
     * 
     * @return DatabaseHelper
     */
    public static DatabaseHelperInterface getDatabase() {
        return mDBHelper;
    }

    /**
     * Get the network client service.
     * 
     * @return SmartMapClient
     */
    public static SmartMapClient getNetworkClient() {
        return mNetworkClient;
    }

    /**
     * Get the search engine service.
     * 
     * @return CachedSearchEngine
     */
    public static CachedSearchEngine getSearchEngine() {
        return mSearchEngine;
    }

    /**
     * Get the settings manager service.
     * 
     * @return SettingsManager
     */
    public static SettingsManager getSettingsManager() {
        return mSettingsManager;
    }

    /**
     * Initializes the uninitialized services
     * 
     * @param context
     *            The app's context
     */
    public static void initSmartMapServices(Context context) {
        if (ServiceContainer.getSettingsManager() == null) {
            setSettingsManager(new SettingsManager(context));
        }
        if (ServiceContainer.getNetworkClient() == null) {
            setNetworkClient(new NetworkSmartMapClient());
        }
        if (ServiceContainer.getDatabase() == null) {
            setDatabaseHelper(new DatabaseHelper(context));
        }
        if (ServiceContainer.getCache() == null) {
            setCache(new Cache());
        }
        if (ServiceContainer.getSearchEngine() == null) {
            setSearchEngine(new CachedSearchEngine());
        }
    }

    /**
     * Set the cache service.
     * 
     * @param cache
     */
    public static void setCache(Cache cache) {
        mCache = cache;
    }

    /**
     * Set the database helper service.
     * 
     * @param db
     */
    public static void setDatabaseHelper(DatabaseHelperInterface db) {
        mDBHelper = db;
    }

    /**
     * Set the network client service.
     * 
     * @param client
     */
    public static void setNetworkClient(SmartMapClient client) {
        mNetworkClient = client;
    }

    /**
     * Set the search engine service.
     * 
     * @param se
     */
    public static void setSearchEngine(CachedSearchEngine se) {
        mSearchEngine = se;
    }

    /**
     * Set the settings manager service.
     * 
     * @param sm
     */
    public static void setSettingsManager(SettingsManager sm) {
        mSettingsManager = sm;
    }
}

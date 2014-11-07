package ch.epfl.smartmap.test.cache;

import java.util.GregorianCalendar;

import org.junit.Test;

import android.location.Location;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.LongSparseArray;
import ch.epfl.smartmap.cache.DatabaseHelper;
import ch.epfl.smartmap.cache.Friend;
import ch.epfl.smartmap.cache.FriendList;
import ch.epfl.smartmap.cache.User;
import ch.epfl.smartmap.cache.UserEvent;

/**
 * Tests for the DatabaseHelper class
 * @author ritterni
 */
public class DatabaseHelperTest extends AndroidTestCase {
    
    private final String name = "test name";
    private Friend a = new Friend(1234, "qwertz uiop");
    private Friend b = new Friend(0, "hcjkehfkl");
    private Friend c = new Friend(9909, "Abc Def");
    private final UserEvent event = new UserEvent("A new event", 1234, "qwertz uiop",
            new GregorianCalendar(), new GregorianCalendar(), new Location("SmartMapProvider"));
    private final UserEvent event2 = new UserEvent("Another new event", 4523, "abababab",
            new GregorianCalendar(), new GregorianCalendar(), new Location("SmartMapProvider"));
    private DatabaseHelper dbh;
    private FriendList filter;
    private FriendList filter2;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dbh = new DatabaseHelper(new RenamingDelegatingContext(getContext(), "test_"));
        //to avoid erasing the actual database
        
        filter = new FriendList(name);
        filter.addUser(a.getID());
        filter.addUser(b.getID());
        filter.addUser(c.getID());
        
        filter2 = new FriendList(name);
        filter.addUser(b.getID());
        filter.addUser(c.getID());
        
        dbh.clearAll();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        dbh.clearAll();
        dbh.close();
    }
    
    @Test
    public void testAddUser() {
        dbh.addUser(a);
        assertTrue(dbh.getUser(a.getID()).getID() == a.getID() 
                && dbh.getUser(a.getID()).getName().equals(a.getName())
                && dbh.getUser(a.getID()).getNumber().equals(a.getNumber())
                && dbh.getUser(a.getID()).getEmail().equals(a.getEmail())
                && dbh.getUser(a.getID()).getPositionName().equals(a.getPositionName())
                && dbh.getUser(a.getID()).getLocation().getLongitude() == a.getLocation().getLongitude()
                && dbh.getUser(a.getID()).getLocation().getLatitude() == a.getLocation().getLatitude());
    }
    
    @Test
    public void testGetAllUsers() {
        dbh.addUser(a);
        dbh.addUser(b);
        dbh.addUser(c);
        LongSparseArray<User> list = dbh.getAllUsers();
        assertTrue(list.get(c.getID()).getID() == c.getID());
    }
    
    @Test
    public void testUpdateUser() {
        dbh.addUser(a);
        dbh.addUser(b);
        a.setName(name);
        int rows = dbh.updateUser(a);
        assertTrue(dbh.getUser(a.getID()).getName().equals(name) && rows == 1);
    }
    
    @Test
    public void testDeleteUser() {
        dbh.addUser(a);
        dbh.addUser(b);
        dbh.addUser(c);
        dbh.deleteUser(b.getID());
        LongSparseArray<User> list = dbh.getAllUsers();
        assertTrue(list.size() == 2 && list.get(c.getID()).getID() == c.getID());
    }
    
    @Test
    public void testAddFilter() {
        long id = dbh.addFilter(filter);
        assertTrue(dbh.getFilter(id).getListName().equals(filter.getListName())
                && dbh.getFilter(id).getList().contains(b.getID()));
    }
    
    @Test
    public void testGetAllFilters() {
    	dbh.addFilter(filter);
    	dbh.addFilter(filter2);
    	assertTrue(dbh.getAllFilters().size() == 2);
    }

    @Test
    public void testDeleteFilter() {
        dbh.addFilter(filter);
        dbh.deleteFilter(filter.getID());
        assertTrue(dbh.getAllFilters().isEmpty());
    }
    
    @Test
    public void testAddEvent() {
        event.setID(123123);
        dbh.addEvent(event);
        assertTrue(dbh.getEvent(event.getID()).getCreatorName().equals(event.getCreatorName()));
    }
    
    @Test
    public void testGetAllEvents() {
    	event.setID(123123);
    	event2.setID(456789);
    	dbh.addEvent(event);
    	dbh.addEvent(event2);
    	assertTrue(dbh.getAllEvents().size() == 2);
    }
}

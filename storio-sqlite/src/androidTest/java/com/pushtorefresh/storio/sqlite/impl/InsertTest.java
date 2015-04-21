package com.pushtorefresh.storio.sqlite.impl;

import android.database.Cursor;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class InsertTest extends BaseTest {

    @Test
    public void insertOne() {
        final User user = putUser();

        // why we created StorIOSQLite: nobody loves nulls
        final Cursor cursor = db.query(UserTableMeta.TABLE, null, null, null, null, null, null);

        // asserting that values was really inserted to db
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());

        final User insertedUser = UserTableMeta.GET_RESOLVER.mapFromCursor(cursor);

        assertNotNull(insertedUser.id());
        assertTrue(user.equalsExceptId(insertedUser));

        cursor.close();
    }

    @Test
    public void insertCollection() {
        final List<User> users = putUsers(3);

        // asserting that values was really inserted to db
        final Cursor cursor = db.query(UserTableMeta.TABLE, null, null, null, null, null, null);

        assertEquals(users.size(), cursor.getCount());

        for (int i = 0; i < users.size(); i++) {
            assertTrue(cursor.moveToNext());
            assertEquals(users.get(i), UserTableMeta.GET_RESOLVER.mapFromCursor(cursor));
        }

        cursor.close();
    }

    @Test
    public void insertAndDeleteTwice() {
        final User user = TestFactory.newUser();

        for (int i = 0; i < 2; i++) {
            putUser(user);

            final List<User> existUsers = getAllUsers();

            assertNotNull(existUsers);
            assertEquals(1, existUsers.size());

            final Cursor cursorAfterPut = db.query(UserTableMeta.TABLE, null, null, null, null, null, null);
            assertEquals(1, cursorAfterPut.getCount());
            cursorAfterPut.close();

            deleteUser(user);

            final Cursor cursorAfterDelete = db.query(UserTableMeta.TABLE, null, null, null, null, null, null);
            assertEquals(0, cursorAfterDelete.getCount());
            cursorAfterDelete.close();
        }
    }
}

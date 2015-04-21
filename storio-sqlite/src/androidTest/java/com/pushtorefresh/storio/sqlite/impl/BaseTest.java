package com.pushtorefresh.storio.sqlite.impl;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;

import com.pushtorefresh.storio.sqlite.SQLiteTypeDefaults;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operation.delete.DeleteResult;
import com.pushtorefresh.storio.sqlite.operation.put.PutResult;
import com.pushtorefresh.storio.sqlite.operation.put.PutResults;

import org.junit.Before;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public abstract class BaseTest {

    @NonNull
    protected StorIOSQLite storIOSQLite;

    @NonNull
    protected SQLiteDatabase db;

    @Before
    public void setUp() throws Exception {
        db = new TestSQLiteOpenHelper(InstrumentationRegistry.getContext())
                .getWritableDatabase();

        storIOSQLite = new DefaultStorIOSQLite.Builder()
                .db(db)
                .addDefaultsForType(User.class, new SQLiteTypeDefaults.Builder<User>()
                        .putResolver(UserTableMeta.PUT_RESOLVER)
                        .getResolver(UserTableMeta.GET_RESOLVER)
                        .deleteResolver(UserTableMeta.DELETE_RESOLVER)
                        .build())
                .addDefaultsForType(Tweet.class, new SQLiteTypeDefaults.Builder<Tweet>()
                        .putResolver(TweetTableMeta.PUT_RESOLVER)
                        .getResolver(TweetTableMeta.GET_RESOLVER)
                        .deleteResolver(TweetTableMeta.DELETE_RESOLVER)
                        .build())
                .build();

        // clearing db before each test case
        storIOSQLite
                .delete()
                .byQuery(UserTableMeta.DELETE_QUERY_ALL)
                .prepare()
                .executeAsBlocking();

        storIOSQLite
                .delete()
                .byQuery(TweetTableMeta.DELETE_QUERY_ALL)
                .prepare()
                .executeAsBlocking();
    }

    @NonNull
    List<User> getAllUsers() {
        return storIOSQLite
                .get()
                .listOfObjects(User.class)
                .withQuery(UserTableMeta.QUERY_ALL)
                .prepare()
                .executeAsBlocking();
    }

    @NonNull
    User putUser() {
        final User user = TestFactory.newUser();
        return putUser(user);
    }

    @NonNull
    User putUser(@NonNull final User user) {

        final PutResult putResult = storIOSQLite
                .put()
                .object(user)
                .prepare()
                .executeAsBlocking();

        assertNotNull(putResult);
        assertTrue(putResult.wasInserted());
        return user;
    }

    @NonNull
    List<User> putUsers(final int size) {
        final List<User> users = TestFactory.newUsers(size);
        return putUsers(users);
    }

    @NonNull
    List<User> putUsers(@NonNull final List<User> users) {

        final PutResults<User> putResults = storIOSQLite
                .put()
                .objects(User.class, users)
                .prepare()
                .executeAsBlocking();

        assertEquals(users.size(), putResults.numberOfInserts());

        return users;
    }

    @NonNull
    DeleteResult deleteUser(@NonNull final User user) {
        final DeleteResult deleteResult = storIOSQLite
                .delete()
                .object(user)
                .prepare()
                .executeAsBlocking();

        assertEquals(1, deleteResult.numberOfRowsDeleted());

        return deleteResult;
    }
}

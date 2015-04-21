package com.pushtorefresh.storio.content_resolver.impl;

import android.support.test.runner.AndroidJUnit4;

import com.pushtorefresh.storio.contentresolver.operation.put.PutResult;
import com.pushtorefresh.storio.contentresolver.operation.put.PutResults;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class UpdateTest extends BaseTest {

    @Test
    public void updateOne() {
        final User userForInsert = TestFactory.newUser();

        final PutResult insertResult = storIOContentResolver
                .put()
                .object(userForInsert)
                .prepare()
                .executeAsBlocking();

        assertTrue(insertResult.wasInserted());

        final User userForUpdate = User.newInstance(
                userForInsert.id(), // using id of inserted user
                "new@email.com" // new value
        );

        final PutResult updateResult = storIOContentResolver
                .put()
                .object(userForUpdate)
                .prepare()
                .executeAsBlocking();

        assertTrue(updateResult.wasUpdated());

        oneUserInStorageCheck(userForUpdate);
    }

    @Test
    public void updateCollection() {
        final List<User> usersForInsert = putUsers(3);
        usersInStorageCheck(usersForInsert);

        final List<User> usersForUpdate = new ArrayList<User>(usersForInsert.size());

        for (int i = 0; i < usersForInsert.size(); i++) {
            usersForUpdate.add(User.newInstance(usersForInsert.get(i).id(), "new" + i + "@email.com" + i));
        }

        final PutResults<User> updateResults = storIOContentResolver
                .put()
                .objects(User.class, usersForUpdate)
                .prepare()
                .executeAsBlocking();

        assertEquals(usersForUpdate.size(), updateResults.numberOfUpdates());

        usersInStorageCheck(usersForUpdate);
    }
}

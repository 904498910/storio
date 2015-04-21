package com.pushtorefresh.storio.sqlite.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operation.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio.sqlite.operation.delete.DeleteResolver;
import com.pushtorefresh.storio.sqlite.operation.get.DefaultGetResolver;
import com.pushtorefresh.storio.sqlite.operation.get.GetResolver;
import com.pushtorefresh.storio.sqlite.operation.put.DefaultPutResolver;
import com.pushtorefresh.storio.sqlite.operation.put.PutResolver;
import com.pushtorefresh.storio.sqlite.operation.put.PutResult;
import com.pushtorefresh.storio.sqlite.query.DeleteQuery;
import com.pushtorefresh.storio.sqlite.query.InsertQuery;
import com.pushtorefresh.storio.sqlite.query.Query;
import com.pushtorefresh.storio.sqlite.query.UpdateQuery;

public class UserTableMeta {

    private UserTableMeta() {
        throw new IllegalStateException("No instances please");
    }

    // they are open just for test purposes
    static final String TABLE = "users";
    static final String COLUMN_ID = "_id";
    static final String COLUMN_EMAIL = "email";

    // We all will be very old when Java will support string interpolation :(
    static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY, " +
            COLUMN_EMAIL + " TEXT NOT NULL" +
            ");";

    static final Query QUERY_ALL = new Query.Builder()
            .table(TABLE)
            .build();

    static final DeleteQuery DELETE_QUERY_ALL = new DeleteQuery.Builder()
            .table(TABLE)
            .build();


    static final PutResolver<User> PUT_RESOLVER = new DefaultPutResolver<User>() {
        @NonNull
        @Override
        protected InsertQuery mapToInsertQuery(@NonNull User user) {
            return new InsertQuery.Builder()
                    .table(TABLE)
                    .build();
        }

        @NonNull
        @Override
        protected UpdateQuery mapToUpdateQuery(@NonNull User user) {
            return new UpdateQuery.Builder()
                    .table(TABLE)
                    .where(COLUMN_ID + " = ?")
                    .whereArgs(user.id())
                    .build();
        }

        @NonNull
        @Override
        protected ContentValues mapToContentValues(@NonNull User user) {
            final ContentValues contentValues = new ContentValues(2);

            contentValues.put(COLUMN_ID, user.id());
            contentValues.put(COLUMN_EMAIL, user.email());

            return contentValues;
        }

        @NonNull
        @Override
        public PutResult performPut(@NonNull StorIOSQLite storIOSQLite, @NonNull User object) {
            final PutResult putResult = super.performPut(storIOSQLite, object);

            if (putResult.wasInserted()) {
                object.setId(putResult.insertedId()); // let's think that we need to set id after insert, sometimes it's really required
            }

            return putResult;
        }
    };


    static final GetResolver<User> GET_RESOLVER = new DefaultGetResolver<User>() {
        @NonNull
        @Override
        public User mapFromCursor(@NonNull Cursor cursor) {
            return User.newInstance(
                    cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL))
            );
        }
    };

    static final DeleteResolver<User> DELETE_RESOLVER = new DefaultDeleteResolver<User>() {
        @NonNull
        @Override
        public DeleteQuery mapToDeleteQuery(@NonNull User user) {
            return new DeleteQuery.Builder()
                    .table(TABLE)
                    .where(COLUMN_ID + " = ?")
                    .whereArgs(user.id())
                    .build();
        }
    };
}

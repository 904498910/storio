package com.pushtorefresh.storio.sqlite.operation.delete;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;

/**
 * Defines behavior of Delete Operation
 */
public interface DeleteResolver<T> {

    /**
     * Performs delete of an object
     *
     * @param storIOSQLite {@link StorIOSQLite} instance to perform delete on
     * @param object       object that should be deleted
     * @return non-null result of Delete Operation
     */
    @NonNull
    DeleteResult performDelete(@NonNull StorIOSQLite storIOSQLite, @NonNull T object);
}

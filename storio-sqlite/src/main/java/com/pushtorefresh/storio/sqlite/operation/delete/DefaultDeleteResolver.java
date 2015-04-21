package com.pushtorefresh.storio.sqlite.operation.delete;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.query.DeleteQuery;

/**
 * Default implementation for {@link DeleteResolver}, thread-safe
 */
public abstract class DefaultDeleteResolver<T> implements DeleteResolver<T> {

    /**
     * Converts object to {@link DeleteQuery}
     *
     * @param object object that should be deleted
     * @return {@link DeleteQuery} that will be performed
     */
    @NonNull
    public abstract DeleteQuery mapToDeleteQuery(@NonNull T object);

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public DeleteResult performDelete(@NonNull StorIOSQLite storIOSQLite, @NonNull T object) {
        final DeleteQuery deleteQuery = mapToDeleteQuery(object);
        final int numberOfRowsDeleted = storIOSQLite.internal().delete(deleteQuery);
        return DeleteResult.newInstance(numberOfRowsDeleted, deleteQuery.table);
    }
}

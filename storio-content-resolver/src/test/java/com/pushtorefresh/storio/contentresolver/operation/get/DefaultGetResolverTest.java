package com.pushtorefresh.storio.contentresolver.operation.get;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.query.Query;

import org.junit.Test;

import static junit.framework.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultGetResolverTest {

    private static class TestItem {

    }

    @Test
    public void query() {
        final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
        final StorIOContentResolver.Internal internal = mock(StorIOContentResolver.Internal.class);
        final Query query = mock(Query.class);
        final Cursor expectedCursor = mock(Cursor.class);

        when(storIOContentResolver.internal())
                .thenReturn(internal);

        when(internal.query(query))
                .thenReturn(expectedCursor);

        final GetResolver<TestItem> defaultGetResolver = new DefaultGetResolver<TestItem>() {
            @NonNull
            @Override
            public TestItem mapFromCursor(@NonNull Cursor cursor) {
                assertSame(expectedCursor, cursor);
                return new TestItem();
            }
        };

        final Cursor actualCursor = defaultGetResolver.performGet(storIOContentResolver, query);

        // only one request should occur
        verify(internal, times(1)).query(any(Query.class));

        // and this request should be equals to original
        verify(internal, times(1)).query(query);

        assertSame(expectedCursor, actualCursor);
    }
}

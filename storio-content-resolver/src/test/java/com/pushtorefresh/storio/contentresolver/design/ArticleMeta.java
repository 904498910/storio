package com.pushtorefresh.storio.contentresolver.design;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.contentresolver.operation.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio.contentresolver.operation.delete.DeleteResolver;
import com.pushtorefresh.storio.contentresolver.operation.get.DefaultGetResolver;
import com.pushtorefresh.storio.contentresolver.operation.get.GetResolver;
import com.pushtorefresh.storio.contentresolver.operation.put.DefaultPutResolver;
import com.pushtorefresh.storio.contentresolver.operation.put.PutResolver;
import com.pushtorefresh.storio.contentresolver.query.DeleteQuery;
import com.pushtorefresh.storio.contentresolver.query.InsertQuery;
import com.pushtorefresh.storio.contentresolver.query.UpdateQuery;

import static org.mockito.Mockito.mock;

public class ArticleMeta {

    static final Uri CONTENT_URI = mock(Uri.class);

    static final PutResolver<Article> PUT_RESOLVER = new DefaultPutResolver<Article>() {
        @NonNull
        @Override
        protected InsertQuery mapToInsertQuery(@NonNull Article object) {
            return new InsertQuery.Builder()
                    .uri(CONTENT_URI)
                    .build();
        }

        @NonNull
        @Override
        protected UpdateQuery mapToUpdateQuery(@NonNull Article article) {
            return new UpdateQuery.Builder()
                    .uri(CONTENT_URI)
                    .where(BaseColumns._ID + " = ?")
                    .whereArgs(article.id())
                    .build();
        }

        @NonNull
        @Override
        protected ContentValues mapToContentValues(@NonNull Article object) {
            return mock(ContentValues.class);
        }
    };

    static final GetResolver<Article> GET_RESOLVER = new DefaultGetResolver<Article>() {
        @NonNull
        @Override
        public Article mapFromCursor(@NonNull Cursor cursor) {
            return Article.newInstance(null, null); // in Design tests it does not matter
        }
    };

    static final DeleteResolver<Article> DELETE_RESOLVER = new DefaultDeleteResolver<Article>() {
        @NonNull
        @Override
        protected DeleteQuery mapToDeleteQuery(@NonNull Article article) {
            return new DeleteQuery.Builder()
                    .uri(CONTENT_URI)
                    .where(BaseColumns._ID + " = ?")
                    .whereArgs(article.id())
                    .build();
        }
    };

    private ArticleMeta() {
        throw new IllegalStateException("No instances please");
    }
}

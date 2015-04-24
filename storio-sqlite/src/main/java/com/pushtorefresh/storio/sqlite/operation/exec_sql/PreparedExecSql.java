package com.pushtorefresh.storio.sqlite.operation.exec_sql;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.operation.PreparedOperation;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.query.RawQuery;
import com.pushtorefresh.storio.util.EnvironmentUtil;

import rx.Observable;
import rx.Subscriber;

import static com.pushtorefresh.storio.util.Checks.checkNotNull;

public class PreparedExecSql implements PreparedOperation<Void> {

    @NonNull
    private final StorIOSQLite storIOSQLite;
    @NonNull
    private final RawQuery rawQuery;

    PreparedExecSql(@NonNull StorIOSQLite storIOSQLite, @NonNull RawQuery rawQuery) {
        this.storIOSQLite = storIOSQLite;
        this.rawQuery = rawQuery;
    }

    @NonNull
    @Override
    public Void executeAsBlocking() {
        storIOSQLite.internal().execSql(rawQuery);
        return null;
    }

    @NonNull
    @Override
    public Observable<Void> createObservable() {
        EnvironmentUtil.throwExceptionIfRxJavaIsNotAvailable("createObservable()");

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                executeAsBlocking();

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            }
        });
    }

    /**
     * Builder for {@link PreparedExecSql}
     */
    public static class Builder {

        @NonNull
        private final StorIOSQLite storIOSQLite;

        private RawQuery rawQuery;

        public Builder(@NonNull StorIOSQLite storIOSQLite) {
            this.storIOSQLite = storIOSQLite;
        }

        /**
         * Required: Specifies query for ExecSql Operation
         *
         * @param rawQuery query
         * @return builder
         */
        @NonNull
        public Builder withQuery(@NonNull RawQuery rawQuery) {
            this.rawQuery = rawQuery;
            return this;
        }

        /**
         * Prepares ExecSql Operation
         *
         * @return {@link PreparedExecSql} instance
         */
        @NonNull
        public PreparedExecSql prepare() {
            checkNotNull(rawQuery, "Please set query object");

            return new PreparedExecSql(
                    storIOSQLite,
                    rawQuery
            );
        }
    }
}

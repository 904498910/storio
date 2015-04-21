###StorIOSQLite — API for SQLite Database

####0. Create an instance of StorIOSQLite

```java
StorIOSQLite storIOSQLite = new DefaultStorIOSQLite.Builder()
  .sqliteOpenHelper(yourSqliteOpenHelper) // or .db(db)
  .addTypeDefaults(SomeType.class, typeDefaults) // required for object mapping
  .build();
```

It's a good practice to use one instance of `StorIOSQLite` per database, otherwise you can have problems with notifications about changes in the db.

####1. Get Operation
######Get list of objects with blocking call:

```java
final List<Tweet> tweets = storIOSQLite
  .get()
  .listOfObjects(Tweet.class)
  .withQuery(new Query.Builder()
    .table("tweets")
    .build())
  .prepare()
  .executeAsBlocking();
```

######Get `Cursor` via blocking call:

```java
final Cursor tweetsCursor = storIOSQLite
  .get()
  .cursor()
  .withQuery(new Query.Builder()
    .table("tweets")
    .build())
  .prepare()
  .executeAsBlocking();
```

Things become much more interesting with `RxJava`!

######Get cursor as `Observable`
```java
storIOSQLite
  .get()
  .cursor()
  .withQuery(new Query.Builder()
    .table("tweets")
    .build())
  .prepare()
  .createObservable()
  .subscribeOn(Schedulers.io()) // Execute Get Operation on Background Thread
  .observeOn(AndroidSchedulers.mainThread()) // Observe on Main Thread
  .subscribe(new Action1<Cursor>() {
    @Override public void call(Cursor cursor) {
      // display the data from cursor
      // will be called once
    }
  });
```

#####What if you want to observe changes in `StorIOSQLite`?

######First-case: Receive updates to `Observable` on each change in tables from `Query` 

```java
storIOSQLite
  .get()
  .listOfObjects(Tweet.class)
  .withQuery(new Query.Builder()
    .table("tweets")
    .build())
  .prepare()
  .createObservableStream() // Get Result as rx.Observable and subscribe to further updates of tables from Query!
  .subscribeOn(Schedulers.io())
  .observeOn(AndroidSchedulers.mainThread())
  .subscribe(new Action1<List<Tweet>>() { // don't forget to unsubscribe please
    @Override public void call(List<Tweet> tweets) {
      // will be called with first result and then after each change of tables from Query
      // several changes in transaction -> one notification
      adapter.setData(tweets);
    }
});
// don't forget to manage Subscription and unsubscribe in lifecycle methods to prevent memory leaks
```

######Second case: Handle changes manually

```java
storIOSQLite
  .observeChangesInTable("tweets")
  .subscribe(new Action1<Changes>() { // or apply RxJava Operators
    // do what you want!
  });
```

######Get result with RawQuery with joins and other SQL things

```java
storIOSQLite
  .get()
  .listOfObjects(TweetAndUser.class)
  .withQuery(new RawQuery.Builder()
    .query("SELECT * FROM tweets JOIN users ON tweets.user_name = users.name WHERE tweets.user_name = ?")
    .args("artem_zin")
    .build())
  .prepare()
  .createObservableStream();
```

######Customize behavior of `Get` Operation with `GetResolver`

```java
GetResolver<Type> getResolver = new DefaultGetResolver()<Type> {
  @Override @NonNull public SomeType mapFromCursor(@NonNull Cursor cursor) {
    return new SomeType(); // parse Cursor here
  }
};

storIOSQLite
  .get()
  .listOfObjects(Tweet.class)
  .withQuery(someQuery)
  .withGetResolver(getResolver) // here we set custom GetResolver for Get Operation
  .prepare()
  .executeAsBlocking();
```

Several things about `Get` Operation:
* There is `DefaultGetResolver` — Default implementation of `GetResolver` which simply redirects query to `StorIOSQLite`, in 99% of cases `DefaultGetResolver` will be enough
* As you can see, results of `Get` Operation computed even if you'll apply `RxJava` operators such as `Debounce`, if you want to avoid unneeded computations, please combine `StorIOSQLite.observeChangesInTable()` with `Get` Operation manually.
* In next versions of `StorIO` we are going to add `Lazy<T>` to allow you skip unneeded computations
* If you want to `Put` multiple items into `StorIOSQLite`, better to do this in transaction to avoid multiple calls to the listeners (see docs about `Put` Operation)

####2. Put Operation

######Put object of some type
```java
Tweet tweet = getSomeTweet();

storIOSQLite
  .put()
  .object(tweet)
  .prepare()
  .executeAsBlocking(); // or createObservable()
```

######Put multiple objects of some type
```java
List<Tweet> tweets = getSomeTweets();

storIOSQLite
  .put()
  .objects(Tweet.class, tweets)
  .prepare()
  .executeAsBlocking(); // or createObservable()
```

######Put `ContentValues`
```java
ContentValues contentValues = getSomeContentValues(); 

storIOSQLite
  .put()
  .contentValues(contentValues)
  .withPutResolver(putResolver) // requires PutResolver<ContentValues>
  .prepare()
  .executeAsBlocking(); // or createObservable()
```

`Put` Operation requires `PutResolver` which defines the behavior of `Put` Operation (insert or update).

```java
PutResolver<SomeType> putResolver = new DefaultPutResolver<SomeType>() {
  @Override @NonNull public InsertQuery mapToInsertQuery(@NonNull SomeType object) {
    return new InsertQuery.Builder()
      .table("some_table")
      .build();
  }
  
  @Override @NonNull public UpdateQuery mapToUpdateQuery(@NonNull SomeType object) {
    return new UpdateQuery.Builder()
      .table("some_table")
      .where("some_column = ?")
      .whereArgs(object.someColumn())
      .build();
  }
  
  @Override @NonNull public ContentValues mapToContentValues(@NonNull SomeType object) {
    final ContentValues contentValues = new ContentValues();
    // fill with fields from object
    return contentValues;
  }
};
```

Several things about `Put` Operation:
* `Put` Operation requires `PutResolver`
* `Put` Operation for collections can be executed in transaction and by default it will use transaction, you can customize this via `useTransaction(true)` or `useTransaction(false)`
* `Put` Operation in transaction will produce only one notification to `StorIOSQLite` observers
* Result of `Put` Operation can be useful if you want to know what happened: insert (and insertedId) or update (and number of updated rows)

####3. Delete Operation

######Delete object
```java
Tweet tweet = getSomeTweet();

storIOSQLite
  .delete()
  .object(tweet)
  .prepare()
  .executeAsBlocking(); // or createObservable()
``` 

######Delete multiple objects
```java
List<Tweet> tweets = getSomeTweets();

storIOSQLite
  .delete()
  .objects(Tweet.class, tweets)
  .prepare()
  .executeAsBlocking(); // or createObservable()
```

Delete Resolver

```java
DeleteResolver<SomeType> deleteResolver = new DefaultDeleteResolver<SomeType>() {
  @Override @NonNull public DeleteQuery mapToDeleteQuery(@NonNull SomeType object) {
    return new DeleteQuery.Builder()
      .table("some_table")
      .where("some_column = ?")
      .whereArgs(object.someColumn())
      .build();
  }
};
```

Several things about `Delete` Operation:
* `Delete` Operation foc collection can be performed in transaction, by default it will use transaction if possible
* Same rules as for `Put` Operation about notifications for `StorIOSQLite` observers: transaction -> one notification, without transaction - multiple notifications
* Result of `Delete` Operation can be useful if you want to know what happened

####4. ExecSql Operation
Sometimes you need to execute raw sql, `StorIOSQLite` allows you to do it

```java
storIOSQLite
  .execSql()
  .withQuery(new RawQuery.Builder()
    .query("ALTER TABLE ? ADD COLUMN ? INTEGER")
    .args("tweets", "number_of_retweets")
    .affectedTables("tweets") // optional: you can specify affected tables to notify Observers 
    .build())
  .prepare()
  .executeAsBlocking(); // or createObservable()
```

Several things about `ExecSql`:
* Use it for non insert/update/query/delete operations
* Notice that you can set list of tables that will be affected by `RawQuery` and `StorIOSQLite` will notify tables Observers


####How object mapping works?
#####You can set default type mappings when you build instance of `StorIOSQLite` or `StorIOContentResolver`

```java
StorIOSQLite storIOSQLite = new DefaultStorIOSQLite.Builder()
  .db(someSQLiteDatabase)
  .addTypeDefaults(Tweet.class, new SQLiteTypeDefaults.Builder<Tweet>()
    .putResolver(Tweet.PUT_RESOLVER) // object that knows how to perform Put Operation (insert or update)
    .getResolver(Tweet.GET_RESOLVER) // object that knows how to perform Get Operation
    .deleteResolver(Tweet.DELETE_RESOLVER)  // object that knows how to perform Delete Operation
    .build())
  .addTypeDefaults(...)
  // other options
  .build(); // This instance of StorIOSQLite will know how to work with Tweet objects
```

You can override Operation Resolver per each individual Operation, it can be useful for working with `SQL JOIN`.
Also, as you can see, there is no Reflection, and no performance reduction in compare to manual object mapping code.

We are thinking about optional Compile-Time annotation processing for generating resolvers implementation in compile-time.

API of `StorIOContentResolver` is same.

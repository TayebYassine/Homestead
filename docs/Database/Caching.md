# Caching

Concurrent caching is a technical approach designed to allow multiple processes to simultaneously read and write data to a shared cache without data conflicts.

It is currently based on Java [Concurrent HashMap](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentHashMap.html), a thread-safe implementation of the Map interface designed for high-performance data fetching.

## Interval

Once Homestead loads all data from the database, it creates an asynchronous task that exports all modified data saved in cached map to the selected database in a specific interval.

You can change the interval in **config.yml**:
```yaml
cache-interval: 30 # 30 seconds
```

## When do I change this interval?

If you have a lot of regions on your server, like over 1,000, increase the interval up to 1 minute (60 seconds).

You can change it to any value, and we recommend it to be 30 seconds if you have few regions.

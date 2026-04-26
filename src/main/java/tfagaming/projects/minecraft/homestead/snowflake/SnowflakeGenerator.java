package tfagaming.projects.minecraft.homestead.snowflake;

import tfagaming.projects.minecraft.homestead.logs.Logger;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Snowflake ID Generator for Homestead models.
 */
public class SnowflakeGenerator {
	private static final long CUSTOM_EPOCH = 1704067200000L;

	private static final long WORKER_ID_BITS = 10L;
	private static final long SEQUENCE_BITS = 12L;

	private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1;
	private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

	private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
	private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

	private final long workerId;
	private final Lock lock = new ReentrantLock();

	private volatile long lastTimestamp = -1L;
	private volatile long sequence = 0L;

	/**
	 * Creates a generator with a specific worker ID.
	 *
	 * @param workerId Unique ID for this machine/worker, ranging from 0 to 1023
	 * @throws IllegalArgumentException if workerId is out of range
	 */
	public SnowflakeGenerator(long workerId) {
		if (workerId < 0 || workerId > MAX_WORKER_ID) {
			throw new IllegalArgumentException("Worker ID must be between 0 and " + MAX_WORKER_ID + ", got: " + workerId);
		}

		this.workerId = workerId;
	}

	/**
	 * Creates a generator with a random worker ID.
	 */
	public SnowflakeGenerator() {
		this.workerId = generateWorkerId();
	}

	/**
	 * Extracts the timestamp (as epoch millis) from a Snowflake ID.
	 */
	public static long extractTimestamp(long snowflakeId) {
		return (snowflakeId >> TIMESTAMP_SHIFT) + CUSTOM_EPOCH;
	}

	/**
	 * Extracts the worker ID from a Snowflake ID.
	 */
	public static long extractWorkerId(long snowflakeId) {
		return (snowflakeId >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
	}

	/**
	 * Extracts the sequence number from a Snowflake ID.
	 */
	public static long extractSequence(long snowflakeId) {
		return snowflakeId & MAX_SEQUENCE;
	}

	/**
	 * Converts a Snowflake ID to an Instant.
	 */
	public static Instant toInstant(long snowflakeId) {
		return Instant.ofEpochMilli(extractTimestamp(snowflakeId));
	}

	private static long generateWorkerId() {
		long id = 0L;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface ni = interfaces.nextElement();
				byte[] mac = ni.getHardwareAddress();
				if (mac != null && mac.length >= 6) {

					id = ((mac[4] & 0xFF) << 8 | (mac[5] & 0xFF)) & MAX_WORKER_ID;
					break;
				}
			}
		} catch (Exception ignored) {
		}

		if (id == 0L) {
			id = new SecureRandom().nextInt((int) MAX_WORKER_ID + 1);
		}

		return id;
	}

	/**
	 * Generates the next unique Snowflake ID.
	 *
	 * @return A unique 64-bit positive long ID
	 * @throws IllegalStateException if the clock moves backwards
	 */
	public long nextId() {
		lock.lock();
		try {
			long currentTimestamp = getCurrentTimestamp();

			if (currentTimestamp < lastTimestamp) {
				throw new IllegalStateException("Refusing to generate ID for " + (lastTimestamp - currentTimestamp) + " milliseconds");
			}

			if (currentTimestamp == lastTimestamp) {
				sequence = (sequence + 1) & MAX_SEQUENCE;

				if (sequence == 0) {
					Logger.error("[Snowflake] Sequence overflow, waiting for next millisecond. Worker=" + workerId);
					currentTimestamp = waitForNextMillis(lastTimestamp);
				}
			} else {
				sequence = 0L;
			}

			lastTimestamp = currentTimestamp;
			long id = ((currentTimestamp - CUSTOM_EPOCH) << TIMESTAMP_SHIFT)
					| (workerId << WORKER_ID_SHIFT)
					| sequence;

			Logger.debug("[Snowflake] Generated ID=" + id + " timestamp=" + currentTimestamp + " worker=" + workerId + " sequence=" + sequence + " hash=" + System.identityHashCode(this));

			return id;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Generates the next unique Snowflake ID.
	 *
	 * @return A unique and unsigned 64-bit positive long ID as string
	 * @throws IllegalStateException if the clock moves backwards
	 */
	public String nextIdString() {
		return Long.toUnsignedString(nextId());
	}

	/**
	 * Returns the worker ID of this generator instance.
	 */
	public long getWorkerId() {
		return workerId;
	}

	/**
	 * Returns a 64-bit long value representing the number of milliseconds elapsed since the Unix epoch.
	 */
	private long getCurrentTimestamp() {
		return System.currentTimeMillis();
	}

	private long waitForNextMillis(long lastTimestamp) {
		long timestamp = getCurrentTimestamp();

		while (timestamp <= lastTimestamp) {
			timestamp = getCurrentTimestamp();
		}

		return timestamp;
	}
}
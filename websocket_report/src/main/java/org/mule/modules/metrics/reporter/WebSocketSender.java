package org.mule.modules.metrics.reporter;

import java.io.Closeable;
import java.io.IOException;

import org.mule.modules.metrics.model.MetricsValue;

public interface WebSocketSender extends Closeable {

	/**
	 * Connects to the server.
	 *
	 * @throws IllegalStateException
	 *             if the client is already connected
	 * @throws IOException
	 *             if there is an error connecting
	 */
	public void connect() throws IllegalStateException, IOException;

	/**
	 * Sends the given measurement to the server.
	 *
	 * @param name
	 *            the name of the metric
	 * @param value
	 *            the value of the metric
	 * @param timestamp
	 *            the timestamp of the metric
	 * @throws IOException
	 *             if there was an error sending the metric
	 */
	public void send(MetricsValue value) throws IOException;

	/**
	 * Flushes buffer, if applicable
	 *
	 * @throws IOException
	 */
	void flush() throws IOException;

	/**
	 * Returns true if ready to send data
	 */
	boolean isConnected();

	/**
	 * Returns the number of failed writes to the server.
	 *
	 * @return the number of failed writes to the server
	 */
	public int getFailures();

}
/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.async;

import io.greenscreens.hllapi.IHllAPI;

/**
 * Callback for async receiving HLL messages
 *
 */
public interface MessageListener {

	void onData(final IHllAPI api, final long retVal);
}

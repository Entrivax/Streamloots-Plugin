package com.cisco.commons.networking;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

/**
 * SSE (Server-sent events) client.
 * Server-Sent Events (SSE) is a <a href="https://en.wikipedia.org/wiki/Push_technology">server push</a> technology 
 * enabling a client to receive automatic updates from a server via HTTP connection. <br/>
 * The Server-Sent Events EventSource API is standardized as part of 
 * <a href="https://www.w3.org/TR/eventsource/">HTML5</a> by the W3C. <br/>
 * It is used for unidirectional server to client events, as opposed to the full-duplex bidirectional WebSockets. <br/>
 * <br/>
 * The SSE client implementation is based on Java 11 HttpClient. <br/>
 * The SSE client has reconnect sampling mechanism with a default time of one minute. <br/>
 * The SSE client has connectivity refresh mechanism with default time of 24 hours. <br/>
 * 
 * For secure SSL, there is support for non-trusted hosts by DisableHostnameVerification system property by setting
 * setDisableHostnameVerificationSystemProperty parameter when building the object.
 * This is translating to: System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true"); 
 * This is not best practice as it is risky and sets a global system property, it is better to work with a trusted host.
 * <br/>
 * <br/>
 * Copyright 2021 Cisco Systems
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 * @author Liran Mendelovich
 */
public class SSEClient {

    private static final long DEFAULT_RECONNECT_SAMPLING_TIME_MILLIS = 60L * 1000L;
    private static final long DEFAULT_CONNECTIVITY_REFRESH_DELAY = 24;
    private static final TimeUnit DEFAULT_CONNECTIVITY_REFRESH_DELAY_TIME_UNIT = TimeUnit.HOURS;
    
	private String url;
	private Map<String, String> headerParams;
	private EventHandler eventHandler;
	private long reconnectSamplingTimeMillis = DEFAULT_RECONNECT_SAMPLING_TIME_MILLIS;
	private long connectivityRefreshDelay = DEFAULT_CONNECTIVITY_REFRESH_DELAY;
	private TimeUnit connectivityRefreshDelayTimeUnit = DEFAULT_CONNECTIVITY_REFRESH_DELAY_TIME_UNIT;
	
    private InputStream inputStream;
    private SSLContext sslContext;

    private AtomicBoolean shouldRun = new AtomicBoolean(true);
    private AtomicBoolean shouldSkipSleep = new AtomicBoolean(false);
    private String threadName;
    private SubscribeStatus status = SubscribeStatus.NOT_STARTED;
    
    private ExecutorService pool;
    private ScheduledExecutorService connectivityRefreshPoolScheduler;

    private Logger logger;
    
    public enum SubscribeStatus {
        SUCCESS, FAILING, RECONNECTING, NOT_STARTED, STOPPED
    }

    public SSEClient(String url, Map<String, String> headerParams, EventHandler eventHandler,
    		Long reconnectSamplingTimeMillis, Long connectivityRefreshDelay,
    		TimeUnit connectivityRefreshDelayTimeUnit, boolean setDisableHostnameVerificationSystemProperty, Logger logger) {
		super();
        this.logger = logger;
		if (url == null) {
			throw new IllegalArgumentException("url cannot be null");
		}
		if (eventHandler == null) {
			throw new IllegalArgumentException("eventHandler cannot be null");
		}
		if (reconnectSamplingTimeMillis != null) {
			if (reconnectSamplingTimeMillis < 500) {
				throw new IllegalArgumentException("reconnectSamplingTimeMillis must be at least 500");
			}
			this.reconnectSamplingTimeMillis = reconnectSamplingTimeMillis;
		}
		if (connectivityRefreshDelay != null) {
			if (connectivityRefreshDelay <= 0) {
				throw new IllegalArgumentException("connectivityRefreshDelay must be larger than zero.");
			}
			if (connectivityRefreshDelayTimeUnit == null) {
				throw new IllegalArgumentException("connectivityRefreshDelayTimeUnit cannot be null when connectivityRefreshDelay is set");
			}
			Duration connectivityRefreshDuration = Duration.of(connectivityRefreshDelay, connectivityRefreshDelayTimeUnit.toChronoUnit());
			if (connectivityRefreshDuration.toMillis() < reconnectSamplingTimeMillis || connectivityRefreshDuration.toSeconds() < 10) {
				throw new IllegalArgumentException("connectivityRefresh duration must be larger than reconnectSamplingTime and at least 10 seconds.");
			}
			this.connectivityRefreshDelay = connectivityRefreshDelay;
			this.connectivityRefreshDelayTimeUnit = connectivityRefreshDelayTimeUnit;
		}
		this.url = url;
		this.headerParams = headerParams;
		if (this.headerParams == null) {
			this.headerParams = new HashMap<>();
		}
		this.eventHandler = eventHandler;
		this.pool = Executors.newSingleThreadExecutor();
		this.connectivityRefreshPoolScheduler = Executors.newScheduledThreadPool(1);
		try {
			if (setDisableHostnameVerificationSystemProperty) {
				System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
			}
			this.sslContext = buildSSLContext();
		} catch (Exception e) {
			throw new IllegalStateException("Failed getting SSL context", e);
		}
	}
    
    private SSLContext buildSSLContext() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        return SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
    }
    
    private void getChanges() {
        threadName = Thread.currentThread().getName();
        while (shouldRun.get() && !Thread.currentThread().isInterrupted()) {
            try {
            	getChangesHelper();
            } catch (Exception e) {
            }
            if (shouldRun.get() && !Thread.currentThread().isInterrupted()) {
                status = SubscribeStatus.RECONNECTING;
                if (!shouldSkipSleep.compareAndSet(true, false)) {
                    sleepQuitely(reconnectSamplingTimeMillis);
                }
            }
        }
    }

    private void sleepQuitely(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
           logger.severe("Error sleeping: " + e.getMessage());
        }
    }
    
    public void getChangesHelper() throws IOException {
        try {

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(url))
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .header("Accept", "text/event-stream");

            Set<Entry<String, String>> customHeaderParams = headerParams.entrySet();
			for (Entry<String, String> headerParam : customHeaderParams) {
				requestBuilder.header(headerParam.getKey(), headerParam.getValue());
			}
			HttpRequest request = requestBuilder.build();
            
            HttpResponse<InputStream> response = HttpClient.newBuilder().sslContext(sslContext)
                .build()
                .send(request, BodyHandlers.ofInputStream());
            
            if (response.statusCode() == HttpStatus.SC_OK) {
                status = SubscribeStatus.SUCCESS;
                inputStream = response.body();
                handleResponse(inputStream);
            } else {
                logger.info("Getting error body response.");
                String body = getNonStreamBodyResponse(response);
                logger.severe(String.format("Got error response: %d %s", response.statusCode(), body));
            }
            
        } catch (Exception e) {
            String errorMessage = "Got exception: " + e.getMessage() + ", cause: " + e.getCause() + ", class: " + e.getClass();
            if (!(e instanceof InterruptedException)) {
                logger.severe(errorMessage);
                throw new IOException(errorMessage, e);
            }
        }
    }

    public String getNonStreamBodyResponse(HttpResponse<InputStream> response) throws IOException {
        try (InputStream currentInputStream = response.body()) {
            return IOUtils.toString(response.body(), StandardCharsets.UTF_8);
        }
    }

    private void handleResponse(InputStream inputStream) throws IOException {
    	
    	/*
         * Separate notifications following SSE (server-sent-events) protocol:
         * - A message text goes after 'data: ', the space after the colon is optional.
         * - Messages are delimited with double line breaks \n\n.
         */
    	
        try (BufferedInputStream in = IOUtils.buffer(inputStream)) {
        	try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        		String line = null;
        		StringBuilder messageBuilder = new StringBuilder();
        		while((line = reader.readLine()) != null) {
        			String content = line;
        			if (content.startsWith("data:")) {
        				messageBuilder.append(content.substring(5));
    				}
        			String message = messageBuilder.toString();
					if (line.trim().isEmpty() && !message.isEmpty()) {
        				handleData(message);
        				messageBuilder = new StringBuilder();
                    }
        	    }
        	}
        } catch (Exception e) {
            String errorMessage = "Could not handle response: " + e.getMessage();
            if (!"closed".equals(e.getMessage())) {
                logger.severe(errorMessage);
                throw new IOException(errorMessage, e);
            }
        }
    }

    private void handleData(String eventText) {
    	try {
			eventHandler.handle(eventText.trim());
		} catch (Exception e) {
			logger.severe("Error handleData: " + e.getMessage());
		}
    }

    public boolean isSubscribedSuccessfully() {
        return shouldRun.get() && SubscribeStatus.SUCCESS.equals(status);
    }

    public void stopCurrentRequest() {
        shouldSkipSleep.set(true);
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                logger.severe("Got error stopCurrentRequest: " + e.getMessage());
                logger.severe(e.toString());
            }
        }
    }
    
    public void start() {
        Runnable connectivityRefreshTask = () -> {
            try {
                stopCurrentRequest();
            } catch (Exception e) {
                logger.severe("Got error at connectivityRefreshTask: " + e.getMessage());
            }
        };
        scheduleConnectivityRefreshTask(connectivityRefreshTask);
        
        pool.execute(() -> {
        	try {
				getChanges();
			} catch (Exception e) {
				logger.severe("Got error getChanges: " + e.getMessage());
				logger.severe(e.toString());
			}
        });
    }

    protected void scheduleConnectivityRefreshTask(Runnable connectivityRefreshTask) {
        connectivityRefreshPoolScheduler.scheduleWithFixedDelay(connectivityRefreshTask , connectivityRefreshDelay, connectivityRefreshDelay, connectivityRefreshDelayTimeUnit);
    }

    protected SubscribeStatus getStatus() {
        return status;
    }
    
    public void shutdown() {
        try {
            shouldRun.set(false);
            stopCurrentRequest();
            pool.shutdownNow();
            connectivityRefreshPoolScheduler.shutdownNow();
            status = SubscribeStatus.STOPPED;
        } catch (Exception e) {
            logger.severe("Error in preDestroy");
            logger.severe(e.toString());
        }
    }

}
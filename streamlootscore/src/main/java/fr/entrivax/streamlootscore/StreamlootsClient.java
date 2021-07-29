package fr.entrivax.streamlootscore;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class StreamlootsClient {
    private String _url;
    private com.cisco.commons.networking.SSEClient _sseClient;
    private IStreamlootsRedemptionHandler _redemptionHandler;
    private Logger _logger;

    public StreamlootsClient(String url, IStreamlootsRedemptionHandler redemptionHandler, Logger logger) {
        this._url = url;
        this._redemptionHandler = redemptionHandler;
        this._logger = logger;
    }

    public void connect() {
        _sseClient = new com.cisco.commons.networking.SSEClient(this._url, null,
            new com.cisco.commons.networking.EventHandler() {

                @Override
                public void handle(String eventText) {
                    try {
                        StreamlootsRedemption event = new Gson().fromJson(eventText, StreamlootsRedemption.class);

                        if (event != null && event.data != null && event.data.type.equals("redemption")) {
                            _redemptionHandler.handle(event);
                        }
                    } catch (Exception e) {
                        _logger.log(Level.SEVERE, e.toString());
                        e.printStackTrace();
                    }
                }

            }, null, null, null, false, this._logger);
        _sseClient.start();
    }

    public void disconnect() {
        _redemptionHandler.cancelRunningTasks();
        _sseClient.shutdown();
    }
}

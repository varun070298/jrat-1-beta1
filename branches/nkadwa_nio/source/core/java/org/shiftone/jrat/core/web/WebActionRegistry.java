package org.shiftone.jrat.core.web;

import org.shiftone.jrat.core.Environment;
import org.shiftone.jrat.core.shutdown.ShutdownListener;
import org.shiftone.jrat.core.spi.WebActionFactory;
import org.shiftone.jrat.core.web.http.Server;
import org.shiftone.jrat.core.web.http.WebActionHandler;
import org.shiftone.jrat.util.log.Logger;

/**
 * @author (jeff@shiftone.org) Jeff Drost
 */
public class WebActionRegistry implements ShutdownListener {

    private static final Logger LOG = Logger.getLogger(WebActionRegistry.class);
    private final Server server;
    private final WebActionHandler handler;

    public WebActionRegistry() {
        LOG.info("WebActionRegistry");
        handler = new WebActionHandler();

        if (Environment.getSettings().isHttpServerEnabled()) {
            LOG.info("starting HTTP server on port " + Environment.getSettings().getHttpPort() + "...");
            server = new Server(Environment.getSettings().getHttpPort(), handler);

            server.start();
        } else {
            server = null;
        }
    }

    public void add(WebActionFactory webActionFactory) {
          LOG.info("adding " + webActionFactory.getTitle());
        handler.add(webActionFactory);
    }

    public String toString()
    {
        return "HTTP Server";
    }

    public void shutdown() throws Exception
    {
        if (server != null)
        {
            server.shutdown();
        }
    }
}

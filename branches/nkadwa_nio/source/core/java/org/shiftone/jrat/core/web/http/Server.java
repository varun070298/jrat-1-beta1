package org.shiftone.jrat.core.web.http;

import org.shiftone.jrat.core.shutdown.ShutdownListener;
import org.shiftone.jrat.util.log.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author jeff@shiftone.org (Jeff Drost)
 */
public class Server extends Thread implements ShutdownListener {

    private static final Logger LOG = Logger.getLogger(Server.class);
    private final int port;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private Handler handler;

    public Server(int port, Handler handler) {
        LOG.info("new");
        this.port = port;
        setHandler(handler);
        setDaemon(true);
        setName("HTTP Server");
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private void initialize() {

        try {

            LOG.info("starting on port " + port + "...");
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(port));

            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void handleException(Response response, HttpException e) throws IOException {
        response.setStatus(e.getStatus());
        Writer out = response.getWriter();
        out.write("<h1>HTTP ");
        out.write(String.valueOf(e.getStatus().getCode()));
        out.write(" - ");
        out.write(e.getStatus().getMessage());
        out.write("</h1>");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        printWriter.flush();
        out.write("<pre>");
        out.write(stringWriter.toString());
        out.write("</pre>");
    }

    public void run() {

        initialize();

        try
        {
            while (selector.select() > 0)
            {
                Iterator selectorIter = selector.selectedKeys().iterator();
                while (selectorIter.hasNext())
                {
                    SelectionKey key = (SelectionKey) selectorIter.next();

                    if (!key.isValid())
                    {
                        LOG.error("INVALID KEY: " + key);
                        continue;
                    }

                    if (key.isAcceptable())
                    {
                        ServerSocketChannel keyChannel = (ServerSocketChannel) key.channel();
                        ServerSocket serverSocket = keyChannel.socket();
                        Socket socket = serverSocket.accept();
//                        LOG.debug("Received connection from " + socket.getInetAddress().getHostAddress());
                        SocketChannel serverChannel = socket.getChannel();
                        serverChannel.configureBlocking(false);
                        ConnectionAttachment attachment = new ConnectionAttachment();
                        SelectionKey connKey = serverChannel.register(selector, SelectionKey.OP_READ, attachment);
                        attachment.setSelectionKey(connKey);
                    }
                    else if (key.isReadable())
                    {
                        ConnectionAttachment attachment = (ConnectionAttachment) key.attachment();

                        if (attachment.readRequest())
                        {
                            try {
                                handler.handle(attachment.getRequest(), attachment.getResponse());
                            }
                            catch (HttpException e)
                            {
                                handleException(attachment.getResponse(), e);
                            }

                            attachment.getResponse().flush();
                        }

                    }
                    else if (key.isWritable())
                    {
                        ConnectionAttachment attachment = (ConnectionAttachment) key.attachment();
    
                        attachment.writeRequest();
                    }
                    else
                    {
                        LOG.error("Unknown select operation");
                    }

                }
                selector.selectedKeys().clear();
            }
        }
        catch (Exception ex)
        {
            LOG.error("HTTP Error", ex);
        }
//        while (true) {
//
//            try {
//                processRequest(serverSocket.accept());
//            } catch (Throwable e) {
//                LOG.error("failed to processRequest request", e);
//            }
//
//        } // while

    }

    public void shutdown() throws Exception
    {
        try
        {
            serverChannel.close();
        }
        catch (Exception ex)
        {
        }
    }

    //    public static void main(String[] args) throws Exception {
//
//        WebActionHandler handler = new WebActionHandler();
//        Server server = new Server(8080, handler);
//        handler.add(new TestWebActionFactory("one"));
//        handler.add(new TestWebActionFactory("two"));
//        handler.add(new TestWebActionFactory("three"));
//
//        handler.add(new TreeWebActionFactory(new ArrayList()));
//        server.start();
//        Thread.sleep(1000 * 160);
//    }

}

package com.example.monopoly_deal_game.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * TCP client that connects to a {@link GameServer}, sending commands and
 * receiving session snapshots and lobby events.
 */
public class NetworkClient implements AutoCloseable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final BlockingQueue<NetworkMessage> inbox = new LinkedBlockingQueue<>();
    private final Consumer<NetworkMessage> listener;
    private final Consumer<Exception> onError;
    private volatile boolean connected = true;

    public NetworkClient(String host, int port, Consumer<NetworkMessage> listener) throws Exception {
        this(host, port, listener, null);
    }

    public NetworkClient(String host, int port, Consumer<NetworkMessage> listener,
                         Consumer<Exception> onError) throws Exception {
        socket = new Socket();
        socket.connect(new java.net.InetSocketAddress(Objects.requireNonNull(host), port), 10000);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        this.listener = listener;
        this.onError = onError;
        Thread t = new Thread(this::readLoop, "network-client-reader");
        t.setDaemon(true);
        t.start();
    }

    public boolean isConnected() {
        return connected && !socket.isClosed();
    }

    public void send(NetworkMessage msg) throws Exception {
        out.reset();
        out.writeObject(msg);
        out.flush();
    }

    public NetworkMessage take() throws InterruptedException {
        return inbox.take();
    }

    private void readLoop() {
        try {
            while (!socket.isClosed()) {
                Object o = in.readObject();
                if (o instanceof NetworkMessage m) {
                    inbox.offer(m);
                    if (listener != null) {
                        listener.accept(m);
                    }
                }
            }
        } catch (Exception ex) {
            connected = false;
            if (onError != null) {
                onError.accept(ex);
            }
        }
    }

    @Override
    public void close() throws Exception {
        connected = false;
        socket.close();
    }
}
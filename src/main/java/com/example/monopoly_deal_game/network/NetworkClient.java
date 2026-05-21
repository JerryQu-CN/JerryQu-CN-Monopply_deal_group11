package com.example.monopoly_deal_game.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class NetworkClient implements AutoCloseable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final BlockingQueue<NetworkMessage> inbox = new LinkedBlockingQueue<>();
    private final Consumer<NetworkMessage> listener;

    public NetworkClient(String host, int port, Consumer<NetworkMessage> listener) throws Exception {
        socket = new Socket(Objects.requireNonNull(host), port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        this.listener = listener;
        Thread t = new Thread(this::readLoop, "network-client-reader");
        t.setDaemon(true);
        t.start();
    }

    public void send(NetworkMessage msg) throws Exception {
        try {
            out.reset();
            out.writeObject(msg);
            out.flush();
        } catch (Exception ex) {
            System.err.println("[NetworkClient] send failed: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            ex.printStackTrace(System.err);
            throw ex;
        }
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
            System.err.println("[NetworkClient] readLoop failed: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    public void close() throws Exception {
        socket.close();
    }
}

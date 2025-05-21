package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<String> validPaths;
    private final ExecutorService threadPool;

    public Server() {
        this.validPaths = List.of(
                "/index.html", "/spring.svg", "/spring.png",
                "/resources.html", "/styles.css", "/app.js",
                "/links.html", "/forms.html", "/classic.html",
                "/events.html", "/events.js"
        );
        this.threadPool = Executors.newFixedThreadPool(64);
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(new ConnectionHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private class ConnectionHandler implements Runnable {
        private final Socket socket;

        public ConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                 socket) {

                String requestLine = in.readLine();
                if (requestLine == null) return;

                String[] parts = requestLine.split(" ");
                if (parts.length != 3) return;

                String path = parts[1];
                if (!validPaths.contains(path)) {
                    sendNotFound(out);
                    return;
                }

                Path filePath = Path.of(".", "public", path);
                String mimeType = Files.probeContentType(filePath);

                if ("/classic.html".equals(path)) {
                    handleClassicHtml(filePath, mimeType, out);
                } else {
                    sendFile(filePath, mimeType, out);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendNotFound(BufferedOutputStream out) throws IOException {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        }

        private void handleClassicHtml(Path filePath, String mimeType, BufferedOutputStream out) throws IOException {
            String template = Files.readString(filePath);
            byte[] content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
            sendResponse(out, mimeType, content.length, content);
        }

        private void sendFile(Path filePath, String mimeType, BufferedOutputStream out) throws IOException {
            long length = Files.size(filePath);
            sendResponse(out, mimeType, length, null);
            Files.copy(filePath, out);
            out.flush();
        }

        private void sendResponse(BufferedOutputStream out, String mimeType, long contentLength, byte[] content) throws IOException {
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + contentLength + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            if (content != null) {
                out.write(content);
            }
        }
    }
}

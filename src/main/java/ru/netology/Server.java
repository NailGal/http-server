package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ConcurrentMap<String, ConcurrentMap<String, Handler>> handlers = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }

    public void listen(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                handleConnection(serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private void handleConnection(Socket socket) {
        threadPool.submit(() -> {
            try (InputStream in = socket.getInputStream(); BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream()); socket) {

                Request request = parseRequest(in);
                if (request == null) {
                    sendBadRequest(out);
                    return;
                }
                Handler handler = findHandler(request);
                if (handler == null) {
                    handler.handle(request, out);
                } else {
                    sendNotFound(out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Request parseRequest(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        int headerEnd = -1;
        while ((bytesRead = in.read(data)) != -1) {
            buffer.write(data, 0, bytesRead);
            byte[] rawData = buffer.toByteArray();
            headerEnd = findHeaderEnd(rawData);
            if (headerEnd != -1) break;
        }

        if (headerEnd == -1) return null;

        byte[] headersBytes = Arrays.copyOfRange(buffer.toByteArray(), 0, headerEnd);
        String[] headers = new String(headersBytes, StandardCharsets.UTF_8).split("\r\n");
        if (headers.length == 0) return null;

        String[] requestLine = headers[0].split(" ");
        if (requestLine.length < 3) return null;
        String method = requestLine[0];
        String path = requestLine[1].split("\\?")[0];
        Map<String, String> headersMap = new ConcurrentHashMap<>();

        for (int i = 1; i < headers.length; i++) {
            String[] header = headers[i].split(":", 2);
            if (header.length == 2) {
                headersMap.put(header[0].trim().toLowerCase(), header[1].trim());

            }
        }

        InputStream body = new ByteArrayInputStream(Arrays.copyOfRange(buffer.toByteArray(), headerEnd + 4, buffer.size()));
        return new Request(method, path, headersMap, body);
    }

    private Handler findHandler(Request request) {
        ConcurrentMap<String, Handler> methodHandler = handlers.get(request.getMethod());
        return methodHandler != null ? methodHandler.get(request.getPath()) : null;
    }

    private int findHeaderEnd(byte[] data) {
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private void sendBadRequest(BufferedOutputStream out) throws IOException {
        out.write(("HTTP/1.1 400 Bad Request\r\n" + "Content-Length: 0\r\n" + "Connection: close\r\n\r\n").getBytes());
        out.flush();
    }

    private void sendNotFound(BufferedOutputStream out) throws IOException {
        out.write(("HTTP/1.1 404 Not Found\r\n" + "Content-Length: 0\r\n" + "Connection: close\r\n\r\n").getBytes());
        out.flush();
    }
}








package ru.netology;

import java.io.IOException;

class Main {
    public static void main(String[] args) {
        Server server = new Server();

        server.addHandler("GET", "/hello", (req, out) -> {
            String response = "HTTP/1.1 200 OK\r\n" + "Content-type: text/plain\r\n" + "Content-length: 13\r\n\r\n" + "Hello, World";
            out.write(response.getBytes());
        });

        server.addHandler("POST", "/echo", (req, out) -> {
            try {
                byte[] body = req.getBody().readAllBytes();
                String response = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + "Content-Length: " + body.length + "\r\n\r\n";
                out.write(response.getBytes());
                out.write(body);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.listen(9999);
    }
}
package ru.netology;

import java.io.IOException;
import java.util.List;

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

        server.addHandler("POST", "/form", (req, out) -> {
            List<String> names = req.getPostParam("name");
            String responseBody;
            if (!names.isEmpty()) {
                responseBody = "Hello, " + String.join(", ", names);
            } else {
                responseBody = "No name provided";
            }
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-type: text/plain\r\n" +
                    "Content-length: " + responseBody.length() + "\r\n" +
                    "\r\n" +
                    responseBody;
            out.write(response.getBytes());
        });

        server.listen(9999);
    }
}
package ru.raccoon;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final ExecutorService executorService = Executors.newFixedThreadPool(64);
    private final HashMap<String, HashMap<String, Handler>> handlersMap = new HashMap<>();


    public void start() {
        try (final var serverSocket = new ServerSocket(9999)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.execute(() -> {
                    try {
                        processingConnection(socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                this.addHandler("GET", "default", ((request, outputStream) -> {
                    try {
                        this.useDefaultHandler(outputStream, request.getRequestPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processingConnection(Socket serverSocket) throws IOException {
        try (
                final var in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                final var out = new BufferedOutputStream(serverSocket.getOutputStream());
        ) {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                return;
            }

            final var method = parts[0];
            final var path = parts[1];

            Request request = createRequest(method, path);

            if (!handlersMap.containsKey(request.getRequestMethod())) {
                responseFault(out, "400", "Bad Request");
                return;
            }

            Map<String, Handler> handlerMap = handlersMap.get(request.getRequestMethod());
            String requestPath = request.getRequestPath();

            if (handlerMap.containsKey(requestPath)) {
                Handler handler = handlerMap.get(requestPath);
                handler.handle(request, out);
            } else {
                if (!validPaths.contains(request.getRequestPath())) {
                    responseFault(out, "404", "Page not found");
                } else {
                    Handler handler = handlerMap.get("default");
                    handler.handle(request, out);
                }
            }
            System.out.println(request.getQueryParams());
            System.out.println(request.getQueryParam("las"));
            System.out.println(request.getQueryParam("last"));

        }
    }

    void addHandler(String requestMethod, String requestPath, Handler handler) {
        if (!handlersMap.containsKey(requestMethod)) {
            handlersMap.put(requestMethod, new HashMap<>());
        }
        handlersMap.get(requestMethod).put(requestPath, handler);
    }

    void useDefaultHandler(BufferedOutputStream out, String path) throws IOException {
        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
        }

        final var length = Files.size(filePath);
           out.write((
                 "HTTP/1.1 200 OK\r\n" +
                  "Content-Type: " + mimeType + "\r\n" +
                  "Content-Length: " + length + "\r\n" +
                 "Connection: close\r\n" +
               "\r\n"
               ).getBytes());
             Files.copy(filePath, out);
             out.flush();
}

    void responseFault(BufferedOutputStream out, String responseCode, String responseStatus) throws IOException {
        out.write((
                "HTTP/1.1 " + responseCode + " " + responseStatus + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private Request createRequest(String requestMethod, String requestPath) {
        return new Request(requestMethod, requestPath);
    }
}

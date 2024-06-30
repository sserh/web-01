package ru.raccoon;

public class Main {
  public static void main(String[] args) {

    Server server = new Server();

    server.addHandler("GET", "/spring.png", (request, outputStream) -> {server.responseFault(outputStream,  "404","Spring not found");});
    server.addHandler("GET", "/links.html", (request, outputStream) -> {server.responseFault(outputStream,  "405","links forbidden");});

    server.start();
  }
}



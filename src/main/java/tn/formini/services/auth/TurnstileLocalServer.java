package tn.formini.services.auth;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class TurnstileLocalServer {
    private static HttpServer server;
    private static int port = 0;
    private static String currentSiteKey;

    public static synchronized void start(String siteKey) {
        if (server != null) {
            currentSiteKey = siteKey;
            return; // Already running
        }
        currentSiteKey = siteKey;
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            port = server.getAddress().getPort();
            
            server.createContext("/", exchange -> {
                String htmlContent = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset='UTF-8'>\n" +
                        "    <title>Turnstile Verification</title>\n" +
                        "    <script src='https://challenges.cloudflare.com/turnstile/v0/api.js' async defer></script>\n" +
                        "    <style>\n" +
                        "        body { margin: 0; padding: 0; overflow: hidden; background-color: transparent; display: flex; justify-content: center; align-items: flex-start; }\n" +
                        "        .cf-turnstile { margin: 0; }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class='cf-turnstile' data-sitekey='" + currentSiteKey + "' data-callback='turnstileCallback' data-theme='light'></div>\n" +
                        "    <script>\n" +
                        "        function turnstileCallback(token) {\n" +
                        "            if (window.javaBridge) {\n" +
                        "                window.javaBridge.setTurnstileToken(token);\n" +
                        "            }\n" +
                        "        }\n" +
                        "    </script>\n" +
                        "</body>\n" +
                        "</html>";

                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                byte[] response = htmlContent.getBytes("UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            });

            server.setExecutor(null);
            server.start();
            System.out.println("Turnstile local server started on http://127.0.0.1:" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUrl() {
        return "http://127.0.0.1:" + port + "/";
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
}

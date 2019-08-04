import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class ServerClass {
    private String tokencode;
    private HttpServer server;
    public void CreateServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/spotifyaccesstokenrequest", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
    public String getRedirectURI() {
        return tokencode;
    }
    public void sendQuery(String query) {
        tokencode = query;
    }
    public class MyHandler implements HttpHandler {
        String query;
        @Override
        public void handle(HttpExchange t) throws IOException {
            URI requestURI = t.getRequestURI();
            printRequestInfo(t);
            String response = "This is the response at " + requestURI;
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        private void printRequestInfo(HttpExchange exchange) {
            URI requestURI = exchange.getRequestURI();
            query = requestURI.getQuery();
            if (query != null) {
                sendQuery(query);
            }
        }
    }

    public void CloseServer() {
        server.stop(0);
    }
}

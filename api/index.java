import java.io.*;
import java.net.*;
import com.sun.net.httpserver.*;

public class index {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);

        server.createContext("/", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String skill = "java";
            if(query != null && query.contains("skill=")) {
                skill = query.split("=")[1];
            }
            String result = fetchTrending(skill);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, result.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(result.getBytes());
            }
        });

        server.setExecutor(null);
        server.start();
    }

    private static String fetchTrending(String skill) {
        try {
            String apiURL = "https://api.github.com/search/repositories?q=language:" 
                            + URLEncoder.encode(skill, "UTF-8") 
                            + "&sort=stars&order=desc&per_page=3";

            HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            conn.disconnect();

            return "{ \"inputSkill\": \"" + skill + "\", \"trendingProjects\": " + response.toString() + " }";

        } catch (Exception e) {
            return "{ \"error\": \"Failed to fetch data for " + skill + "\" }";
        }
    }
}
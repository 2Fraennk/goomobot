package goomobot;

import com.google.gson.*;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import static java.net.http.HttpRequest.*;


public class Goomobot {
    private static String url = "";
    private static String user = "";
    private static String passwd = "";
    private static String userId = System.getProperty("userId");
    private static String authToken = System.getProperty("authToken");
    private static String searchMessage = "moin";
    private static String sendMessage = "Moin, moin!";
    private static JsonObject responseData = null;
    private static String roomId = System.getProperty("roomId");
    private static String aka = System.getProperty("aka");
    private static String host = System.getProperty("host");
    private static JsonArray responseMessages;
    private static boolean foundMessages = false;

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        //responseBody = loginAndGetMessages(url,user,passwd);
        //authToken = getAuthToken(responseBody);
        //userId = getUserId(responseBody);
        System.out.println("userId: "+userId);
        System.out.println("authToken: "+authToken);
        System.out.println("roomId: "+roomId);
        System.out.println("aka: "+aka);
        System.out.println("host: "+host);
        foundMessages = searchMessages(userId,authToken,roomId,searchMessage);
        System.out.println("foundMessages: " + foundMessages);
        if (foundMessages == true) {
            sendMessage(userId, authToken, roomId, sendMessage);
            System.out.println("Passt! Message wurde versendet!");
        }
    }


    public static String loginAndGetMessages(String url, String user, String passwd) throws IOException, InterruptedException, URISyntaxException {
        URI URI;
        URI = new URI(
                "https",
                host,
                "/api/v1chat.search?roomId=blabla=\"DashieristeinkrasserTest2!\"",
                null);

        HttpRequest request = newBuilder()
                .uri(URI)
                .setHeader("Content-Type", "application/json2")
                .GET()
                .timeout(Duration.of(30, ChronoUnit.SECONDS))
                .build();

        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        int loginResponseCode = response.statusCode();

        if (loginResponseCode == 200) {
            return responseData.toString();
        }
        else throw new IOException("The login failed with a result of: " + loginResponseCode
                + " (" + responseData + ")");

    }


    public static String getAuthToken(String responseBody) {
        JsonObject data2 = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonObject jdata = data2.get("authtoken").getAsJsonObject();
        authToken = jdata.get("authtoken").toString();
        return authToken;
    }

    public static String getUserId(String responseBody) {
        JsonObject data2 = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonObject jdata = data2.get("data").getAsJsonObject();
        userId = jdata.get("userId").toString();
        return userId;
    }

    public static boolean searchMessages(String XUserId, String XAuthToken, String roomId, String searchMessage) throws IOException, InterruptedException, URISyntaxException {
        System.out.println("userId2: "+userId);
        System.out.println("authToken2: "+authToken);
        System.out.println("roomId2: "+roomId);
        System.out.println("aka2: "+aka);
        System.out.println("host2: "+host);

        URI URI;
        URI = new URI(
                "https",
                host,
                "/api/v1/chat.search?roomId="+roomId+"&searchText=\""+searchMessage+"\"",
                null);

        HttpRequest request = newBuilder()
                .uri(URI)
                .setHeader("Content-Type", "application/json2")
                .setHeader("X-Auth-Token", XAuthToken)
                .setHeader("X-User-Id", XUserId)
                .GET()
                .timeout(Duration.of(30, ChronoUnit.SECONDS))
                .build();

        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        responseData = (JsonObject) JsonParser.parseString(response.body());
        System.out.println("responseData: "+ responseData.toString());
        JsonArray responseMessages = responseData.get("messages").getAsJsonArray();
        for (int i=0;i<responseMessages.size();i++) {
            JsonObject message = responseMessages.get(i).getAsJsonObject();
            JsonElement id = message.get("_id");
            JsonElement score = message.get("score");
            JsonElement ts = message.get("ts");

            System.out.println(""+id.toString()+", "+score.toString()+"");

            if (score.getAsFloat()>1.0) {
                String tsString = ts.toString().split("T")[0].split("\"")[1];
                LocalDate ld = LocalDate.now();
                if (tsString.equals(ld.toString())) {
                    System.out.println(message.toString());
                    foundMessages=true;
                }
            }
        }

        return foundMessages;


    }
    

    public static void sendMessage(String XUserId, String XAuthToken, String roomId, String sendMessage) throws IOException, InterruptedException, URISyntaxException {
        URI URI;
        URI = new URI(
                "https",
                host,
                "/api/v1/chat.postMessage",
                null);

        HttpRequest request = newBuilder()
                .uri(URI)
                .setHeader("Content-Type", "application/json")
                .setHeader("X-Auth-Token", XAuthToken)
                .setHeader("X-User-Id", XUserId)
                .POST(HttpRequest.BodyPublishers.ofString("{\"alias\": \""+aka+"\",\"channel\": \""+roomId+"\",\"text\": \""+sendMessage+"\"}"))
                .timeout(Duration.of(30, ChronoUnit.SECONDS))
                .build();

        HttpResponse<String> response = HttpClient
                .newBuilder()
                //.proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.statusCode());
    }


}
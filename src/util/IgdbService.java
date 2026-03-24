package util;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class IgdbService {

    private static final String CLIENT_ID = "zg39h2jpof98mj4t1ss5anv982z1s2";
    private static final String CLIENT_SECRET = "dtnjw5mepam5jywvucoj8ibbj15nrn";
    private static String accessToken = null;

    // Twitch'ten token al
    private static void getAccessToken() throws Exception {
        HttpResponse<String> response = Unirest.post("https://id.twitch.tv/oauth2/token")
                .queryString("client_id", CLIENT_ID)
                .queryString("client_secret", CLIENT_SECRET)
                .queryString("grant_type", "client_credentials")
                .asString();

        // Token'ı JSON'dan çek
        String body = response.getBody();
        int start = body.indexOf("access_token\":\"") + 15;
        int end = body.indexOf("\"", start);
        accessToken = body.substring(start, end);
    }

    // Oyun ara
    public static String searchGames(String gameName) throws Exception {
        if (accessToken == null) getAccessToken();

        HttpResponse<String> response = Unirest.post("https://api.igdb.com/v4/games")
                .header("Client-ID", CLIENT_ID)
                .header("Authorization", "Bearer " + accessToken)
                .body("fields name,summary,cover.url,genres.name,first_release_date,rating; search \"" + gameName + "\"; limit 10;")
                .asString();

        return response.getBody();
    }
}
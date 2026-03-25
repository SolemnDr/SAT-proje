package util;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import magaza.dao.GameDAO;
import magaza.model.Game;

import java.util.ArrayList;
import java.util.List;

public class IgdbService {

    private static final String CLIENT_ID = "zg39h2jpof98mj4t1ss5anv982z1s2";
    private static final String CLIENT_SECRET = "dtnjw5mepam5jywvucoj8ibbj15nrn";
    private static String accessToken = null;

    private static void getAccessToken() throws Exception {
        HttpResponse<JsonNode> response = Unirest.post("https://id.twitch.tv/oauth2/token")
                .queryString("client_id", CLIENT_ID)
                .queryString("client_secret", CLIENT_SECRET)
                .queryString("grant_type", "client_credentials")
                .asJson();

        accessToken = response.getBody().getObject().getString("access_token");
    }

    // IGDB'den oyun ara ve veritabanına kaydet
    public static List<Game> searchAndSave(String gameName) throws Exception {
        if (accessToken == null) getAccessToken();

        HttpResponse<JsonNode> response = Unirest.post("https://api.igdb.com/v4/games")
                .header("Client-ID", CLIENT_ID)
                .header("Authorization", "Bearer " + accessToken)
                .body("fields name,summary,cover.url,genres.name,first_release_date,rating; search \"" + gameName + "\"; limit 10;")
                .asJson();

        JSONArray array = response.getBody().getArray();
        List<Game> games = new ArrayList<>();
        GameDAO gameDAO = new GameDAO();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);

            Game game = new Game();
            game.setName(obj.optString("name", ""));
            game.setSummary(obj.optString("summary", ""));
            game.setRating(obj.optDouble("rating", 0.0));
            game.setReleaseDate(obj.optLong("first_release_date", 0));
            game.setPrice(0.0); // fiyatı publisher belirleyecek

            // Kapak resmi
            if (obj.has("cover")) {
                String url = obj.getJSONObject("cover").optString("url", "");
                url = "https:" + url.replace("t_thumb", "t_cover_big");
                game.setCoverUrl(url);
            }

            // Kategoriler
            if (obj.has("genres")) {
                JSONArray genres = obj.getJSONArray("genres");
                List<String> genreList = new ArrayList<>();
                for (int j = 0; j < genres.length(); j++) {
                    genreList.add(genres.getJSONObject(j).getString("name"));
                }
                game.setGenres(String.join(", ", genreList));
            }

            gameDAO.save(game);
            games.add(game);
        }

        return games;
    }
}
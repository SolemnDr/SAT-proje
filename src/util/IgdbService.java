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
import java.util.Random; // HATA 1 ÇÖZÜLDÜ: Random eklendi

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

    // IGDB'den oyun ara ve veritabanına kaydet (Manuel Yayıncı Eklemeleri İçin)
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

    // 2. Mağazayı Otomatik Doldur (En Popüler 1000 Oyun)
    public static void seedStoreDatabase() throws Exception {
        if (accessToken == null) getAccessToken();

        System.out.println("IGDB'den En Popüler 1000 Oyun Çekiliyor...");

        Random random = new Random();
        int totalFetched = 0;
        GameDAO gameDAO = new GameDAO(); // HATA 2 ÇÖZÜLDÜ: DAO nesnesi yaratıldı

        for (int i = 0; i < 2; i++) {
            int offset = i * 500;

            String query = "fields name,summary,cover.url,genres.name,rating,first_release_date; " +
                    "where cover != null & rating != null; " +
                    "sort rating_count desc; limit 500; offset " + offset + ";";

            // HATA 3 ÇÖZÜLDÜ: Unirest tipleri Onur'un koduyla (JsonNode) eşitlendi
            HttpResponse<JsonNode> response = Unirest.post("https://api.igdb.com/v4/games")
                    .header("Client-ID", CLIENT_ID)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(query)
                    .asJson();

            JSONArray gamesArray = response.getBody().getArray();
            if (gamesArray.isEmpty()) break;

            for (int j = 0; j < gamesArray.length(); j++) {
                JSONObject jsonGame = gamesArray.getJSONObject(j);
                Game game = new Game();

                game.setName(jsonGame.getString("name"));
                game.setSummary(jsonGame.has("summary") ? jsonGame.getString("summary") : "Açıklama bulunmuyor.");

                // Resim linklerinin başına https: eklemeyi unutmayalım (UI'da resim patlamasın)
                if (jsonGame.has("cover")) {
                    String coverUrl = jsonGame.getJSONObject("cover").getString("url");
                    game.setCoverUrl("https:" + coverUrl.replace("t_thumb", "t_1080p"));
                } else {
                    continue;
                }

                // HATA 4 ÇÖZÜLDÜ: Tavsiye algoritması için Kategoriler (Genres) çekiliyor
                if (jsonGame.has("genres")) {
                    JSONArray genres = jsonGame.getJSONArray("genres");
                    List<String> genreList = new ArrayList<>();
                    for (int k = 0; k < genres.length(); k++) {
                        genreList.add(genres.getJSONObject(k).getString("name"));
                    }
                    game.setGenres(String.join(", ", genreList));
                }

                // 250 TL ile 2000 TL arası rastgele fiyat
                double randomPrice = 250 + (random.nextDouble() * 1750);
                game.setPrice(Math.round(randomPrice * 100.0) / 100.0);

                game.setRating(jsonGame.has("rating") ? jsonGame.getDouble("rating") : 0.0);
                game.setReleaseDate(jsonGame.has("first_release_date") ? jsonGame.getLong("first_release_date") : 0);
                game.setPublisherId(1); // Sistem ID'si

                gameDAO.save(game);
                totalFetched++;
            }

            System.out.println((i + 1) + ". Paket (500 Oyun) başarıyla veritabanına işlendi.");
            Thread.sleep(500);
        }

        System.out.println("Efsane! Mağazaya dünyanın en popüler " + totalFetched + " oyunu başarıyla eklendi!");
    }
}
import magaza.dao.GameDAO;
import magaza.model.Game;
import util.IgdbService;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        // Oyunları IGDB'den çek ve kaydet
        IgdbService.searchAndSave("GTA");

        // Veritabanından oku ve göster
        GameDAO gameDAO = new GameDAO();
        List<Game> games = gameDAO.findAll();
        for (Game g : games) {
            System.out.println(g.getName() + " - " + g.getRating() + " - " + g.getGenres());
        }
    }
}
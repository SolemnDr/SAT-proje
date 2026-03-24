import util.IgdbService;

public class Main {
    public static void main(String[] args) throws Exception {
        String result = IgdbService.searchGames("GTA");
        System.out.println(result);
    }
}
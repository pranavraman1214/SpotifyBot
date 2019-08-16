import com.adamratzman.spotify.main.SpotifyClientAPI;
import com.adamratzman.spotify.utils.*;

import java.util.List;
import java.util.Scanner;

public class SpotifySearch {
    public Album searchAlbum(String albumRequested, SpotifyClientAPI clientAPI) {
        System.out.println("\n");
        System.out.println("We Found The Following Albums: " );
        SpotifyRestAction<PagingObject<SimpleAlbum>> restActionFoundAlbums = clientAPI.getSearch().searchAlbum(albumRequested,11,0, Market.US);
        PagingObject<SimpleAlbum> simpleAlbumPagingObject = restActionFoundAlbums.complete();
        List<SimpleAlbum> found_albums = simpleAlbumPagingObject.getItems();
        for (int i = 0; i < found_albums.size();  i++) {
            System.out.println((i+1) + ". " + found_albums.get(i).getName() + " By: " + found_albums.get(i).getArtists().get(0).getName());
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type the integer of the album you want:");
        int album_wanted = scanner.nextInt();
        SimpleAlbum the_album = found_albums.get(album_wanted);
        String albumid = the_album.getId();
        SpotifyRestAction<Album> restActionmy_album = clientAPI.getAlbums().getAlbum(albumid,Market.US);
        return restActionmy_album.complete();
    }
}

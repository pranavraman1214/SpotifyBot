import com.adamratzman.spotify.endpoints.priv.playlists.ClientPlaylistAPI;
import com.adamratzman.spotify.endpoints.pub.artists.ArtistsAPI;
import com.adamratzman.spotify.main.SpotifyClientAPI;
import com.adamratzman.spotify.utils.*;

import java.util.*;

public class PlaylistFunctions {
    private SpotifyClientBaseline clientobj;

    PlaylistFunctions(SpotifyClientBaseline input) {
        clientobj = input;
    }

    private void CreatePlaylist(String playlist_name, boolean collab, String description,List<Track> tracks) {
        System.out.println("\n");
        System.out.println("Creating Playlist.....");
        System.out.println("\n");
        ClientPlaylistAPI playlistAPI = clientobj.getClientAPI().getClientPlaylists();
        Playlist id = playlistAPI.createPlaylist(clientobj.getClientAPI().getClientProfile().getUserProfile().complete().getId(),playlist_name,description,false,collab).complete();
        System.out.println("The Playlist "+ playlist_name + " has been created!");
        AddTracksToPlaylist(id.getId(),false,"",tracks);
    }

    private void AddTracksToPlaylist(String playlist_id, boolean collab, String description, List<Track> tracks) {
        ClientPlaylistAPI playlistAPI = clientobj.getClientAPI().getClientPlaylists();
        double tracks_divide = Math.ceil(tracks.size() / 100.0);
        System.out.println("Tracks Divide: "+ tracks_divide);
        String[] urids = new String[1];
        for (int i = 0; i < tracks.size(); i++) {
            urids[0] = tracks.get(i).getId();
            playlistAPI.addTracksToPlaylist(clientobj.getClientAPI().getClientProfile().getUserProfile().complete().getId(),playlist_id,urids,null).complete();
        }
        System.out.println("The songs have been added");
    }

    public void FilterandCreateBalancedTurnUpPlaylist() {
        List<Track> highenergytracklist = new ArrayList<>();
        for (int i = 0; i < clientobj.getPlaylist_track_features().size(); i++) {
            SpotifyClientBaseline.TrackAudioFeatures temp = clientobj.getPlaylist_track_features().get(i);
            if (temp.energy > .55 && temp.loudness > -5  && temp.dance >  .50  && temp.duration < 250000) {
                highenergytracklist.add(temp.track);
            }
        }
        System.out.println("\n");
        System.out.println("Songs Added Balanced");

        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int year =calendar.get(Calendar.YEAR);
        CreatePlaylist("Balanced Gym Energy Songs " + month + "/" + year,false, "A Pran Bot Creation listing his high energy songs for the gym at the displayed time of the month",highenergytracklist );

    }

    public void FilterHighEnergySongs() {
        List<Track> highenergytracklist = new ArrayList<>();
        for (int i = 0; i < clientobj.getPlaylist_track_features().size(); i++) {
            SpotifyClientBaseline.TrackAudioFeatures temp = clientobj.getPlaylist_track_features().get(i);
            if (temp.energy > .7) {
                highenergytracklist.add(temp.track);
                System.out.println(temp.track.getName());
            }
        }
        System.out.println("\n");
        System.out.println("Songs Added To High Energy");
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int year =calendar.get(Calendar.YEAR);
        CreatePlaylist("High Energy Songs " + month + "/" + year,false, "A Pran Bot Creation listing his high energy songs at the displayed time of the month",highenergytracklist);
    }

    public void CreateTop20SongsPlaylist() {
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int year =calendar.get(Calendar.YEAR);
        for (int i = 0; i < clientobj.getClientFavTracks().size();  i++)  {
            System.out.println(clientobj.getClientFavTracks().get(i).getName());
        }
        CreatePlaylist("Top 20 Songs " + month + "/" + year,false, "A Pran Bot Creation listing his favorite songs at the displayed month",clientobj.getClientFavTracks());
    }

    public void CreateTopArtistsPlaylist() {
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int year =calendar.get(Calendar.YEAR);
        List<Track>tracks = new ArrayList<>();
        for (int i = 0; i < clientobj.getClientFavArtists().size(); i++) {
            Artist  current = clientobj.getClientFavArtists().get(i);
            ArtistsAPI  artistsAPI =  clientobj.getClientAPI().getArtists();
            List<Track> artistTracks = artistsAPI.getArtistTopTracks(current.getId(), Market.US).complete();
            for (int j = 0; j < artistTracks.size() && j < 5; j++) {
                tracks.add(artistTracks.get(j));
            }
        }
        CreatePlaylist("20 Fav Artists Top 5 Songs " + month + "/" + year,false,"Pran Bot creation listing his fav artists top 5 most popular songs",tracks);
    }

    public void CreateRelatedArtistPlaylist() {
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int year =calendar.get(Calendar.YEAR);
        List<Track>tracks = new ArrayList<>();
        List<Artist> relatedArtists = new ArrayList<>();
        for (int i = 0; i < clientobj.getClientFavArtists().size(); i++) {
            Artist  current = clientobj.getClientFavArtists().get(i);
            ArtistsAPI  artistsAPI =  clientobj.getClientAPI().getArtists();
            List<Artist> artistTracks = artistsAPI.getRelatedArtists(current.getId()).complete();
            for (int j = 0; j < artistTracks.size(); j++) {
                if (!relatedArtists.contains(artistTracks.get(j)) && !current.getName().equals("Justin Bieber") && !clientobj.getClientFavArtists().contains(artistTracks.get(j))) {
                    relatedArtists.add(artistTracks.get(j));
                }
            }
        }
        for (int i = 0; i < relatedArtists.size(); i++) {
            Artist current = relatedArtists.get(i);
            ArtistsAPI  artistsAPI =  clientobj.getClientAPI().getArtists();
            List<Track> artistTracks = artistsAPI.getArtistTopTracks(current.getId(), Market.US).complete();
            for (int j = 0; j < artistTracks.size() && j < 5; j++) {
                tracks.add(artistTracks.get(j));
            }
        }
        CreatePlaylist("Related Artists Top 5 Songs " + month + "/" + year,false,"Pran Bot creation listing his most related artists top 5 most popular songs",tracks);
    }



    public void CreatePlaylistOfAllSongsWithAnArtist() {

    }
}

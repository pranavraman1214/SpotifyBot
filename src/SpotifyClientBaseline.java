
import com.adamratzman.spotify.endpoints.priv.library.ClientLibraryAPI;
import com.adamratzman.spotify.endpoints.priv.personalization.PersonalizationAPI;
import com.adamratzman.spotify.endpoints.priv.playlists.ClientPlaylistAPI;
import com.adamratzman.spotify.endpoints.pub.playlists.PlaylistsAPI;
import com.adamratzman.spotify.endpoints.pub.tracks.TracksAPI;
import com.adamratzman.spotify.main.SpotifyAPI;
import com.adamratzman.spotify.main.SpotifyClientAPI;
import com.adamratzman.spotify.utils.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SpotifyClientBaseline {

    // Variables
    private SpotifyClientAPI clientAPI;
    private List<SimplePlaylist> clientPlaylists;
    private List<Playlist> clientCreatedPlaylists;
    private List<Artist> clientFavArtists;
    private List<Track> clientFavTracks;
    public class TrackAudioFeatures {
        Track track;
        float dance;
        float energy;
        float instrumental;
        float liveness;
        float acoustics;
        float loudness;
        float tempo;
        float speechiness;
        float valence;
        int mode;
        String lyrics;
        String type;
        int duration;
        int popularity;
        TrackAudioFeatures(Track trackin, String lyricsin, float dancein, float energyin, float instrumentalness,float livenessin,
                           float acousticsin, float loudnessin, float tempoin, float speechinessin, float valencein, int modein,
                            int durationin, int popularityin) {
            track = trackin;
            dance = dancein;
            energy = energyin;
            instrumental = instrumentalness;
            liveness = livenessin;
            acoustics=acousticsin;
            loudness = loudnessin;
            tempo = tempoin;
            speechiness = speechinessin;
            valence = valencein;
            mode =  modein;
            duration = durationin;
            popularity = popularityin;
            lyrics = lyricsin;
        }
        @Override
        public String toString(){
            String name = track.getName();
            String concat = name + ": " + "Dance = " + dance + " Energy = " + energy + " Instrumentalness = " + instrumental + " Liveness = " + liveness
                + " Acousticness = " + acoustics +  " Loudness = " + loudness + " Tempo = " + tempo + " Speechiness = " + speechiness + " Valence " + valence + " Mode "
                    + mode + " Type = " + type + " Duration = " + duration;
            return concat;
        }
    }
    private List<TrackAudioFeatures> playlist_track_features = new ArrayList<>();
    private List<TrackAudioFeatures> top_track_features = new ArrayList<>();
    private List<TrackAudioFeatures> temp = new ArrayList<>();


    // Constructor To Create Client Baseline
    SpotifyClientBaseline (SpotifyClientAPI input) {
        clientAPI = input;
        //EstablishClientPlaylists();
        EstabishTopArtists();
       //EstablishTopTracks();
    }


    private void EstablishClientPlaylists() {
        // Gets 50 of the User's playlists. I felt like 50 was more than enough playlists because in all likelihood, people dont create a huge number of playlists.
        ClientPlaylistAPI my_playlists = clientAPI.getClientPlaylists();
        SpotifyRestAction<PagingObject<SimplePlaylist>> playlistRestAction = my_playlists.getClientPlaylists(50,0);
        PagingObject<SimplePlaylist> simplePlaylistPagingObject = playlistRestAction.complete();
        List<SimplePlaylist> simplePlaylists = simplePlaylistPagingObject.getItems();
        System.out.println("\n");
        System.out.println("Playlists in Your Library:");
        for (int i = 0; i < simplePlaylists.size(); i++) {
            SimplePlaylist tempPlaylist = simplePlaylists.get(i);
            System.out.println((i+1) + ". " + tempPlaylist.getName());
        }
        clientPlaylists = simplePlaylists;


        // Gets the playlists the user has created. I felt like this was a better indicator of music taste, than all their playlists
        List<Playlist> userPlaylists = new ArrayList<>();
        for (int i = 0; i < simplePlaylists.size(); i++) {
            Playlist current = clientAPI.getPlaylists().getPlaylist(clientAPI.getClientProfile().getUserProfile().complete().getId(),simplePlaylists.get(i).getId(),Market.US).complete();
            if (current.getOwner().getId().equals(clientAPI.getClientProfile().getUserProfile().complete().getId()) && !current.getCollaborative()) {
                //current.getName().equals("Tester")
                userPlaylists.add(current);
            }
        }
        clientCreatedPlaylists = userPlaylists;

        // Takes the client created playlists and writes it to a file
        System.out.println(clientCreatedPlaylists.size());
        for (int i = 0; i < clientCreatedPlaylists.size(); i++) {
            PlaylistsAPI playlistsAPI = clientAPI.getPlaylists();
            Playlist current = clientCreatedPlaylists.get(i);
            SpotifyRestAction<LinkedResult<PlaylistTrack>> playlistTrackRestAction
                    = playlistsAPI.getPlaylistTracks(clientAPI.getClientProfile().getUserProfile().complete().getId(),current.getId(),50,0,Market.US);
            List<PlaylistTrack> playlistTracks  = playlistTrackRestAction.complete().getItems();
            for (int q = 50; q < 700; q+=50 ) {
                PlaylistsAPI tempAPI = clientAPI.getPlaylists();
                SpotifyRestAction<LinkedResult<PlaylistTrack>> tempplaylistTrackRestAction
                        = tempAPI.getPlaylistTracks(clientAPI.getClientProfile().getUserProfile().complete().getId(),current.getId(),50,q,Market.US);
                playlistTracks.addAll(tempplaylistTrackRestAction.complete().getItems());
            }
            for (int z = 0; z < playlistTracks.size(); z++) {
                TracksAPI tracksAPI = clientAPI.getTracks();
                Track current_track = playlistTracks.get(z).getTrack();
                System.out.println("Current Track:" + current_track.getName());
                TrackAudioFeatures currentfeatures = AudioFeatures(current_track,tracksAPI);
                if (currentfeatures != null) {
                    playlist_track_features.add(currentfeatures);
                    temp.add(currentfeatures);
                }
            }
            WriteToDataFile(current.getName());
            temp.clear();
        }
    }



    private void EstabishTopArtists() {
        // Gets 20 of their top artists
        PersonalizationAPI personalizationAPI = clientAPI.getPersonalization();
        SpotifyRestAction<PagingObject<Artist>> restActionFavoriteArtists = personalizationAPI.getTopArtists();
        PagingObject<Artist> pagingObjectFavArtist = restActionFavoriteArtists.complete();
        List<Artist> artistsList = pagingObjectFavArtist.getItems();
        System.out.println("\n");
        System.out.println("Your Fav Artists: " );
        for (int i = 0; i < artistsList.size(); i++) {
            Artist temp_artist = artistsList.get(i);
            System.out.println((i+1) + ". " + temp_artist.getName());
        }
        clientFavArtists = artistsList;
    }


    private void EstablishTopTracks() {
        // Gets the Users Top 20 Tracks
        PersonalizationAPI personalizationAPI = clientAPI.getPersonalization();
        SpotifyRestAction<PagingObject<Track>> restActionFavoriteTracks = personalizationAPI.getTopTracks();
        PagingObject<Track> pagingObjectFavTrack = restActionFavoriteTracks.complete();
        List<Track> trackList = pagingObjectFavTrack.getItems();
        System.out.println("\n");
        System.out.println("Your Fav Tracks: " );
        for (int i = 0; i < trackList.size(); i++) {
            Track temp_track = trackList.get(i);
            TracksAPI tracker = clientAPI.getTracks();
            TrackAudioFeatures tempfeatures = AudioFeatures(temp_track,tracker);
            top_track_features.add(tempfeatures);
            temp.add(tempfeatures);
            if (temp_track.getArtists().size() == 1) {
                System.out.println((i+1) + ". " + temp_track.getName() + " By: " + temp_track.getArtists().get(0).getName());
            } else {
                String artists = "";
                for (int j = 0; j < temp_track.getArtists().size(); j++) {
                    if (j != temp_track.getArtists().size() - 1) {
                        artists += temp_track.getArtists().get(j).getName() + ", ";
                    } else {
                        artists += temp_track.getArtists().get(j).getName();
                    }
                }
                System.out.println((i+1) + ". " + temp_track.getName() + " By: " + artists);
            }
        }
        WriteToDataFile("Client Top 20 Tracks Analysis");
        temp.clear();
        clientFavTracks = trackList;

    }

    // Creates Data Of The Library,Playlists, and Top Tracks
    private TrackAudioFeatures AudioFeatures (Track current_track, TracksAPI tracksAPI) {

        TracksAPI temp = clientAPI.getTracks();
        AudioFeatures current_track_features = temp.getAudioFeatures(current_track.getId()).complete();
        if (current_track_features == null ) {
            return null;
        }
        float danceability = current_track_features.getDanceability();
        float energy = current_track_features.getEnergy();
        float instrumentalness  = current_track_features.getInstrumentalness();
        float liveness = current_track_features.getLiveness();
        float acoustics = current_track_features.getAcousticness();
        float loudness = current_track_features.getLoudness();
        float tempo = current_track_features.getTempo();
        float speechiness = current_track_features.getSpeechiness();
        float valence = current_track_features.getValence();
        int mode = current_track_features.getMode();
        int duration = current_track_features.getDuration_ms();
        int popularity = current_track.getPopularity();
        String lyrics = "";
        //Checks if the function went through
        boolean exceptionCaught = false;
        String name = current_track.getName();
        String artist = current_track.getArtists().get(0).getName();
        try {
            LyricData lyricsparser = new LyricData();
            lyrics = lyricsparser.getSongLyrics(name,artist);
        } catch (Exception e) {
            lyrics = "Lyrics Not Found";
            exceptionCaught = true;
        }
        boolean lyricsRetrieved = false;
        int counter = 0;
        boolean artist_exception = false;
        if (exceptionCaught) {
            for (int i = 0; i < current_track.getArtists().size(); i++) {
                artist = "";
                for (int j = 0; j <= counter; j++) {
                    if (j == counter && j != 0 ) {
                        artist = artist + " and " + current_track.getArtists().get(j).getName();
                        //System.out.println(artist);
                    } else {
                        if (j != 0) {
                            artist = artist + " " + current_track.getArtists().get(j).getName();
                        } else {
                            artist = current_track.getArtists().get(0).getName();
                        }
                        //System.out.println(artist);
                    }
                }
                System.out.println(artist);
                name = current_track.getName();
                while (true) {
                    if (name.length() <= 1) {
                        break;
                    }
                    try {
                        LyricData lyricsparser = new LyricData();
                        lyrics = lyricsparser.getSongLyrics(name,artist);
                        artist_exception = false;
                        lyricsRetrieved = true;
                    } catch (Exception e) {
                        lyrics = "Lyrics Not Found";
                        artist_exception = true;
                    }
                    if (artist_exception) {
                        name = name.substring(0,name.length() - 1);
                    } else {
                        break;
                    }
                }
                if (lyricsRetrieved) {
                    break;
                }
                counter++;
            }
        }

        System.out.println(current_track.getName() + ": " + lyrics);
        TrackAudioFeatures audioFeatures = new TrackAudioFeatures(current_track,lyrics,danceability,energy,instrumentalness,liveness,acoustics,loudness,tempo,speechiness,valence,mode,duration,popularity);
        return audioFeatures;
    }
    private  boolean checkIfDataFileExists(String currentname) {
        try {
            FileInputStream filein = new FileInputStream("SpotifyDataFiles/PlaylistTrackData.xls");
            Workbook workbook = new HSSFWorkbook(filein);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                if (workbook.getSheetName(i).equals(currentname + " Song Data")) {
                    return true;
                }
            }
            return false;
        }  catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
    private void WriteToDataFile (String current_playlist_name) {
            try
            {
                FileInputStream filein = new FileInputStream("SpotifyDataFiles/PlaylistTrackData.xls");
                Workbook workbook = new HSSFWorkbook(filein);
                if (checkIfDataFileExists(current_playlist_name)) {
                    System.out.println("We have the file " + current_playlist_name + " in storage already");
                } else {
                    Sheet sheet = workbook.createSheet(current_playlist_name + " Song Data");
                    Map<String, Object[]> data = new TreeMap<String, Object[]>();
                    data.put(Integer.toString(1),new Object[] {"TRACK ID", " TRACK NAME", "ARTIST", "LYRICS","POPULARITY", "DANCEABLE", "ENERGY", "INSTRUMENTAL", "LIVENESS","ACOUSTICS","LOUDNESS"
                            ,"TEMPO","SPEECHINESS","VALENCE","MODE","DURATION"});
                    for (int i = 0; i < temp.size();  i++) {
                        TrackAudioFeatures audioFeatures = temp.get(i);
                        if (audioFeatures == null) {
                            continue;
                        }
                        data.put(Integer.toString(i + 2), new Object[] {audioFeatures.track.getId(),audioFeatures.track.getName(),audioFeatures.track.getArtists().get(0).getName(),audioFeatures.lyrics,audioFeatures.popularity,audioFeatures.dance,audioFeatures.energy,audioFeatures.instrumental,
                                audioFeatures.liveness,audioFeatures.acoustics, audioFeatures.loudness,audioFeatures.tempo, audioFeatures.speechiness,audioFeatures.valence,audioFeatures.mode,audioFeatures.duration});
                    }
                    Set<String> keyset = data.keySet();
                    int rownum = 0;
                    for (String key : keyset)
                    {
                        Row row = sheet.createRow(rownum++);
                        Object [] objArr = data.get(key);
                        int cellnum = 0;
                        for (Object obj : objArr)
                        {
                            Cell cell = row.createCell(cellnum++);
                            if(obj instanceof String)
                                cell.setCellValue((String)obj);
                            else if (obj instanceof Float)
                                cell.setCellValue((Float) obj);
                            else if(obj instanceof Integer)
                                cell.setCellValue((Integer)obj);
                        }
                    }
                    //Write the workbook in file system
                    FileOutputStream out = new FileOutputStream(new File("SpotifyDataFiles/PlaylistTrackData.xls"));
                    workbook.write(out);
                    workbook.close();
                    out.close();
                    System.out.println("Written successfully on disk.");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
    }












    // Getters
    public List<Artist> getClientFavArtists() {
        return clientFavArtists;
    }
    public List<SimplePlaylist> getClientPlaylists() {
        return clientPlaylists;
    }
    public List<Track> getClientFavTracks() {
        return clientFavTracks;
    }
    public SpotifyClientAPI getClientAPI() {
        return clientAPI;
    }
    public List<Playlist> getClientCreatedPlaylists() {
        return clientCreatedPlaylists;
    }
    public List<TrackAudioFeatures> getPlaylist_track_features() {
        return playlist_track_features;
    }

    public List<TrackAudioFeatures> getTop_track_features() {
        return top_track_features;
    }
}

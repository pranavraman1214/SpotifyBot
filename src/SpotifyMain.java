import com.adamratzman.spotify.*;
import com.adamratzman.spotify.main.SpotifyAPI;
import com.adamratzman.spotify.main.SpotifyClientAPI;
import com.adamratzman.spotify.main.SpotifyLogger;
import com.adamratzman.spotify.utils.SpotifyCopyright;
import com.adamratzman.spotify.utils.SpotifyRestAction;
import com.adamratzman.spotify.utils.SpotifyUserInformation;
import com.adamratzman.spotify.utils.Token;
import org.json.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.TokenQueue;

import java.awt.*;
import java.io.*;
import java.lang.module.Configuration;
import java.net.*;
import java.rmi.server.RemoteServer;
import java.util.Scanner;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class SpotifyMain {

    public static void main(String[] args) {
        // Creates the server which later acquires the access code for the authorization token
        System.out.println("\n");
        System.out.println("API Set Up Process: ");
        ServerClass myserver = new ServerClass();
        try {
            // Starts The Server
            myserver.CreateServer();
            // User Specific Variables
            //
            String secret_key = "";
            String client_id = "dd95ae3960a84df79fbea9fc82cbb640";
            String my_user_uri = "http://localhost:8000/spotifyaccesstokenrequest";

            // Establishes The Scope and what functions i wanted added to the project. In this case, I just added everything just to experiment
            SpotifyClientAPI.Scope[] my_scopes = new SpotifyClientAPI.Scope[10];
            my_scopes[0] = SpotifyClientAPI.Scope.PLAYLIST_MODIFY_PRIVATE;
            my_scopes[1] = SpotifyClientAPI.Scope.USER_READ_CURRENTLY_PLAYING;
            my_scopes[2] = SpotifyClientAPI.Scope.PLAYLIST_MODIFY_PUBLIC;
            my_scopes[3] = SpotifyClientAPI.Scope.USER_LIBRARY_READ;
            my_scopes[4] = SpotifyClientAPI.Scope.PLAYLIST_READ_PRIVATE;
            my_scopes[5] = SpotifyClientAPI.Scope.USER_READ_RECENTLY_PLAYED;
            my_scopes[6] = SpotifyClientAPI.Scope.USER_LIBRARY_MODIFY;
            my_scopes[7] = SpotifyClientAPI.Scope.USER_READ_PLAYBACK_STATE;
            my_scopes[8] = SpotifyClientAPI.Scope.PLAYLIST_READ_COLLABORATIVE;
            my_scopes[9] = SpotifyClientAPI.Scope.USER_TOP_READ;

            //Builds the authorization url and runs it on the server in order to obtain the OAuth Token
            String authorization = new SpotifyClientAPI.Builder(client_id,secret_key,my_user_uri).getAuthUrl(my_scopes);
            System.out.println(new URL(authorization).toString());
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(new URL(authorization).toString()));
            System.out.println("\n");
            System.out.println("Loading Authorization Code.......");
            TimeUnit.SECONDS.sleep(6);

            // I get the access code from the server/ close the server and run my ClientAPI functions
            String access_code = cropAccessCode(myserver.getRedirectURI());
            myserver.CloseServer();
            System.out.println("Your Authorization Code:" + access_code);
            System.out.println("\n");
            System.out.println("Loading Client API.....");
            TimeUnit.SECONDS.sleep(2);
            SpotifyClientAPI clientAPI = new SpotifyClientAPI.Builder(client_id,secret_key,my_user_uri).buildAuthCode(access_code,false);
            SpotifyClientBaseline client_bot = new SpotifyClientBaseline(clientAPI);
            PlaylistFunctions playlistFunctions = new PlaylistFunctions(client_bot);
            LyricData mylyrics = new LyricData();
        } catch (InterruptedException|IOException|URISyntaxException e) {
            e.printStackTrace();
            return;
        }

    }

    private static String cropAccessCode(String uncropped) {
        String cropped = uncropped.substring(5);
        return cropped;
    }



}




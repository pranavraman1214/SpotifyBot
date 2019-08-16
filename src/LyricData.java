import com.adamratzman.spotify.utils.SimpleTrack;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.DocumentAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneScore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class LyricData {
    public class WordFrequency {
        private String current_word;
        private int word_count;
        WordFrequency (String word, Integer times) {
            current_word = word;
            word_count =  times;
        }

        public String getCurrent_word() {
            return current_word;
        }

        public int getWord_count() {
            return word_count;
        }
    }
    public String getSongLyrics(String song_title, String song_artist) throws IOException {
        String geniusurl = "https://genius.com/";
        song_artist = Normalizer.normalize(song_artist,Normalizer.Form.NFD);
        song_artist = song_artist.replaceAll("[\\s]","-");
        song_artist = song_artist.trim();
        //song_artist = song_artist.replaceAll("[\\p{P}]","");
        song_artist = song_artist.replaceAll("[^\\p{Alnum}]", "-");
        song_title= Normalizer.normalize(song_title,Normalizer.Form.NFD);
        song_title = song_title.replaceAll("[\\p{P}]","");
        song_title = song_title.replaceAll("[^\\p{IsDigit}\\p{IsAlphabetic}\\p{Alnum}]", "-");
        song_title = song_title.replaceAll("[\\p{Space}]",  "-");
        geniusurl += song_artist + "-" + song_title + "-" + "lyrics";
        List<Integer> duplicatedashesindexes = new ArrayList<>();
        for (int i = 0; i < geniusurl.length(); i++) {
            if (geniusurl.charAt(i) == '-') {
                for (int j = i+1; j < geniusurl.length(); j++) {
                    if (geniusurl.charAt(j) == '-') {
                        duplicatedashesindexes.add(j);
                    } else {
                        break;
                    }
                }
            }
        }
        String genius_url_cropped = "";
        for (int q = 0; q < geniusurl.length(); q++) {
            if (duplicatedashesindexes.contains(q)) {
                continue;
            } else {
                genius_url_cropped += geniusurl.charAt(q);
            }
        }
        if (genius_url_cropped.toUpperCase().indexOf("-FEAT-",0) != -1){
            genius_url_cropped = genius_url_cropped.substring(0,genius_url_cropped.toUpperCase().indexOf("-FEAT-"));
            genius_url_cropped += "-lyrics";
        }
        geniusurl  = genius_url_cropped;
        //System.out.println(genius_url_cropped);
        Document doc = Jsoup.connect(geniusurl).get();
        Element e = doc.body();
        Elements elyrics = e.getElementsByClass("lyrics");
        String lyrics  = Jsoup.clean(elyrics.html(), Whitelist.none());
        return lyrics;
    }

    private int getFrequencyofSpecificWordInLyrics(String lyrics, String findword) {
        lyrics = lyrics.toUpperCase();
        findword = findword.toUpperCase();
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = lyrics.indexOf(findword,lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += lyrics.length();
            }
        }
        return count;
    }
    private List<WordFrequency> getWordFrequencyOfLyricsInOneSong (String lyrics) {
        lyrics = lyrics.replaceAll("[\\p{P}]","");
        String[] wordsInTheSong = lyrics.toUpperCase().split(" ");
        List<WordFrequency> words_found = new ArrayList<>();
        for (int i = 0; i < wordsInTheSong.length; i++)  {
            boolean word_found = false;
            String current_word = wordsInTheSong[i];
            for (int k = 0; k < words_found.size(); k++) {
                if (words_found.get(k).current_word.equals(current_word)) {
                    word_found = true;
                    break;
                }
            }
            if (!word_found) {
                int counter = 1;
                for (int  z = i + 1; z < wordsInTheSong.length; z++) {
                    if (wordsInTheSong[z].equals(current_word)) {
                        counter++;
                    }
                }
                WordFrequency wordFrequencyCreate = new WordFrequency(current_word,counter);
                words_found.add(wordFrequencyCreate);
            }
        }
        return words_found;
    }

    public double getToneOfLyrics(String lyrics) {
        ToneAnalyzer service = new ToneAnalyzer("2017-09-21");
        IamOptions options = new IamOptions.Builder()
                .apiKey("")
                .build();
        service.setIamCredentials(options);

        ToneOptions toneOptions = new ToneOptions.Builder()
                .html(lyrics)
                .build();

        DocumentAnalysis tone = service.tone(toneOptions).execute().getDocumentTone();

        System.out.println(tone.getTones().get(0));
        return tone.getTones().get(0).getScore();
    }

}

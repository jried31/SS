package edu.ucla.cs218.crawler;

import com.mongodb.util.JSON;
import database.parse.util.DBGlobals;
import edu.ucla.cs218.crawler.Controller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import wordnet.wordnet;

/**
 *
 * @author Alvin
 */

/* 
*  This class takes a sentence as input and produces a list of possible
*  phenomenon or sensor matches as output.
*/ 
public class MatchFinder {
    
    private wordnet wordNet;
    
    public List<String> FindMatches(String text) throws IOException {
        String splitOn = "[ ]+";                // Split on spaces
        String[] words = text.split(splitOn);   // Stores all tokenized words
        String currentWord;                     // Current word being parsed
        List<String> possibleMatches = new ArrayList<String>();        // Stores matches found by autocomplete
        List<String> matches = new ArrayList<String>();                // Stores found matches
        
        // Iterate through all words to find matches
        for(int i = 0; i < words.length; i++) {
            currentWord = words[i];
            if(!wordNet.isNoun(currentWord))
               continue;
            
            if(getMatches(currentWord, possibleMatches)) {
                for(String pMatch : possibleMatches) {
                    if(text.contains(pMatch))
                        matches.add(pMatch);
                }
            }
        }
        return matches;
    }
    
    public boolean getMatches(String word, List<String> possibleMatches ) {
        possibleMatches.clear();
       //Grab the Autocomplete Bag of words from Google Suggest
            JSONArray data = null;
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            
        try {
            String str = DBGlobals.URL_GOOGLE_SEARCH.replace("WORD", word);
            URL url = new URL(str);
            
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty(
                  "User-Agent",
                  "Mozilla/5.0 (X11; U; Linux x86_64; en-GB; rv:1.8.1.6) Gecko/20070723 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1)");
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();

            String line = null;
            while(( line = br.readLine()) != null){
                sb.append(line);
            }

            br.close();
            iStream.close();
            urlConnection.disconnect();
            
            //Convert the String array to a JSON array
            data =  new JSONArray(sb.toString());
            
            //Get the bag of words: NOTE - Index 0 = search term | Index 1 = bag of words
            //We want the bag of words
            data = data.getJSONArray(1);
            System.out.println(data);
            
            /*First, validate whether the search term matches...If no match then run though the bag of words
            Basically my point is query for the Search term 1st to see if it matches a Sensor or Phenomena
            If not then search the bag of words ... Reason why is
            ["jim",["jimmy johns","jimmy kimmel","jimmy fal
              ^                 ^
              |                 |
            Search Term    Bag of Words
            NOTICE the Bad of Words does not have "jim" in it.
            
            if(word == PHENOMENA | SENSOR) {Do somethign here}
            else {
            */
            if(Controller.isSensor(word) || Controller.isPhenomenon(word))
                possibleMatches.add(word);
            
            for (int k = 0; k < data.length(); k++) {
                String term = data.getString(k);
                System.out.println(term);
                if(Controller.isSensor(term) || Controller.isPhenomenon(term))
                    possibleMatches.add(term);
            }
            
            if(possibleMatches.isEmpty())
                return false;
            
            return true;
            /* } */
        } catch (Exception ex) {
            // handle exception here
            ex.printStackTrace();
        }
        
        
        
        /*      GOOGLE AUTOCOMPLETE CODE
        * TAKE FIRST 3 AUTOCOMPLETE WORDS FOR THE WORD INPUT AND ADD TO MATCHES
        * autocomplete(word, matches);
        *
        *       WORDNET MATCHES
        * USE MATCHES FOUND BY GOOGLE AUTOCOMPLETE AND ADD MATCHES FOUND BY
        * WORDNET USING THOSE MATCHES
        * wordnet(matches);
        */
        return false;
    }
    
    public static void main(String []art){
        MatchFinder match = new MatchFinder();
        List<String> possibleMatches = new ArrayList<String>();
        match.getMatches("temperature", possibleMatches);
        for(String poop : possibleMatches)
            System.out.println(poop);
        
    }
}


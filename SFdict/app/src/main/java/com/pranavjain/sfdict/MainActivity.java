package com.pranavjain.sfdict;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class MainActivity extends AppCompatActivity {

    Button button;
    int flag;
    EditText editText, editTextLanguage;
    String word, lang;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        callAPI();
    }



    public void callAPI(){

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText = (EditText) findViewById(R.id.editText);
                word = editText.getText().toString().toLowerCase();
                editTextLanguage = (EditText) findViewById(R.id.editTextLanguage);
                lang = editTextLanguage.getText().toString().toLowerCase();
                if (!lang.equals("hindi") && !lang.equals("english"))
                    notSupported();
                else {
                    new doStuff().execute();
                }
            }
        });
    }

    private void notSupported() {
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("Language not supported");
    }

    class doStuff extends AsyncTask<Void, Void, String>{


        @Override
        protected String doInBackground(Void... params) {

            String src, dest;

            String apiurl = "https://glosbe.com/gapi/translate?format=json";
            Log.i("word", word);
            if(lang.equals("hindi")){
                src = "hi";
                dest = "en";
                flag = 1;
            }
            else{
                src = "en";
                dest = "hi";
                flag = 0;
            }
            URL url = null;
            try {
                url = new URL(apiurl+"&from="+src+"&dest="+dest+"&phrase="+word);
                Log.i("url", url.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            }
            catch (Exception e){
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }

            //return null;

        }

        @Override
        protected void onPostExecute(String response) {
            TextView textView = (TextView) findViewById(R.id.textView);

            if(response == null) {
                //response = "THERE WAS AN ERROR";
                textView.setText("ERROR (no API response)");
            }
            //progressBar.setVisibility(View.GONE);
            //Log.i("INFO", response);
            else{
                parseJSON(response);
            }
            super.onPostExecute(response);
        }
    }

    public static boolean isPureAscii(String v) {
        byte bytearray []  = v.getBytes();
        CharsetDecoder d = Charset.forName("US-ASCII").newDecoder();
        try {
            CharBuffer r = d.decode(ByteBuffer.wrap(bytearray));
            r.toString();
        }

        catch(CharacterCodingException e) {
            return false;
        }
        return true;



    }

    private void parseJSON(String response) {
        String answer="",definition="",allDefs="",translatedText="";
        TextView textView = (TextView) findViewById(R.id.textView);
        int i=0;
        try {
            JSONObject root = new JSONObject(response);
            JSONArray tuc = root.optJSONArray("tuc");
            if(tuc.length() == 0){
                answer = "ERROR (word error)";
            }
            else {
                JSONObject element = tuc.getJSONObject(i);
                JSONObject phrase = element.getJSONObject("phrase");
                translatedText = phrase.optString("text");
                //JSONObject meanings = tuc.getJSONObject(1);
                for (i = 0; i < tuc.length(); i++) {
                    element = tuc.getJSONObject(i);
                    JSONArray meaning = element.optJSONArray("meanings");
                    if (meaning == null) continue;
                    JSONObject textualMeaning = meaning.getJSONObject(0);
                    definition = textualMeaning.optString("text");
                    if (flag == 1) {
                        if (!isPureAscii(definition))
                            allDefs = allDefs + definition + "\n";
                    } else {
                        //if (isPureAscii(definition))
                        allDefs = allDefs + definition + "\n";
                    }
                }
                answer = translatedText + "\n" + allDefs;
            }
            /*JSONArray meaning = element.optJSONArray("meanings");
            while(meaning == null){
                i++;
                element = tuc.getJSONObject(i);
                meaning = element.optJSONArray("meanings");
            }*/

            //answer = translatedText + "\n" + allDefs;

        } catch (JSONException e) {
            e.printStackTrace();
            textView.setText("exception");
        }

        textView.setText(answer);

    }


}

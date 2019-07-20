package com.example.translit;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.example.translit.GlobalVars.BASE_REQ_URL;
import static com.example.translit.GlobalVars.DEFAULT_LANG_POS;
import static com.example.translit.GlobalVars.LANGUAGE_CODES;
public class Translation extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int REQ_CODE_SPEECH_INPUT=1;
    private  TextToSpeech mTextToSpeech;
    private Button translate;
    private Spinner spinnerFrom,spinnerTo;
    private EditText mTextInput;
    private ImageView clear;
    private ImageView speak,texttospeech;
    private Dialog process_tts;
    private TextView mTextTranslated;
    private String mLanguageCodeFrom = "en";
    private String mLanguageCodeTo = "en";
    volatile boolean activityRunning;
    HashMap<String, String> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);
        activityRunning=true;
        mTextInput=findViewById(R.id.from_text);
        clear=(ImageView)findViewById(R.id.clear);
        spinnerFrom=findViewById(R.id.spinner_from);
        spinnerTo=findViewById(R.id.spinner_to);
        speak=(ImageView)findViewById(R.id.speak);
        texttospeech=(ImageView)findViewById(R.id.texttospeech);
        mTextTranslated=(TextView)findViewById(R.id.to_text);
        mTextTranslated.setMovementMethod(new ScrollingMovementMethod());
        translate=findViewById(R.id.translate);
        mTextToSpeech = new TextToSpeech(this, this);
        process_tts = new Dialog(Translation.this);
        process_tts.setContentView(R.layout.dialog_processing_tts);
        process_tts.setTitle("switching languages");
        TextView title = (TextView) process_tts.findViewById(android.R.id.title);



        if(!isOnline()){
            Toast.makeText(Translation.this,"CHECK INTERNET CONNECTIVITY",Toast.LENGTH_SHORT).show();
        }
        else
        {
            new GetLanguages().execute();
            speak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mLanguageCodeFrom);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "en");
                    try {
                        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                    } catch (ActivityNotFoundException a) {
                        Toast.makeText(getApplicationContext(), "language not supported", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            texttospeech.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    speakOut();
                }
            });




            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTextInput.setText("");
                    mTextTranslated.setText("");
                }
            });






            translate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String input = mTextInput.getText().toString();
                    new TranslateText().execute(input);
                }
            });






            spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mLanguageCodeFrom = LANGUAGE_CODES.get(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Toast.makeText(getApplicationContext(), "No option selected", Toast.LENGTH_SHORT).show();
                }
            });
            //  SPINNER LANGUAGE TO
            spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mLanguageCodeTo = LANGUAGE_CODES.get(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Toast.makeText(getApplicationContext(), "No option selected", Toast.LENGTH_SHORT).show();
                }
            });





        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case REQ_CODE_SPEECH_INPUT:{
                if (resultCode == RESULT_OK && null != data) {
                    /*
                            Dialog box to show list of processed Speech to text results
                            User selects matching text to display in chat
                     */
                    final Dialog match_text_dialog = new Dialog(Translation.this);
                    match_text_dialog.setContentView(R.layout.dialog_matches_frag);
                    match_text_dialog.setTitle("select the matching string");
                    ListView textlist = (ListView)match_text_dialog.findViewById(R.id.list);
                    final ArrayList<String> matches_text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,matches_text);
                    textlist.setAdapter(adapter);
                    textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            mTextInput.setText(matches_text.get(position));
                            match_text_dialog.dismiss();
                        }
                    });
                    match_text_dialog.show();
                    break;
                }
            }
        }
    }






    @Override
    public void onInit(int status) {
        Log.e("Inside----->", "onInit");
        if (status == TextToSpeech.SUCCESS) {
            int result = mTextToSpeech.setLanguage(new Locale("en"));
            if (result == TextToSpeech.LANG_MISSING_DATA) {
                Toast.makeText(getApplicationContext(), "Language not supported", Toast.LENGTH_SHORT).show();
            } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(getApplicationContext(), "language not supported", Toast.LENGTH_SHORT).show();
            }
            speak.setEnabled(true);
            mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.e("Inside","OnStart");
                    process_tts.hide();
                }
                @Override
                public void onDone(String utteranceId) {
                }
                @Override
                public void onError(String utteranceId) {
                }
            });
        } else {
            //Log.e(LOG_TAG,"TTS Initilization Failed");
        }
    }







    private class TranslateText extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... input) {
            Uri baseUri = Uri.parse(BASE_REQ_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendPath("translate")
                    .appendQueryParameter("key",getString(R.string.API_KEY))
                    .appendQueryParameter("lang",mLanguageCodeFrom+"-"+mLanguageCodeTo)
                    .appendQueryParameter("text",input[0]);
            Log.e("String Url ---->",uriBuilder.toString());
            return QueryUtils.fetchTranslation(uriBuilder.toString());
        }
        @Override
        protected void onPostExecute(String result) {
            if(activityRunning) {
                mTextTranslated.setText(result.toString());
            }
        }
    }










    private void speakOut(){
        int result = mTextToSpeech.setLanguage(new Locale(mLanguageCodeTo));
        if (result == TextToSpeech.LANG_MISSING_DATA ){
            Toast.makeText(getApplicationContext(),"language feature not supported",Toast.LENGTH_SHORT).show();
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        } else if(result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(getApplicationContext(),"langusge not supported",Toast.LENGTH_SHORT).show();
        } else {
            String textMessage = mTextTranslated.getText().toString();
            process_tts.show();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
            mTextToSpeech.speak(textMessage, TextToSpeech.QUEUE_FLUSH, map);
        }
    }







    private class GetLanguages extends AsyncTask<Void,Void,ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            Uri baseUri = Uri.parse(BASE_REQ_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendPath("getLangs")
                    .appendQueryParameter("key",getString(R.string.API_KEY))
                    .appendQueryParameter("ui","en");
            Log.e("String Url ---->",uriBuilder.toString());
            return QueryUtils.fetchLanguages(uriBuilder.toString());
        }
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (activityRunning) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(Translation.this, android.R.layout.simple_spinner_item, result);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFrom.setAdapter(adapter);
                spinnerTo.setAdapter(adapter);
                //  SET DEFAULT LANGUAGE SELECTIONS
                spinnerFrom.setSelection(DEFAULT_LANG_POS);
                spinnerTo.setSelection(DEFAULT_LANG_POS);
            }
        }
    }










    public  boolean isOnline()
    {   try {
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    } catch (Exception e) {
        Toast.makeText(Translation.this,"something went wrong",Toast.LENGTH_SHORT).show();
    }
        return false;
    }
}

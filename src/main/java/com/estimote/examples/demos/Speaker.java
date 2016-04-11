package com.estimote.examples.demos; /**
 * Created by Alex on 4/9/2016.
 */
import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class Speaker implements OnInitListener {
    private final float DEFAULT_RATE = 1.0f;
    private TextToSpeech tts;
    private boolean ready = false;
    private boolean allowed = true;
    private Context context;
    public Speaker(Context context){
        this.context = context;
        tts = new TextToSpeech(context, this);
    }

//    public boolean isAllowed(){
//        return allowed;
//    }
//    public void allow(boolean allowed){
//        this.allowed = allowed;
//    }
    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            // Change this to match your
            // locale
//            tts.setLanguage(Locale.US);
            tts.setSpeechRate(DEFAULT_RATE);
            ready = true;
        }else{
            Toast.makeText(this, "Sorry! Text To Speech failed...",Toast.LENGTH_LONG).show();
            ready = false;
        }
    }
    public void speak(String text, boolean toFlush){

        // Speak only if the TTS is ready
        // and the user has allowed speech

        if(ready) {
            HashMap<String, String> hash = new HashMap<String,String>();
            hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                    String.valueOf(AudioManager.STREAM_NOTIFICATION));
            if(toFlush){
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, hash);
            }else{
                tts.speak(text, TextToSpeech.QUEUE_ADD, hash);
            }
        }else{
            Log.e("tts", "not ready!!!");
        }
    }

    public void pause(int duration){
        tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }

    // Free up resources
    public void destroy(){
        tts.shutdown();
    }
}

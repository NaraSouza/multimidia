package com.example.nara.soundeffect;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.ConstantQ;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.resample.RateTransposer;

public class MainActivity extends AppCompatActivity {
    private AudioDispatcher dispatcher;
    private WaveformSimilarityBasedOverlapAdd wsola;
    private GainProcessor gain;
    private RateTransposer rateTransposer;
    private double currentFactor;// pitch shift factor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //Requisitando permissao
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            startFile();

        }
    }

    private void startFile() {
        if (dispatcher != null) {
            dispatcher.stop();
        }

        double rate = 1.0;
        rateTransposer = new RateTransposer(rate);
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
        wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(rate, 2048));

        wsola.setDispatcher(dispatcher);
        dispatcher.addAudioProcessor(wsola);
        dispatcher.addAudioProcessor(rateTransposer);
        dispatcher.addAudioProcessor(new AndroidAudioPlayer(dispatcher.getFormat()));
        dispatcher.addAudioProcessor(new AudioProcessor() {
            @Override
            public void processingFinished() {
                dispatcher = null;
                startFile();
            }

            @Override
            public boolean process(AudioEvent audioEvent) {
                return true;
            }
        });

        Thread t = new Thread(dispatcher);
        t.start();
    }

}

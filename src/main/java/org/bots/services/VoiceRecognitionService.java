package org.bots.services;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Service
public class VoiceRecognitionService {

    private RecognitionConfig config;


    @PostConstruct
    private void init()
    {
        this.config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.OGG_OPUS)
                .setSampleRateHertz(16000)
                .setLanguageCode("ru-RU")
                .build();

    }
    public String recognizeVoice(File file){
        String returnString = null;
        try (SpeechClient speechClient = SpeechClient.create()) {

            ByteString audioBytes = ByteString.readFrom(new FileInputStream(file));
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            RecognizeResponse response = speechClient.recognize(this.config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                returnString =  alternative.getTranscript();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnString;
    }

}

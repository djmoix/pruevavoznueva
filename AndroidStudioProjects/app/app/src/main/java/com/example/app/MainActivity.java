package com.example.app;

// https://dialogflow.com usuario:parcticasit@qualitytelecom.es pass:Practicas_2019 agente:app_tiempo
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast; //crea etiquetas a modo de alertas
import android.Manifest;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;// transforma texto en palabras
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.google.gson.JsonElement;
import ai.api.AIListener; // abre la escucha de la aplicacion
import ai.api.android.AIConfiguration;// configura la clave dialogflow
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import java.io.IOException;
import java.lang.String;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity implements AIListener {


    private AIService mAIService;
    private TextToSpeech mTextToSpeech;
    private static final int REQUEST_INTERNET = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //funcion que sirve para validar permisos, en este caso permiso de microfono
        validacion();
        //configuramos dialogflow, importamos token del cliente, para poder tener acceso a nuestros intents
        //en nuestro perfil de dialogflow
        final AIConfiguration config = new AIConfiguration("57c23b09147f469a99fcc985a6d40fa5",
                AIConfiguration.SupportedLanguages.Spanish,
                AIConfiguration.RecognitionEngine.System);
        //activamos el servicio escucha
        mAIService = AIService.getService(this, config);
        mAIService.setListener(this);
        //se activa directamente con la apertura de la aplicacion
        mAIService.startListening();
        //activamos servicio de voz(respuesta)
        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

            }
        });
        //activamos el boton, esta es la primera opcion que contemplamos de activacion
        /*findViewById(R.id.btn_micro).setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
                mAIService.startListening();

            }
        });*/

    }

    private void validacion() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_INTERNET);
        }
    }

    String url = "https://www.eltiempo.es/";
    //funcion del AiListener
    @Override
    public void onResult(AIResponse response) {
        //devuelve respuesta hablada
        Result result = response.getResult();
        mTextToSpeech.speak(result.getFulfillment().getSpeech(), TextToSpeech.QUEUE_FLUSH, null, null);

        //usamos la accion definida en dialogoflow para activar if
        if (result.getAction().equals("hoy")) {
            String pag = "";
            if (result.getParameters() != null && !result.getParameters().isEmpty()) {
                for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                    pag += entry.getValue();
                }
                String pag1 = pag.substring(1, pag.length() - 1);
                //en el caso de que sea mas de una palabra sustituimos los espacios por guiones para que lo coga la url
                String pag2 = pag1.replace(" ", "-");
                //aqui obtenemos la url definitiva que se la enviamos al browser para mostrar la pagina
                Intent intent;
                intent = new Intent(this, browser.class);
                String resultado = url + pag2 + ".html";
                intent.putExtra("resultado", resultado);
                startActivity(intent);
                //siguiente paso cogemos informacion de la pagina web y con el metodo TextToSpeech la leemos
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //creamos los construstores donde se guardara la informacion
                        final StringBuilder constructor1 = new StringBuilder();
                        final StringBuilder constructor2 = new StringBuilder();
                        final StringBuilder constructor3 = new StringBuilder();

                        try {
                            //conectamos con la pagina web "guardandola" en doc
                            Document doc = Jsoup.connect(resultado).get();
                            //creamos los elements guardando las etiquetas donde vamos a ir a buscar la informacion de la pagina web
                            Elements obtg_temp = doc.select("span.c-tib-text");
                            Elements obtg_nub= doc.select("section.c-pois-text");
                            Elements obtg_vien= doc.select("section.c-pois-wind");


                            //estos for hacen el bucle de busqueda hasta que los dos son igualados y guardamos la informacion
                            for (Element span : obtg_temp) {
                                constructor1.append(" en este momento La temperatura es ").append(span.attr("span")).append(span.text());
                            }
                            for (Element span : obtg_nub) {
                                constructor2.append(" el cielo esta").append(span.attr("section")).append(span.text());
                            }
                            for (Element span : obtg_vien) {
                                constructor3.append("y con un ").append(span.attr("span")).append(span.text());
                            }




                        } catch (IOException e) {
                            constructor1.append("Error : ").append(e.getMessage());
                        }
                        //aqui ya tenemos la informacion y el programa nos responde
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {                                                                               //el textospeech se añade al ya en ejecucion evitando que se pisen
                                mTextToSpeech.speak((constructor1.toString()+constructor2.toString()+constructor3.toString()),TextToSpeech.QUEUE_ADD, null, null);

                            }
                        });
                    }
                }).start();
            }
        }

    }

    //estas funciones las crea AIListener para posibles usos seria buena idea intentar saber mas al respecto

    @Override
    public void onError(AIError error) {


    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //aqui estan las diferentes estados que hace nuestra aplicacion mas informacion del flujo https://www.youtube.com/watch?v=poipVVd2jzU
    //importante investigar mas al respecto el oncreate se encuentra arriba
    @Override
    protected void onStart() {
        super.onStart();

        // La actividad está a punto de hacerse visible.
    }
    @Override
    protected void onResume() {
        super.onResume();

        // La actividad se ha vuelto visible (ahora se "reanuda").
    }
    @Override
    protected void onPause() {
        super.onPause();

        // Enfocarse en otra actividad  (esta actividad está a punto de ser "detenida").
    }
    @Override
    protected void onStop() {
        super.onStop();

        // La actividad ya no es visible (ahora está "detenida")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // La actividad está a punto de ser destruida.
    }


}




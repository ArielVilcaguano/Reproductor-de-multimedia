package com.marticurto.reproductormultimedia

import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.lang.Thread.sleep


class MainActivity : AppCompatActivity() {
    private lateinit var spSource:Spinner
    private lateinit var btPlay: Button
    private lateinit var btPause: Button
    private lateinit var btStop: Button
    private lateinit var ibRewind: ImageButton
    private lateinit var ibFastForward: ImageButton
    private lateinit var tvState : TextView
    private lateinit var tvTimer:TextView
    private lateinit var tvDuration:TextView
    private lateinit var tvMetadata:TextView
    private lateinit var tvMetadata2:TextView
    private lateinit var seekBar: SeekBar
    private lateinit var videoView:VideoView

    private lateinit var mpMusic1: MediaPlayer
    private lateinit var mpMusic2: MediaPlayer




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spSource = findViewById(R.id.spSource)
        btPlay = findViewById(R.id.btplay)
        btPause = findViewById(R.id.btPause)
        btStop = findViewById(R.id.btStop)
        ibRewind = findViewById(R.id.ibRewind)
        ibFastForward = findViewById(R.id.ibFastForward)
        tvState = findViewById(R.id.tvState)
        tvMetadata = findViewById(R.id.tvMetadata)
        tvMetadata2 = findViewById(R.id.tvMetadata2)
        tvTimer = findViewById(R.id.tvTimer)
        tvDuration =findViewById(R.id.tvDuration)
        seekBar = findViewById(R.id.seekBar)
        videoView = findViewById(R.id.videoView)



        val myUri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.musica3)
        mpMusic1 = MediaPlayer.create(this, myUri)

        val myUri2: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.music2)
        mpMusic2 = MediaPlayer()
        mpMusic2.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mpMusic2.setDataSource(applicationContext,myUri2)
        mpMusic2.prepare()

        val videoUri:Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.video2)
        videoView.setVideoURI(videoUri)

        /*
        si queremos unos controles pror defecto podemos activar esto
        en nuestro caso no sera necesario ya que los creamos nosotros

        val vidControl = MediaController(this)
        vidControl.setAnchorView(videoView)
        videoView.setMediaController(vidControl)*/



        btPlay.text="play"
        btPause.text="Pause"
        btPause.isEnabled=false
        btStop.text="stop"
        btStop.isEnabled=false
        tvTimer.text="00:00"
        fillSpinner(spSource)
        tvDuration.text=getDuration(mpMusic1)


        spSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                initialState(mpMusic1,mpMusic2, videoView)
                initializeSeekbar(mpMusic1,mpMusic2,videoView)
            }
        }


        controlSound(mpMusic1,mpMusic2,videoView)


        btPlay.setOnClickListener { starPlay(mpMusic1,mpMusic2,videoView) }
        btPause.setOnClickListener { pauseReproduction(mpMusic1,mpMusic2,videoView) }
        btStop.setOnClickListener { stopReproduction(mpMusic1,mpMusic2,videoView) }
        ibRewind.setOnClickListener { rewind(mpMusic1,mpMusic2,videoView) }
        ibFastForward.setOnClickListener { fastFroward(mpMusic1,mpMusic2,videoView) }


        Handler().postDelayed({
            startActivity(Intent(MainActivity@this,PopUpSubcribe::class.java))
        }, 5000)

    }


    override fun onDestroy() {
        super.onDestroy()
        mpMusic1.stop()
        mpMusic2.stop()
        videoView.stopPlayback()
        videoView.resume()

    }



    /**
     * Rellena el spinner con Strings prediseñadas

     */
    private fun fillSpinner(sp:Spinner){
        val sources = arrayOf("Musica mediante \"create\"","Musica mediante \"setDataSource\"","Video")
        val adapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, sources)
        sp.adapter=adapter
    }

    /**
     * Deja el estado inicial de los botones y los mediaplayers preparado
     */
    private fun initialState(mp1: MediaPlayer, mp2: MediaPlayer, video: VideoView){

        btPlay.isEnabled=true
        btStop.isEnabled=false
        btPause.isEnabled=false

        if(mp1.isPlaying){
            mp1.stop()
            mp1.prepare()
        }
        if (mp2.isPlaying){
            mp2.stop()
            mp2.prepare()
        }
        if(video.isPlaying){
            video.stopPlayback()
            video.resume()
        }

        if(spSource.selectedItemPosition==0)tvDuration.text=getDuration(mp1)
        if(spSource.selectedItemPosition==1)tvDuration.text=getDuration(mp2)
        if(spSource.selectedItemPosition==2) {

            var duration = video.duration.toLong()/1000
            tvDuration.text=duration.toString()
        }



        val myMetadata=obtainMetadata()
        tvMetadata.text=myMetadata[0]
        tvMetadata2.text=myMetadata[1]
    }

    /**
     * obtenemos metadoatos de las fuentes
     */
    private fun obtainMetadata():Array<String>{

        var metadata= MediaMetadataRetriever()
        val myMetaData:Array<String>
        val title:String?
        val genre:String?


        if(spSource.selectedItemPosition==0) {
            metadata.setDataSource(
                MainActivity@ this,
                Uri.parse("android.resource://" + packageName + "/" + R.raw.musica3)
            )
            title = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            genre= metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            myMetaData = arrayOf("Titulo: $title","Genero: $genre")
        }else if(spSource.selectedItemPosition==1){
            metadata.setDataSource(
                MainActivity@ this,
                Uri.parse("android.resource://" + packageName + "/" + R.raw.music2)
            )
            title = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            genre= metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            myMetaData = arrayOf("Titulo: $title","Genero: $genre")
        }else{
            metadata.setDataSource(
                MainActivity@ this,
                Uri.parse("android.resource://" + packageName + "/" + R.raw.video2)
            )
            val videoMetadata:String?=metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val videoMetadata2:String?=metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            myMetaData = arrayOf("Titulo: $videoMetadata","ancho del video: $videoMetadata2")
        }
        return myMetaData
    }



    private fun obtainSource(sp:Spinner):Int{
        return sp.selectedItemPosition
    }

    /**
     * Obtenemos la duracion de la cancion
     */
    private fun getDuration(mp: MediaPlayer):String{
        val duration:Long = (mp.duration.toLong() / 1000)
        return duration.toString()
    }



    /**
     * Empieza a reproducir una musica o video
     */
    private fun starPlay(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView) {
        tvState.text = "Playing"


        btPlay.isEnabled = false
        btPause.isEnabled = true
        btStop.isEnabled = true


        if (obtainSource(spSource) == 0) {
            mp1.start()
        }else if(obtainSource(spSource)==1){
            mp2.start()
        }else{
            video.start()
        }
    }

    /**
     * Tpausa la reproduccion del medio seleccionado
     */
    private fun pauseReproduction(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView){
        tvState.text="Paused"


        btPause.isEnabled=false
        btPlay.isEnabled=true
        btStop.isEnabled=true


        if (obtainSource(spSource) == 0) {
            mp1.pause()
        }else if(obtainSource(spSource)==1){
            mp2.pause()
        }else{
            video.pause()
        }
    }

    /**
     * Paramos la reproduccion del medio que este seleccionado
     */
    private fun stopReproduction(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView){
        tvState.text="Stopped"


        btStop.isEnabled=false
        btPause.isEnabled=false
        btPlay.isEnabled=true


        if (obtainSource(spSource) == 0) {
            mp1.stop()
            mp1.prepare()
        }else if(obtainSource(spSource)==1){
            mp2.stop()
            mp2.prepare()
        }else{
            video.stopPlayback()
            video.resume()
        }
    }

    /**
     * Retrocede la musica 10s
     */
    private fun rewind(mp1: MediaPlayer, mp2: MediaPlayer, video: VideoView) {
        //retrocede la posicion de la fuente 10000ms
        if (obtainSource(spSource) == 0) {
            val position = mp1.currentPosition
            mp1.seekTo(position - 10000)
        } else if (obtainSource(spSource) == 1) {
            val position = mp2.currentPosition
            mp2.seekTo(position - 10000)
        } else {
            val position = videoView.currentPosition
            video.seekTo(position - 10000)
        }
    }


    private fun fastFroward(mp1:MediaPlayer, mp2:MediaPlayer, video:VideoView){
        //retrocede la posicion de la fuente 10000ms
        if(obtainSource(spSource)==0){
            val position =mp1.currentPosition
            mp1.seekTo(position+10000)
        }else if(obtainSource(spSource)==1){
            val position=mp2.currentPosition
            mp2.seekTo(position+10000)
        }else{
            val position =videoView.currentPosition
            video.seekTo(position+10000)
        }
    }


    private fun controlSound(mp1: MediaPlayer, mp2: MediaPlayer, video: VideoView) {


        initializeSeekbar(mp1, mp2, videoView)


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {

                    if(spSource.selectedItemPosition==0)mp1.seekTo(progress)
                    if(spSource.selectedItemPosition==1)mp2.seekTo(progress)
                    if(spSource.selectedItemPosition==2)video.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


    private fun initializeSeekbar(mp1: MediaPlayer, mp2: MediaPlayer, video: VideoView){
        //definimos el maximo segun la fuente
        when (spSource.selectedItemPosition) {
            0 -> {
                seekBar.max =mp1.duration
            }
            1 -> {
                seekBar.max =mp2.duration
            }
            else -> {
                seekBar.max =video.duration
            }
        }

        //actualizamos la barra de progreso y el texto cada 0,1s
        val handler = Handler()
        handler.postDelayed(object :Runnable{
            override fun run() {
                try {
                    when (spSource.selectedItemPosition) {
                        0 -> {
                            seekBar.progress = mp1.currentPosition
                            tvTimer.text = (mp1.currentPosition / 1000).toString()
                        }
                        1 -> {
                            seekBar.progress = mp2.currentPosition
                            tvTimer.text = (mp2.currentPosition/1000).toString()
                        }
                        else -> {
                            seekBar.progress = video.currentPosition
                            tvTimer.text = (video.currentPosition/1000).toString()
                        }
                    }
                    handler.postDelayed(this,100)
                }catch (e:Exception){
                    seekBar.progress=0
                }
            }
        },0)
    }
}
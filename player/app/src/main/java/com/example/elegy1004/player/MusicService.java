package com.example.elegy1004.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

//音乐服务，实现音乐的后台播放
public class MusicService extends Service {
    private static final String TAG = MusicService.class.getSimpleName();

    public static final String ACTION_UPDATE_PROGRESS = "com.macernow.djstava.djmusic.UPDATE_PROGRESS";
    public static final String ACTION_UPDATE_DURATION = "com.macernow.djstava.djmusic.UPDATE_DURATION";
    public static final String ACTION_UPDATE_CURRENT_MUSIC = "com.macernow.djstava.djmusic.UPDATE_CURRENT_MUSIC";


    private int currentMode = 1; //default all loop当前循环模式

    public static final int MODE_ONE_LOOP = 0;       //循环
    public static final int MODE_ALL_LOOP = 1;       //全部循环
    public static final int MODE_RANDOM = 2;         //随机
    public static final int MODE_SEQUENCE = 3;      //顺序

    private static final int updateProgress = 1;
    private static final int updateCurrentMusic = 2;
    private static final int updateDuration = 3;

    private Notification notification;  //通知

    private MediaPlayer mediaPlayer;//mediaplayer对象

    private int currentIndex = 0;
    private int currentPosition = 0;
    private boolean isPlaying = false;

    private ArrayList<File> mMusicFilesList = new ArrayList<File>();  //音乐文件所在文件路径数组？

    private AudioManager audioManager;  //提供音量控制和和振铃器mode控制
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = null;
    //监听音频资源是否被抢占的监听器，比如当我们播放音乐的时候打开视频播放
    private final IBinder musicBinder = new MusicBinder();


    /*
    * 启动service
    * */
    public MusicService() {
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case updateProgress:
                    toUpdateProgress();
                    break;
                case updateDuration:
                    toUpdateDuration();
                    break;
                case updateCurrentMusic:
                    toUpdateCurrentMusic();
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");

        Log.e(TAG, "service onBind.");

        return musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "service onUnbind.");
        super.onUnbind(intent);

        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "service onRebind.");
        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate.");

        //初始化音频焦点
        initAudioFocusListener();

        //初始化播放器
        initMediaPlayer();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*
        * 获取activity里传递过来的ArrayList<File>
        * */
        Bundle bundle = intent.getExtras();
        mMusicFilesList.clear();

        mMusicFilesList = (ArrayList<File>) bundle.getSerializable("musicFileList");
        currentIndex = 0;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        abandonAudioFocus();

        super.onDestroy();
        Log.e(TAG, "onDestroy.");
    }

    private void initAudioFocusListener() {//初始化音频焦点监听
        onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {//重写
                Log.e(TAG, "onAudioFocusChange");
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS: //失去焦点，其他程序占用音频，停止播放
                        Log.e(TAG, "AUDIOFOCUS_LOSS");
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        }

                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://短暂的失去音频焦点,可以暂停音乐,不要释放资源,一会就可以夺回焦点并继续使用
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        }
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        //要申请的AudioFocus是暂时性的，还指示当前正在使用AudioFocus的可以继续播放，只是要“duck”一下（降低音量）。
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.setVolume(0.1f, 0.1f);
                        }
                        break;

                    case AudioManager.AUDIOFOCUS_GAIN:   //获得了Audio Focus，允许进行播放
                        Log.e(TAG, "AUDIOFOCUS_GAIN");
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        }
                        break;

                    default:
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }

                        mediaPlayer.release();
                        mediaPlayer = null;
                        break;
                }
            }
        };

    }

    /**
     * 初始化 MediaPlayer
     */
    private void initMediaPlayer() {

        requestAudioFocus();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.seekTo(currentPosition);   //寻找到当前播放时间
                mediaPlayer.start();
                //Log.e(TAG, "[OnPreparedListener] Start at " + currentIndex + " in mode " + currentMode + ", currentPosition : " + currentPosition);
                handler.sendEmptyMessage(updateDuration);
            }
        });
        //提示： AUDIOFOCUS_LOSS：失去了Audio Focus，并将会持续很长的时间。这里因为可能会停掉很长时间，所以不仅仅要停止Audio的播放，最好直接释放掉Media资源。

        //循环播放方式
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (isPlaying) {
                    switch (currentMode) {
                        case MODE_ONE_LOOP://单曲循环
                            Log.e(TAG, "[Mode] currentMode = MODE_ONE_LOOP.");
                            mediaPlayer.start();
                            break;
                        case MODE_ALL_LOOP://全部循环
                            Log.e(TAG, "[Mode] currentMode = MODE_ALL_LOOP.");
                            play((currentIndex + 1) % mMusicFilesList.size(), 0);
                            //mMusicFilesList.size()当前所有音频文件数量
                            break;
                        case MODE_RANDOM://随机播放
                            Log.e(TAG, "[Mode] currentMode = MODE_RANDOM.");
                            play(getRandomPosition(), 0);
                            break;
                        case MODE_SEQUENCE://队列循环（将所有音乐从头到尾播放一次后结束播放）
                            Log.e(TAG, "[Mode] currentMode = MODE_SEQUENCE.");
                            if (currentIndex < mMusicFilesList.size() - 1) {
                                playNext();
                            } else {
                                stop();
                            }

                            break;
                        default:
                            Log.e(TAG, "No Mode selected! How could that be ?");
                            break;
                    }
                    //Log.e(TAG, "[OnCompletionListener] Going to play at " + currentIndex);
                }
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {//监听错误
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "media player error.");
                return false;
            }
        });

    }

    /*
    * 请求音频焦点
    * */
    private void requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager == null) {
            Log.e(TAG, "audioManager create failed.");
        }

        int ret = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (ret != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.e(TAG, "Request AudioFocus failed.");
        }
    }

    /*
    * 放弃音频焦点
    * */
    private void abandonAudioFocus() {
        if (audioManager != null) {
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
            Log.e(TAG, "abandonAudioFocus");
            audioManager = null;
        }
    }

    private void play(int curIndex, int pCurrentPosition) {
        currentPosition = pCurrentPosition;
        setCurrentMusic(curIndex);//当前播放音乐在mMusicFilesList数组中的位置
        mediaPlayer.reset();

        if ((0 <= currentIndex) && (currentIndex < mMusicFilesList.size())) {
            try {
                mediaPlayer.setDataSource(mMusicFilesList.get(currentIndex).getAbsolutePath());
                mediaPlayer.prepareAsync();//子线程中准备避免阻塞主线程
            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.sendEmptyMessage(updateProgress);//比较与sendMessage函数有什么区别？

            isPlaying = true;

            sendPendingIntend();
        } else {
            Log.e(TAG, "music index out of bounds.");
        }
    }

    private void sendPendingIntend() {
        //消息中心
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new Notification.Builder(this)
                .setTicker("DJMusic")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Playing")
                .setContentText(mMusicFilesList.get(currentIndex).getName())
                .setContentIntent(pendingIntent)
                .getNotification();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        startForeground(1, notification);
    }

    private void setCurrentMusic(int pCurrentMusicIndex) {
        currentIndex = pCurrentMusicIndex;
        handler.sendEmptyMessage(updateCurrentMusic);
    }

    private void stop() {
        mediaPlayer.stop();
        isPlaying = false;
    }

    private void playNext() {
        switch (currentMode) {
            case MODE_ONE_LOOP:
                play(currentIndex, 0);
                break;
            case MODE_ALL_LOOP:
                if (currentIndex + 1 == mMusicFilesList.size()) {
                    currentIndex = 0;
                    play(currentIndex, 0);
                } else {
                    currentIndex += 1;
                    play(currentIndex, 0);
                }
                break;
            case MODE_SEQUENCE:
                if (currentIndex + 1 == mMusicFilesList.size()) {
                    Toast.makeText(this, R.string.music_no_songs, Toast.LENGTH_SHORT).show();
                } else {
                    currentIndex += 1;
                    play(currentIndex, 0);
                }
                break;
            case MODE_RANDOM:
                play(getRandomPosition(), 0);
                break;
        }
    }

    private void playPrevious() {
        switch (currentMode) {
            case MODE_ONE_LOOP:
                play(currentIndex, 0);
                break;
            case MODE_ALL_LOOP:
                if (currentIndex - 1 < 0) {
                    currentIndex = mMusicFilesList.size() - 1;
                    play(currentIndex, 0);
                } else {
                    currentIndex -= 1;
                    play(currentIndex, 0);
                }
                break;
            case MODE_SEQUENCE:
                if (currentIndex - 1 < 0) {
                    Toast.makeText(this, R.string.music_no_previous, Toast.LENGTH_SHORT).show();
                } else {
                    currentIndex -= 1;
                    play(currentIndex, 0);
                }
                break;
            case MODE_RANDOM:
                play(getRandomPosition(), 0);
                break;
        }
    }

    private int getRandomPosition() {
        int random = (int) (Math.random() * mMusicFilesList.size());
        return random;
    }

    private void toUpdateProgress() {
        if (mediaPlayer != null && isPlaying) {
            int progress = mediaPlayer.getCurrentPosition();
            //Log.e(TAG,"current: " + progress);
            Intent intent = new Intent();
            intent.setAction(ACTION_UPDATE_PROGRESS);
            intent.putExtra(ACTION_UPDATE_PROGRESS, progress);
            sendBroadcast(intent);
            handler.sendEmptyMessageDelayed(updateProgress, 1000);
        }
    }

    private void toUpdateDuration() {
        if (mediaPlayer != null) {
            int duration = mediaPlayer.getDuration();
            //Log.e(TAG,"duration=" + duration);
            Intent intent = new Intent();
            intent.setAction(ACTION_UPDATE_DURATION);
            intent.putExtra(ACTION_UPDATE_DURATION, duration);
            sendBroadcast(intent);
        }
    }

    private void toUpdateCurrentMusic() {
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_CURRENT_MUSIC);
        intent.putExtra(ACTION_UPDATE_CURRENT_MUSIC, currentIndex);
        sendBroadcast(intent);
    }

    class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }

        //index是目标歌曲在musicFileList中的索引
        public void startPlay(int index, int currentPosition) {
            play(index, currentPosition);
        }

        public void stopPlay() {
            stop();
        }

        public void toNext() {
            playNext();
        }

        public void toPrevious() {
            playPrevious();
        }

        /**
         * MODE_ONE_LOOP = 1;
         * MODE_ALL_LOOP = 2;
         * MODE_RANDOM = 3;
         * MODE_SEQUENCE = 4;
         */
        public void setMode(int mode) {
            currentMode = mode;
        }

        /**
         * return the current mode
         * MODE_ONE_LOOP = 1;
         * MODE_ALL_LOOP = 2;
         * MODE_RANDOM = 3;
         * MODE_SEQUENCE = 4;
         *
         * @return
         */
        public int getCurrentMode() {
            return currentMode;
        }

        /**
         * The service is playing the music
         *
         * @return
         */
        public boolean isPlaying() {
            return isPlaying;
        }

        /**
         * Notify Activities to update the current music and duration when current activity changes.
         */
        public void notifyActivity() {
            toUpdateCurrentMusic();
            toUpdateDuration();
            toUpdateProgress();
        }

        /**
         * Seekbar changes
         *
         * @param progress
         */
        public void changeProgress(int progress) {
            if (mediaPlayer != null) {
                //Log.e(TAG, "changeProgress.");
                currentPosition = progress * 1000;
                mediaPlayer.seekTo(currentPosition);

            }
        }
    }
}

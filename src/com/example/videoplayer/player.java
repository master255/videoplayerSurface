package com.example.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import java.io.IOException;

public class player extends SurfaceView
    implements MediaPlayerControl {
    private String TAG = "VideoView", path;

    // all possible internal states
    private static final int STATE_ERROR              = -1;
    private static final int STATE_IDLE               = 0;
    private static final int STATE_PREPARING          = 1;
    private static final int STATE_PREPARED           = 2;
    private static final int STATE_PLAYING            = 3;
    private static final int STATE_PAUSED             = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private static int mCurrentState;

    // All the stuff we need for playing and showing a video
    static public MediaPlayer mMediaPlayer;
    public Surface mSurfaceHolder;
    private  MediaController mMediaController;
    private OnCompletionListener mOnCompletionListener;
    private OnPreparedListener mOnPreparedListener;
    private static int         mCurrentBufferPercentage;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener  mOnInfoListener;
    private static boolean     mCanPause;
    private static boolean     mCanSeekBack;
    private static boolean     mCanSeekForward;

    public player(Context context) {
        super(context);
       
    }

    public player(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
       
    }

    public player(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVideoView();
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        }

        public void surfaceCreated(SurfaceHolder holder) {
         mSurfaceHolder = holder.getSurface();
          if ((mMediaPlayer != null)) {
            mMediaPlayer.setSurface(mSurfaceHolder);
          } 
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
        	if (player.mMediaPlayer != null) {
    			player.mMediaPlayer.setSurface(null);}
        }
      }; 

    private void initVideoView() {
        getHolder().addCallback(mSHCallback);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    public void setVideoPath(String path1, Activity activity) {
        path=path1;
        openVideo();
    }

    private void openVideo() {
        if (path == null) {
            return;
        } 

        try {
            mCurrentBufferPercentage = 0;
            mMediaPlayer = new MediaPlayer();
            //listeners
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            ///////////////////////
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(path);
            if (mSurfaceHolder!=null) {mMediaPlayer.setSurface(mSurfaceHolder);}
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            attachMediaController();
          } catch (IOException ex) {
            mCurrentState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
          } catch (IllegalArgumentException ex) {
            mCurrentState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
          }
    }

 ///////////////////////////////////////////////////////////////////////////////////
    public void setMediaController(MediaController controller) {
    	if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ?
                (View)this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
        new MediaPlayer.OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
               /* mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();
                if (mVideoWidth != 0 && mVideoHeight != 0) {
                    getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
                    requestLayout();
                }*/
            }
        };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;

            mCanPause = mCanSeekBack = mCanSeekForward = true;

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener =
        new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
            if (mCurrentState == STATE_PLAYBACK_COMPLETED) {
                return;
            }
                mCurrentState = STATE_PLAYBACK_COMPLETED;
                if (mMediaController != null) {
                    mMediaController.hide();
                }
                if (mOnCompletionListener != null) {
                    mOnCompletionListener.onCompletion(mMediaPlayer);
                }
            }
        };

    private MediaPlayer.OnInfoListener mInfoListener =
        new MediaPlayer.OnInfoListener() {
            public  boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
                if (mOnInfoListener != null) {
                    mOnInfoListener.onInfo(mp, arg1, arg2);
                }
                return true;
            }
        };

    private MediaPlayer.OnErrorListener mErrorListener =
        new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                mCurrentState = STATE_ERROR;
                if (mMediaController != null) {
                    mMediaController.hide();
                }
                if (mOnErrorListener != null) {
                    if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                        return true;
                    }
                }
               return true;
            }
        };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
        new MediaPlayer.OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                mCurrentBufferPercentage = percent;
            }
        };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l)
    {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener l)
    {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, TextureVideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(OnErrorListener l)
    {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(OnInfoListener l) {
        mOnInfoListener = l;
    }
    void startVideoPlayback() {
        mMediaPlayer.start();
      }
    /*
     * release the media player in any state
     */
    private void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
            keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
            keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
            keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
            keyCode != KeyEvent.KEYCODE_MENU &&
            keyCode != KeyEvent.KEYCODE_CALL &&
            keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
    }

    public void suspend() {
        release();
    }

    public void resume() {
        openVideo();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return (int)mMediaPlayer.getDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return (int)mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
            mCurrentState != STATE_ERROR &&
            mCurrentState != STATE_IDLE &&
            mCurrentState != STATE_PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    } 

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    public int getAudioSessionId() {
        return mMediaPlayer.getAudioSessionId();
    }

}
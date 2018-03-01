package com.alnadal.nada.photogallery;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ThumbnailDownloader<T> extends HandlerThread{
    private  static final String TAG= "ThumbnailDownloader";
    private  static final int MESSAGE_DOWNLOAD=0;

    private boolean mHasQuit=false;
    private android.os.Handler mRequestHandler;
    private ConcurrentMap<T,String>mRequesttMap=new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T>mThumbnnailDownloadListener;

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target,Bitmap thumbnail);
    }
    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener){
        mThumbnnailDownloadListener=listener;
    }

    public ThumbnailDownloader(Handler responseHandler){
        super(TAG);
        mResponseHandler=responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {

                if (msg.what==MESSAGE_DOWNLOAD){
                    T target=(T) msg.obj;
                    Log.i(TAG,"got a request for URl: "+mRequesttMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    public boolean Quit(){
        mHasQuit=true;
        return super.quit();
    }
    public void queueThumbnail(T target, String url){
        Log.i(TAG,"Got URL: "+url);

        if(url==null){
            mRequesttMap.remove(target);
        }else {
            mRequesttMap.put(target,url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();
        }
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequesttMap.clear();
    }

    private void handleRequest(final T target){
        try {
            final String url=mRequesttMap.get(target);

            if (url==null){
                return;
            }
            byte[]bitmapBytes =new FlickrFetcher().getUrlBytes(url);
            final Bitmap bitmap= BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            Log.i(TAG,"Bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequesttMap.get(target)!=url ||mHasQuit){
                        return;
                    }

                    mRequesttMap.remove(target);
                    mThumbnnailDownloadListener.onThumbnailDownloaded(target,bitmap);
                }
            });

        }catch (IOException e){
            Log.i(TAG,"Error downloading image"+e);
        }

    }

}

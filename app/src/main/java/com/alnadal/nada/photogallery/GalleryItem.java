package com.alnadal.nada.photogallery;


import android.net.Uri;

public class GalleryItem {
    private String mCaption;
    private String mID;
    private String mUrl;
    private String mOwner;

    public String toString(){
        return mCaption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String cation) {
        mCaption = cation;
    }

    public String getID() {
        return mID;
    }

    public void setID(String ID) {
        mID = ID;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mmUrl) {
        this.mUrl = mmUrl;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }
    public Uri getPhotoPageUri(){
        return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mID)
                .build();
    }

}

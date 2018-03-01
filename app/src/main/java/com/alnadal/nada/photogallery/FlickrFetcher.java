package com.alnadal.nada.photogallery;



import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetcher {

    private static final String TAG="FlickrFetcher";
    private static final String API_KET="yourApiKeyHere";
    private static final String FETCH_RECENTS_METHOD="flickr.photos.getRetRecent";
    private static final String SEARCH_METHOD="flickr.photos.search";
    private static final  Uri ENDPOINT= Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("method","flickr.photos.getRecent")
            .appendQueryParameter("api_key",API_KET)
            .appendQueryParameter("format","json")
            .appendQueryParameter("nojsoncallback","1")
            .appendQueryParameter("extras","url_s")
            .build();


    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url=new URL(urlSpec);
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            InputStream in=connection.getInputStream();

            if(connection.getResponseCode() !=HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+":with"+urlSpec);
            }
            int bytesRead=0;
            byte[]buffer=new byte[1024];
            while ((bytesRead=in.read(buffer))>0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos(){
        String url=buildUrl(FETCH_RECENTS_METHOD,null);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPotos(String query){
        String url=buildUrl(SEARCH_METHOD,query);
        return downloadGalleryItems(url);
    }

    private List<GalleryItem> downloadGalleryItems(String url){

        List<GalleryItem>items=new ArrayList<>();

        try {

            String jsonString=getUrlString(url);
            Log.i(TAG,"Recived JSON: "+jsonString);
            JSONObject jsonBody=new JSONObject(jsonString);
            parseItems(items,jsonBody);

        }catch (IOException ioe){
            Log.i(TAG,"Failed to fatch items",ioe);
        }catch (JSONException je){
            Log.i(TAG,"Failed to parse JSON",je);
        }
        return items;
    }
    private String buildUrl(String method,String query){
        Uri.Builder urlBuilder=ENDPOINT.buildUpon().appendQueryParameter("method",method);

        if(method.equals(SEARCH_METHOD)){
            urlBuilder.appendQueryParameter("text",query);
        }
        return urlBuilder.build().toString();
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException,JSONException{

        JSONObject photosJsonObject =jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray=photosJsonObject.getJSONArray("photo");

        for(int i = 0;i < photoJsonArray.length(); i++){
            JSONObject photoJsonObject=photoJsonArray.getJSONObject(i);

            GalleryItem item=new GalleryItem();
            item.setID(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if(!photoJsonObject.has("url_s")){
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            item.setOwner(photoJsonObject.getString("owner"));
            items.add(item);

        }

    }

}

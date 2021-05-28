package com.mobdeve.s13_demesa_noveda.mobdeve_mc02;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainMenuActivity extends AppCompatActivity {

    private String address = "";
    private Context context;
    private int listSize;
    private Elements popMoviesListAlpha, popNamesListAlpha, popLinksListAlpha;
    private Elements soonMoviesListAlpha, soonNamesListAlpha, soonLinksListAlpha;
    private List<String> popNamesList, popLinksList, popImgList;
    private List<String> soonNamesList, soonLinksList, soonImgList;
    private List<Document> docList;
    private Document doc;
    private ArrayList<Movie> resultsPopular;
    private ArrayList<Movie> resultsComingSoon;
    private Handler mHandler;
    private RecyclerView popularRecyclerView;
    private RecyclerView comingSoonRecyclerView;
    private RecyclerView.Adapter myAdapter1;
    private LinearLayoutManager myManager1;
    private RecyclerView.Adapter myAdapter2;
    private LinearLayoutManager myManager2;

    private TextView tv_seeMorePopular;
    private TextView tv_seeMoreComingSoon;
    private TextView tv_searchByGenre;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        docList = new ArrayList<>();
        popNamesList = new ArrayList<>();
        popLinksList = new ArrayList<>();
        popImgList = new ArrayList<>();
        soonImgList = new ArrayList<>();
        soonLinksList = new ArrayList<>();
        soonNamesList = new ArrayList<>();

        resultsPopular = new ArrayList<>();
        resultsComingSoon = new ArrayList<>();
        loadMostPopular();
//        loadComingSoon();
        setUpPopularRecyclerView();
//
        this.tv_seeMorePopular = findViewById(R.id.tv_seeMorePopMovies);
        this.tv_seeMoreComingSoon = findViewById(R.id.tv_seeMoreComingSoonMovies);
        this.tv_searchByGenre = findViewById(R.id.tv_searchByGenre);
        Log.d("SETUP", "OnCreate Done");

        this.tv_searchByGenre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainMenuActivity.this, ResultsActivity.class);
                startActivity(i);
            }
        });
    }

    private void loadMostPopular() {
        address = "https://www.imdb.com/search/title/?groups=top_100";
        getDataMostPopular();
    }

    private void loadComingSoon(){
        address = "https://www.imdb.com/movies-coming-soon/?ref_=nv_mv_cs";
        getDataComingSoon();
    }
    private void fillPopularArray(){
        for(int i = 0; i< popNamesList.size(); i++){
            Movie movie = new Movie();
            Log.d("Name", popNamesList.get(i));
            Log.d("Link", popLinksList.get(i));
            movie.setMovieName(popNamesList.get(i));
            movie.setImage(popImgList.get(i));
            movie.setLink(popLinksList.get(i));
            resultsPopular.add(movie);
        }
    }
    private void fillComingSoonArray(){
        for(int i = 0; i < soonNamesList.size(); i++){
            Movie movie = new Movie();
            Log.d("Name", soonNamesList.get(i));
            Log.d("Link", soonLinksList.get(i));
            movie.setMovieName(soonNamesList.get(i));
            movie.setImage(soonImgList.get(i));
            movie.setLink(soonLinksList.get(i));
            resultsComingSoon.add(movie);
        }
    }
    private void getDataMostPopular() {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                try {
                    doc = Jsoup.connect(address).get();
                    popMoviesListAlpha = doc.select(".lister-item");
                    //Get list size
                    listSize = popMoviesListAlpha.size();
                    //Get names elements
                    popNamesListAlpha = popMoviesListAlpha.select("img");
                    popNamesList = popNamesListAlpha.eachAttr("alt");
                    //Get first 10 elements;
                    if(listSize>10){
                        popNamesList = trimSelection(popNamesList);
                    }
                    //Get links elements
                    popLinksListAlpha = doc.select("h3.lister-item-header");
                    popLinksListAlpha = popLinksListAlpha.select("a");
                    popLinksList = popLinksListAlpha.eachAttr("href");
                    //Get first 10 elements;
                    if(listSize>10){
                        popLinksList = trimSelection(popLinksList);
                        listSize = 10;
                    }
                    //Fix link elements
                    popLinksList = fixLinkList(popLinksList);
                    //Get documents for image elements
                    popLinksList.stream().forEach(link -> {
                        try {
                            Document document1 = Jsoup.connect(link).get();
                            docList.add(document1);
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    for (int i = 0; i<listSize;  i++){
                        Document doc2 = docList.get(i);
                        Element poster = doc2.selectFirst(".poster");
                        Element img = poster.selectFirst("img");
                        popImgList.add(img.attr("src"));
                        Log.v("Image List Builder",img.attr("src") );
                    }

                    Log.v("End", "End of run");


                    //Test list by printing
                    //Iterator iterator = namesList.iterator();
                    //testWrite(iterator);



                   fillPopularArray();
                   mHandler = new Handler(Looper.getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            myAdapter1.notifyDataSetChanged();
                        }
                    });

                    Log.v("getData", "Finished ");
//                    //Create intent
//                    Intent i = new Intent(context, movieList.class);
//                    //Pass lists
//                    i.putStringArrayListExtra("names", (ArrayList<String>)namesList);
//                    i.putStringArrayListExtra("links", (ArrayList<String>)linksList);
//                    i.putStringArrayListExtra("img", (ArrayList<String>)imgList);
//                    //Start activity
//                    startActivity(i);
//                    finish();

                } catch (IOException e) {
                }
            }
        }).start();
    }



    private void getDataComingSoon(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                try {
                    doc = Jsoup.connect(address).get();
                    soonMoviesListAlpha = doc.select(".lister-item");
                    //Get list size
                    listSize = soonMoviesListAlpha.size();
                    //Get names elements
                    soonNamesListAlpha = soonMoviesListAlpha.select("img");
                    soonNamesList = soonNamesListAlpha.eachAttr("alt");
                    //Get first 10 elements;
                    if(listSize>10){
                        soonNamesList = trimSelection(soonNamesList);
                    }
                    //Get links elements
                    soonLinksListAlpha = doc.select("h3.lister-item-header");
                    soonLinksListAlpha = soonLinksListAlpha.select("a");
                    soonLinksList = soonLinksListAlpha.eachAttr("href");
                    //Get first 10 elements;
                    if(listSize>10){
                        soonLinksList = trimSelection(soonLinksList);
                        listSize = 10;
                    }
                    //Fix link elements
                    soonLinksList = fixLinkList(soonLinksList);
                    //Get documents for image elements
                    soonLinksList.stream().forEach(link -> {
                        try {
                            Document document1 = Jsoup.connect(link).get();
                            docList.add(document1);
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    for (int i = 0; i<listSize;  i++){
                        Document doc2 = docList.get(i);
                        Element poster = doc2.selectFirst(".poster");
                        Element img = poster.selectFirst("img");
                        soonImgList.add(img.attr("src"));
                        Log.v("Image List Builder",img.attr("src") );
                    }

                    Log.v("End", "End of run");


                    //Test list by printing
                    //Iterator iterator = namesList.iterator();
                    //testWrite(iterator);



                    fillPopularArray();
                    mHandler = new Handler(Looper.getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            myAdapter2.notifyDataSetChanged();
                        }
                    });

                    Log.v("getData", "Finished ");

                } catch (IOException e) {
                }
            }
        }).start();
    }
    public List<String> trimSelection(List<String> list){
        Log.v("getFirstFifty", "Trimming Data");
        List<String> listFinal = new ArrayList<>();
        for(int i = 0; i<10; i++){
            listFinal.add(list.get(i));
        }
        Log.v("getFirstFifty", "Finished trimming Data");
        return listFinal;
    }

    public List<String> fixLinkList(List<String> list){
        List<String> fixedLinkList = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            fixedLinkList.add("https://www.imdb.com" + list.get(i));
            Log.v("Link Fixer", "https://www.imdb.com" + list.get(i));
        }
        return fixedLinkList;
    }
    public boolean isEmptyStringArray(String [] array){
        for(int i=0; i<array.length; i++){
            if(array[i]!=null){
                return false;
            }
        }
        return true;
    }

    void setUpPopularRecyclerView(){
        this.popularRecyclerView = findViewById(R.id.popularRecyclerView);
        this.comingSoonRecyclerView = findViewById(R.id.comingSoonRecyclerView);
        this.myManager1 = new LinearLayoutManager(this);
        this.myManager2 = new LinearLayoutManager(this);
        this.popularRecyclerView.setLayoutManager(this.myManager1);
        this.comingSoonRecyclerView.setLayoutManager(this.myManager2);
        this.myAdapter1 = new MovieListAdapter(this.resultsPopular);
        this.popularRecyclerView.setAdapter(myAdapter1);
        this.myAdapter2 = new MovieListAdapter(this.resultsComingSoon);
        this.comingSoonRecyclerView.setAdapter(myAdapter2);
    }
}
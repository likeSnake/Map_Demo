package com.jcl.map_demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;


import com.jcl.map_demo.Adapter.DestinationAdapter;
import com.jcl.map_demo.Util.DistanceUtil;
import com.jcl.map_demo.pojo.DestinationInfo;
import com.mapbox.geojson.Point;
import com.mapbox.search.CompletionCallback;
import com.mapbox.search.SearchEngine;

import com.mapbox.search.SearchEngineSettings;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchSelectionCallback;

import com.mapbox.search.ServiceProvider;
import com.mapbox.search.common.AsyncOperationTask;
import com.mapbox.search.record.HistoryDataProvider;
import com.mapbox.search.record.HistoryRecord;
import com.mapbox.search.result.SearchAddress;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements View.OnClickListener{

    private final HistoryDataProvider historyDataProvider = ServiceProvider.getInstance().historyDataProvider();
    private AsyncOperationTask task = null;
    List<DestinationInfo> list = new ArrayList<>();
    private DestinationAdapter destinationAdapter;
    private RecyclerView rv_search_result;
    private RecyclerView rv_search_record;
    private EditText et_key;
    private SearchEngine searchEngine;
    private AsyncOperationTask searchRequestTask;
    private TextView tv_search_title;
    private TextView tv_search_result;


    final SearchOptions options = new SearchOptions.Builder()
            .limit(5)
            .build();

    private final SearchSelectionCallback searchCallback = new SearchSelectionCallback() {

        @Override
        public void onSuggestions(@NonNull List<SearchSuggestion> suggestions, @NonNull ResponseInfo responseInfo) {
            list.clear();
            if (suggestions.isEmpty()) {
                Log.i("SearchApiExample", "没搜索到");
            } else {
                System.out.println("设置可见");
                tv_search_title.setText("SEARCH RESULT");
                System.out.println(suggestions.size());
                System.out.println(suggestions.get(0).getName());
                Log.i("SearchApiExample", "总共搜索到: " + suggestions + "\nSelecting first...");
                System.out.println("搜索到："+suggestions.size());//coordinates
                System.out.println("responseInfo"+responseInfo.toString());
                for (int i = 0; i < 5; i++) {
                    searchRequestTask = (AsyncOperationTask) searchEngine.select(suggestions.get(i), this);
                }
            }
        }

        @Override
        public void onResult(@NonNull SearchSuggestion suggestion, @NonNull SearchResult result, @NonNull ResponseInfo info) {
            Log.i("SearchApiExample", "精确地址: " + result);
            String addressInfo = "";
            Point point = result.getCoordinate();
            SearchAddress address = result.getAddress();
            assert address != null;
            String country = address.getCountry();
            String region = address.getRegion();
            String place = address.getPlace();
            String locality = address.getLocality();
            String street = address.getStreet();
            String name = result.getName();
            Point MyPoint = result.getRequestOptions().getOptions().getProximity();
            if (country!=null){
                addressInfo = country+" ";
            }
            if (region!=null){
                addressInfo += region+" ";
            }
            if (place!=null){
                addressInfo += place+" ";
            }
            if (locality!=null){
                addressInfo += locality+" ";
            }
            if (street!=null){
                addressInfo += street;
            }
          //  System.out.println(point.longitude()+" "+ point.latitude()+ " "+MyPoint.longitude()+" "+ MyPoint.latitude());
            double distances = DistanceUtil.getDistances(point.longitude(), point.latitude(), MyPoint.longitude(), MyPoint.latitude());
            System.out.println("-地址名:"+name+"-地址:"+addressInfo+"-距离:"+distances);
            list.add(new DestinationInfo(name,addressInfo,String.valueOf(distances)+" km",point));
            start(false,list);
            rv_search_record.setVisibility(View.GONE);
            rv_search_result.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCategoryResult(@NonNull SearchSuggestion suggestion, @NonNull List<SearchResult> results, @NonNull ResponseInfo responseInfo) {
            Log.i("SearchApiExample", "Category search results: " + results);
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Search error: ", e);
        }
    };
    // 检索搜索历史
    private final CompletionCallback<List<HistoryRecord>> callback = new CompletionCallback<List<HistoryRecord>>() {
        @Override
        public void onComplete(List<HistoryRecord> result) {
            Log.i("SearchApiExample", "所有搜索历史: " + result);

            System.out.println(result.size());
            for (int i = 0; i < result.size(); i++) {
                System.out.println(result.get(i).getName());
            }

        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Unable to retrieve history records", e);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        this.searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
                new SearchEngineSettings(this.getString(R.string.mapbox_access_token))
        );

        System.out.println(getIntent().getStringExtra("longitude"));
       /* MyLongitude = Double.valueOf(getIntent().getStringExtra("longitude"));
        MyLatitude = Double.valueOf(getIntent().getStringExtra("latitude"));*/
        et_key = findViewById(R.id.et_key);
        et_key.setOnClickListener(this);
        tv_search_title = findViewById(R.id.tv_search_title);
        rv_search_result = findViewById(R.id.rv_search_result);
        rv_search_record = findViewById(R.id.rv_search_record);
        task = historyDataProvider.getAll(callback);

        et_key.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == 0 || actionId == 3) && event != null) {

                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(SearchActivity.this.getCurrentFocus()
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    System.out.println("开始搜索");
                    searchRequestTask = (AsyncOperationTask) searchEngine.search(et_key.getText().toString(), options, searchCallback);
                }

                return false;
            }
        });

        et_key.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
       // searchEngine = MapboxSearchSdk.getSearchEngine();

    }
    @Override
    protected void onDestroy() {
        if (searchRequestTask!=null){
            searchRequestTask.cancel();
        }
        if (task!=null){
            task.cancel();
        }

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

        }
    }
    public void start(Boolean b,List<DestinationInfo> list){
        LinearLayoutManager manager = new LinearLayoutManager(SearchActivity.this, LinearLayoutManager.VERTICAL, false);
        destinationAdapter = new DestinationAdapter(list,this);
        if(b){
            rv_search_result.scrollToPosition(destinationAdapter.getItemCount()-1);
        }
        rv_search_result.setLayoutManager(manager);
        rv_search_result.setAdapter(destinationAdapter);
    }
}
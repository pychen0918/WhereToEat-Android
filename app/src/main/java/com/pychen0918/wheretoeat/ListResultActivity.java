package com.pychen0918.wheretoeat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdView;
import com.pychen0918.wheretoeat.common.GlobalVariables;
import com.pychen0918.wheretoeat.common.QueryResults;
import com.pychen0918.wheretoeat.common.Restaurant;
import com.pychen0918.wheretoeat.common.Util;

public class ListResultActivity extends AppCompatActivity {
    private QueryResults queryResults;
    private ListRestaurantRecyclerAdapter mAdapter = null;
    private int mSortMethod = 1;
    private String mType = "";
    private boolean mUseIu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_result);

        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        mType = extra.getString("TYPE");
        mUseIu = extra.getBoolean("USEIU");

        Toolbar toolbar = (Toolbar) findViewById(R.id.list_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(mType);
        }

        queryResults = ((GlobalVariables) this.getApplicationContext()).queryResults;

        loadPreference(this, getString(R.string.preference_key));
        if (mSortMethod == 0)
            queryResults.sortByRating();
        else
            queryResults.sortByDistance();

        Util.initAdView((AdView)findViewById(R.id.list_adview));

        showListResult();
    }

    private void loadPreference(Context context, String preferenceFile) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                preferenceFile, Context.MODE_PRIVATE);
        mSortMethod = sharedPref.getInt("SORTMETHOD", 1);
    }

    private void savePreference(Context context, String preferenceFile) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                preferenceFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("SORTMETHOD", mSortMethod);
        editor.apply();
    }

    @Override
    protected void onPause() {
        savePreference(this, getString(R.string.preference_key));
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sort_by_rating:
                queryResults.sortByRating();
                mSortMethod = 0;
                if (mAdapter != null)
                    mAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_sort_by_distance:
                queryResults.sortByDistance();
                mSortMethod = 1;
                if (mAdapter != null)
                    mAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_share_list:
                Util.startShareListActivity(this, mType, queryResults.getRestaurantData());
                return true;
            case R.id.action_random_pick_one:
                Util.startSingleResultActivity(this, queryResults.getRandomIndex(), mUseIu);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void showListResult() {
        if (queryResults.getRestaurantCount() > 0) {
            RecyclerView restaurantListRecyclerView = (RecyclerView) findViewById(R.id.recycler_restaurant_list);
            mAdapter = new ListRestaurantRecyclerAdapter(this, queryResults.getRestaurantData());
            mAdapter.setUseIu(mUseIu);

            restaurantListRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            restaurantListRecyclerView.setAdapter(mAdapter);
            restaurantListRecyclerView.setHasFixedSize(true);

            mAdapter.setOnItemClickListener(new ListRestaurantRecyclerAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Restaurant restaurant = queryResults.getOneRestaurant(position);
                    if (restaurant != null) {
                        Util.startSingleResultActivity(ListResultActivity.this, position, mUseIu);
                    }
                }
            });
        } else {
            showEmptyResult();
        }
    }

    private void showEmptyResult() {
        setContentView(R.layout.empty_result);
    }
}
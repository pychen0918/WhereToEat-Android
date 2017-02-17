package com.pychen0918.wheretoeat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.pychen0918.wheretoeat.common.Constants;
import com.pychen0918.wheretoeat.common.Credential;
import com.pychen0918.wheretoeat.common.GlobalVariables;
import com.pychen0918.wheretoeat.common.QueryResults;
import com.pychen0918.wheretoeat.common.Restaurant;
import com.pychen0918.wheretoeat.common.Util;
import com.pychen0918.wheretoeat.googleplace.DetailQueryTaskResponse;
import com.pychen0918.wheretoeat.googleplace.GooglePlaceDetailQueryTask;
import com.pychen0918.wheretoeat.googleplace.GooglePlacePhotoQueryTask;
import com.pychen0918.wheretoeat.googleplace.PhotoQueryTaskResponse;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SingleResultActivity extends AppCompatActivity implements PhotoQueryTaskResponse, DetailQueryTaskResponse {
    private QueryResults queryResults;
    private int currentIndex;
    private boolean detailToggle = false;
    private boolean mUseIu = false;
    private boolean mDownloadPhoto = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queryResults = ((GlobalVariables) this.getApplicationContext()).queryResults;

        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        int index = extra.getInt("INDEX");

        if (index < 0) {
            showEmptyResult();
        } else {
            loadDefaultPreference();
            prepareBriefRestaurantData(index);
            prepareRestaurantPhoto(index);
            prepareRestaurantDetail(index);
        }

        Util.initAdView((AdView)findViewById(R.id.single_adview));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share_restaurant:
                Util.startShareRestaurantInfoActivity(this, queryResults.getOneRestaurant(currentIndex));
                return true;
            case R.id.action_open_map:
                Util.startGoogleMapActivity(this, queryResults.getOneRestaurant(currentIndex));
                return true;
            case R.id.action_phone_call:
                Util.startPhoneCallActivity(this, queryResults.getOneRestaurant(currentIndex));
                return true;
            case R.id.action_browse_website:
                Util.startBrowseWebsiteActivity(this, queryResults.getOneRestaurant(currentIndex));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadDefaultPreference(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUseIu = sharedPref.getBoolean(getString(R.string.use_iu_key), false);
        mDownloadPhoto = sharedPref.getBoolean(getString(R.string.download_photo_key), true);
    }

    private void showEmptyResult() {
        setContentView(R.layout.empty_result);
    }

    // ---------------------------------------------------
    // Brief info related functions
    // ---------------------------------------------------

    private void prepareBriefRestaurantData(int index) {
        currentIndex = index;
        Restaurant restaurant = queryResults.getOneRestaurant(index);
        if (restaurant == null || restaurant.getPlaceId().isEmpty()) {
            showEmptyResult();
        } else {
            showRestaurantBriefData(restaurant);
        }
    }

    private void showRestaurantBriefData(Restaurant restaurant) {
        setContentView(R.layout.activity_single_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.single_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_single_activity));
        }

        TextView textview_title = (TextView) findViewById(R.id.textview_title);
        TextView textview_subtitle = (TextView) findViewById(R.id.textview_subtitle);
        RatingBar ratingbar_rating = (RatingBar) findViewById(R.id.rating_bar);
        TextView textview_rating = (TextView) findViewById(R.id.textview_rating);
        TextView textview_distance = (TextView) findViewById(R.id.textview_distance);

        textview_title.setText(restaurant.getName());
        String type = queryResults.restaurantTypeTranslate(restaurant.getTypes());
        if(!type.isEmpty())
            textview_subtitle.setText(type);
        else
            textview_subtitle.setVisibility(View.GONE);
        ratingbar_rating.setRating(restaurant.getRating());
        textview_rating.setText("(" + String.format(Locale.getDefault(), "%.1f", restaurant.getRating()) + ")");
        textview_distance.setText(Util.getDistanceDisplayString(this, restaurant.getDistance(), mUseIu));
    }

    // ---------------------------------------------------
    // Photo related functions
    // ---------------------------------------------------

    private void prepareRestaurantPhoto(int index) {
        Restaurant restaurant = queryResults.getOneRestaurant(index);
        // Case 1: User decide not to download photos
        if(!mDownloadPhoto) {
            showEmptyRestaurantPhoto();
        }
        // Case 2: We have a photo of the restaurant: show it
        else if (restaurant.getPhoto(0) != null) {
            showRestaurantPhoto(index);
        }
        // Case 3: We don't have a existing photo, but have an id. We need to retrieve the photo from Google
        else if (restaurant.getPhotoId(0).length() > 0) {
            startPhotoQueryTask(index);
        }
        // Case 4: We have restaurant but it doesn't have photo
        else {
            showEmptyRestaurantPhoto();
        }
    }

    private void showEmptyRestaurantPhoto() {
        ImageView imageview_restaurant_preview = (ImageView) findViewById(R.id.imageview_thumbnail);
        imageview_restaurant_preview.setImageResource(R.drawable.image_no_photo);
    }

    private void startPhotoQueryTask(int index) {
        String photoReference = queryResults.getOneRestaurant(index).getPhotoId(0);
        if (photoReference.length() > 0) {
            String params = "maxwidth=" + Constants.photoMaxWidth +
                    "&maxheight=" + Constants.photoMaxHeight +
                    "&photoreference=" + photoReference +
                    "&key=" + Credential.googleApiKey;
            String getPhotoUrl = Constants.googlePhotoUrl + "?" + params;
            GooglePlacePhotoQueryTask photoQueryTask = new GooglePlacePhotoQueryTask();
            photoQueryTask.setTaskResponse(this);
            photoQueryTask.execute(String.valueOf(index), getPhotoUrl);
        } else {
            finishPhotoQueryTask(false, index, null);
        }
    }

    @Override
    public void finishPhotoQueryTask(boolean status, int index, Bitmap output) {
        if (status) {
            if (index >= 0)
                queryResults.getRestaurantData().get(index).addPhoto(output);
            showRestaurantPhoto(index);
        } else {
            showEmptyRestaurantPhoto();
        }
    }

    private void showRestaurantPhoto(int index) {
        Restaurant restaurant = queryResults.getOneRestaurant(index);
        Bitmap photo = restaurant.getPhoto(0);
        if (photo == null) {
            showEmptyRestaurantPhoto();
        } else {
            ImageView imageview_restaurant_preview = (ImageView) findViewById(R.id.imageview_thumbnail);
            imageview_restaurant_preview.setImageBitmap(photo);
        }
    }

    // ---------------------------------------------------
    // Detail related functions
    // ---------------------------------------------------

    private void prepareRestaurantDetail(int index) {
        Restaurant restaurant = queryResults.getOneRestaurant(index);
        // Case 1: We already have detail information about this restaurant
        if (restaurant.getAddress().length() > 0) {
            showRestaurantDetail(index);
        }
        // Case 2: If not, send query to google
        else {
            startDetailQueryTask(index);
        }
    }

    private void startDetailQueryTask(int index) {
        Restaurant restaurant = queryResults.getOneRestaurant(index);
        String params = "placeid=" + restaurant.getPlaceId() +
                "&language=" + queryResults.getLocaleLanguage() +
                "&key=" + Credential.googleApiKey;
        String getDetailUrl = Constants.googleDetailUrl + "?" + params;
        GooglePlaceDetailQueryTask detailQueryTask = new GooglePlaceDetailQueryTask();
        detailQueryTask.setTaskResponse(this);
        detailQueryTask.execute(String.valueOf(index), getDetailUrl);
    }

    @Override
    public void finishDetailQueryTask(boolean status, int index, String output) {
        if (status) {
            if (queryResults.parseDetailResult(index, output)) {
                showRestaurantDetail(index);
            } else {
                disableDetailButton();
            }
        } else {
            disableDetailButton();
        }
    }

    private void disableDetailButton() {
        ImageButton detailButton = (ImageButton) findViewById(R.id.button_toggle_detail);
        detailButton.setVisibility(View.GONE);
    }

    private void showRestaurantDetail(int index) {
        Restaurant restaurant = queryResults.getOneRestaurant(index);

        Util.fillOneLabeledItem(this, (LinearLayout) findViewById(R.id.detail_address), R.string.label_address, restaurant.getAddress());
        Util.fillOneLabeledItem(this, (LinearLayout) findViewById(R.id.detail_phone), R.string.label_phone, restaurant.getPhoneNumber());
        Util.fillOneLabeledLink(this, (LinearLayout) findViewById(R.id.detail_website), R.string.label_website, restaurant.getWebsite());
        fillOpeningHours(R.id.detail_opening_hours, restaurant.getOpeningHours());

        int count = 0;
        for (Restaurant.Review review : restaurant.getReviews()) {
            if (count > 5)
                break;
            fillOneReviewItem(R.id.ll_review_container, review);
            count++;
        }
    }

    private void fillOpeningHours(int resourceId, List<Restaurant.OpeningHours> openingHours) {
        TextView tv_text = (TextView) findViewById(resourceId);

        if (openingHours.isEmpty()) {
            tv_text.setVisibility(View.GONE);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(queryResults.getLastQueryTime());
            int currentTime = (calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek()) * 1440 +
                    calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                    calendar.get(Calendar.MINUTE);
            int periodStart = 0, periodEnd = 10800;
            int nearestStartTime = -1;
            boolean opening = false;
            boolean neverClose = false;

            for (Restaurant.OpeningHours openingHour : openingHours) {
                // Case 1: start is 0 and end is 10800
                // this indicates 24/7
                if (openingHour.start == 0 && openingHour.end == 10800) {
                    // never close
                    neverClose = true;
                    break;
                }
                // Case 2: start < end, general case
                // See if current time falls into it
                else if (openingHour.start <= openingHour.end) {
                    // current time falls in a opening hours period
                    if (openingHour.start <= currentTime && currentTime <= openingHour.end) {
                        opening = true;
                        periodStart = openingHour.start;
                        periodEnd = openingHour.end;
                        break;
                    }
                }
                // Case 3: end < start, special case like Sat 23:00 to Sun 1:00
                // The current time check logic needs to change
                else if (openingHour.start > openingHour.end) {
                    // current time falls in a opening hours period
                    if (!(openingHour.end < currentTime && currentTime < openingHour.start)) {
                        opening = true;
                        periodStart = openingHour.start;
                        periodEnd = openingHour.end;
                        break;
                    }
                }

                // Keep tracking the nearest opening time
                if (currentTime <= openingHour.start && ((openingHour.start - currentTime) < (openingHour.start - nearestStartTime))) {
                    nearestStartTime = openingHour.start;
                }
            }

            if (neverClose) {
                // "Open (24/7)"
                tv_text.setText(getResources().getString(R.string.info_never_close));
            } else if (opening) {
                // "Open (17:00 - 22:00)"
                String text = getResources().getString(R.string.info_opening) + " (" +
                        periodTimeToString(periodStart) + " - " + periodTimeToString(periodEnd) + ")";
                tv_text.setText(text);
            } else {
                // nearest start time isn't today
                if (currentTime / 1440 != nearestStartTime / 1440) {
                    // "Closed Today"
                    tv_text.setText(getResources().getString(R.string.info_close_today));
                } else {
                    // "Closed (Open at 17:00)"
                    String text = getResources().getString(R.string.info_not_opening) + " (" +
                            getResources().getString(R.string.info_next_open_time) + " " +
                            periodTimeToString(nearestStartTime) + ")";
                    tv_text.setText(text);
                }
            }
        }
    }

    private String periodTimeToString(int time) {
        int hour, min;
        hour = time % 1440 / 60;
        min = time % 60;
        return (String.format(Locale.getDefault(), "%02d", hour) + ":" + String.format(Locale.getDefault(), "%02d", min));
    }

    private void fillOneReviewItem(int resourceId, Restaurant.Review review) {
        LinearLayout container = (LinearLayout) findViewById(resourceId);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.review_item, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 24);
        view.setLayoutParams(params);
        ((TextView) view.findViewById(R.id.author)).setText(review.author);
        ((RatingBar) view.findViewById(R.id.rating_bar)).setRating(review.rating);
        ((TextView) view.findViewById(R.id.date)).setText(convertReviewTime(review.time));
        ((TextView) view.findViewById(R.id.text)).setText(review.text);

        container.addView(view);
    }

    private String convertReviewTime(long reviewTime) {
        long now = queryResults.getLastQueryTime() / 1000;
        long diff = now / 86400 - reviewTime / 86400;
        if (diff < 1) {
            // Within a day. Note that "review from the future" error case included
            // Display "today"
            return getString(R.string.today);
        } else if (diff < 14) {
            // Within 14 days
            // Display "n days ago"
            return diff + " " + getString(R.string.days_ago);
        } else if (diff < 30) {
            // Within a month
            // Display "n weeks ago"
            return diff / 7 + " " + getString(R.string.weeks_ago);
        } else if (diff < 56) {
            // Within 2 months
            // Display "1 month ago"
            return 1 + " " + getString(R.string.month_ago);
        } else if (diff < 365) {
            // Within a year
            // Display "n months ago"
            return diff / 30 + " " + getString(R.string.months_ago);
        } else if (diff < 730) {
            // Within two years
            // Display "1 year ago"
            return 1 + " " + getString(R.string.year_ago);
        } else {
            // Greater than two years
            // Display "n years ago"
            return diff / 365 + " " + getString(R.string.years_ago);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_open_map:
                Util.startGoogleMapActivity(this, queryResults.getOneRestaurant(currentIndex));
                break;
            case R.id.button_phone_call:
                Util.startPhoneCallActivity(this, queryResults.getOneRestaurant(currentIndex));
                break;
            case R.id.button_toggle_detail:
                LinearLayout ll_detail_container = (LinearLayout) findViewById(R.id.ll_detail_container);
                ImageButton btn_detail = (ImageButton) findViewById(R.id.button_toggle_detail);
                if (detailToggle) {
                    ll_detail_container.setVisibility(View.GONE);
                    btn_detail.setImageResource(R.drawable.ic_down_arrow);
                } else {
                    ll_detail_container.setVisibility(View.VISIBLE);
                    btn_detail.setImageResource(R.drawable.ic_up_arrow);
                }
                detailToggle = !detailToggle;
                break;
        }
    }
}

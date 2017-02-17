package com.pychen0918.wheretoeat.common;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.kobakei.ratethisapp.RateThisApp;
import com.pychen0918.wheretoeat.ListResultActivity;
import com.pychen0918.wheretoeat.R;
import com.pychen0918.wheretoeat.SettingsActivity;
import com.pychen0918.wheretoeat.SingleResultActivity;

import java.util.List;
import java.util.Locale;

/**
 * Created by pychen0918 on 2016/12/19.
 */

public final class Util {
    private Util(){}

    public static void fillOneLabeledItem(Context context, LinearLayout container, int labelStringId, String text) {
        if (container == null)
            return;
        if (text != null && !text.isEmpty()) {
            TextView tv_label = (TextView) container.findViewById(R.id.textview_label);
            TextView tv_text = (TextView) container.findViewById(R.id.textview_text);
            tv_label.setText(context.getResources().getString(labelStringId));
            tv_text.setText(text);
        } else {
            container.setVisibility(View.GONE);
        }
    }

    public static void fillOneLabeledLink(Context context, LinearLayout container, int labelStringId, String text) {
        if (container == null)
            return;
        if (text != null && !text.isEmpty()) {
            TextView tv_label = (TextView) container.findViewById(R.id.textview_label);
            TextView tv_text = (TextView) container.findViewById(R.id.textview_text);
            tv_label.setText(context.getResources().getString(labelStringId));
            tv_text.setAutoLinkMask(Linkify.ALL);
            tv_text.setMovementMethod(LinkMovementMethod.getInstance());
            tv_text.setText(text);
        } else {
            container.setVisibility(View.GONE);
        }
    }

    public static void initialRateThisApp(final Context context){
        RateThisApp.Config config = new RateThisApp.Config(Constants.rateAppIntervalDays, Constants.rateAppIntervalLaunches);
        config.setTitle(R.string.action_rate_app);
        config.setMessage(R.string.rate_this_app_message);
        config.setYesButtonText(R.string.rate_this_app_rate);
        config.setNoButtonText(R.string.rate_this_app_thanks);
        config.setCancelButtonText(R.string.rate_this_app_cancel);
        RateThisApp.init(config);
        RateThisApp.setCallback(new RateThisApp.Callback() {
            @Override
            public void onYesClicked() {
                Toast.makeText(context, context.getString(R.string.info_thank_you_for_rating), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNoClicked() {
            }

            @Override
            public void onCancelClicked() {
            }
        });
    }

    public static void showAboutDialog(Context context) {
        PackageInfo pInfo;
        String version = "";
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.about, null);

        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Util.fillOneLabeledItem(context, (LinearLayout) view.findViewById(R.id.about_version), R.string.label_version, version);
        Util.fillOneLabeledItem(context, (LinearLayout) view.findViewById(R.id.about_author), R.string.label_author, Credential.authorName);
        Util.fillOneLabeledLink(context, (LinearLayout) view.findViewById(R.id.about_email), R.string.label_email, Credential.authorEmail);

        builder.setView(view)
                .setPositiveButton(context.getResources().getString(R.string.action_close), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    public static void showGooglePlaceApiErrorMessage(Context context) {
        Toast.makeText(context, context.getString(R.string.error_google_place_fail), Toast.LENGTH_SHORT).show();
    }

    public static void initAdView(AdView adview){
        if(adview != null) {
            AdRequest adRequest = new AdRequest.Builder().addTestDevice(Credential.testDevice).build();
            adview.loadAd(adRequest);
        }
    }

    public static void startSingleResultActivity(Context context, int index, boolean useIu){
        Intent intent = new Intent(context, SingleResultActivity.class);
        intent.putExtra("INDEX", index);
        intent.putExtra("USEIU", useIu);
        context.startActivity(intent);
    }

    public static void startListResultActivity(Context context, String type, boolean useIu){
        Intent intent = new Intent(context, ListResultActivity.class);
        intent.putExtra("TYPE", type);
        intent.putExtra("USEIU", useIu);
        context.startActivity(intent);
    }

    public static void startSettingsActivity(Context context){
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void startShareAppActivity(Context context){
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, Util.getPromotion(context) + "\n" + Util.getAppGooglePlayLink(context));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, context.getString(R.string.info_no_messaging_app), Toast.LENGTH_SHORT).show();
        }
    }

    public static void startShareRestaurantInfoActivity(Context context, Restaurant restaurant) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, restaurant.getName());
        intent.putExtra(Intent.EXTRA_TEXT, restaurant.getName() + " \n" + restaurant.getAddress() + " \n" + restaurant.getPhoneNumber());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, context.getString(R.string.info_no_messaging_app), Toast.LENGTH_SHORT).show();
        }
    }

    public static void startShareListActivity(Context context, String type, List<Restaurant> list){
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, type);
        intent.putExtra(Intent.EXTRA_TEXT, type + ": " + Util.getRestaurantsListString(list));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, context.getString(R.string.info_no_messaging_app), Toast.LENGTH_SHORT).show();
        }
    }

    public static void startPhoneCallActivity(Context context, Restaurant restaurant) {
        if (!restaurant.getPhoneNumber().isEmpty()) {
            Uri uri = Uri.parse("tel:" + restaurant.getPhoneNumber());
            Intent intent = new Intent(Intent.ACTION_DIAL, uri);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else
                Toast.makeText(context, context.getString(R.string.info_no_phone_app), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.info_no_phone_number), Toast.LENGTH_SHORT).show();
        }
    }

    public static void startGoogleMapActivity(Context context, Restaurant restaurant) {
        Uri uri;
        // if we have acquire google map URL (which should always be the case), use it
        if (!restaurant.getUrl().isEmpty()) {
            uri = Uri.parse(restaurant.getUrl());
        }
        // if not, use geo location, address and title
        else {
            uri = Uri.parse("geo:" + restaurant.getLat() + "," + restaurant.getLon() +
                    "?z=" + 7 +
                    "&q=" + Uri.encode(restaurant.getAddress() + " " + restaurant.getName()));
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, context.getString(R.string.info_no_map_app), Toast.LENGTH_SHORT).show();
        }
    }

    public static void startBrowseWebsiteActivity(Context context, Restaurant restaurant) {
        Uri uri;
        if (!restaurant.getWebsite().isEmpty()) {
            uri = Uri.parse(restaurant.getWebsite());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            }
            else{
                Toast.makeText(context, context.getString(R.string.info_no_browser_app), Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(context, context.getString(R.string.info_no_website), Toast.LENGTH_SHORT).show();
        }
    }

    public static String getDistanceDisplayString(Context context, long distance, boolean useIu){
        String unit1, unit2;
        double displayDistance;
        if(!useIu){
            unit1 = context.getString(R.string.kilometers);
            unit2 = context.getString(R.string.meters);
            displayDistance = distance;
            if(displayDistance > 1000.0){
                return (String.format(Locale.getDefault(), "%.1f", (displayDistance/1000.0)) + " " +unit1);
            }
            else{
                return (String.format(Locale.getDefault(), "%d", (int)(displayDistance/10)*10) + " " + unit2);
            }
        }
        else{
            unit1 = context.getString(R.string.miles);
            unit2 = context.getString(R.string.yards);
            displayDistance = distance*0.000621371;
            if(displayDistance > 0.5){
                return (String.format(Locale.getDefault(), "%.1f", displayDistance) + " " +unit1);
            }
            else{
                return (String.format(Locale.getDefault(), "%d", (int)((displayDistance*1760)/10)*10) + " " + unit2);
            }
        }
    }

    private static String getPromotion(Context context) {
        return context.getString(R.string.download) + " \"" + context.getString(R.string.app_name) + "\" APP:";
    }

    private static String getAppGooglePlayLink(Context context) {
        return Constants.googlePlayUrl + "?id=" + context.getPackageName();
    }

    private static String getRestaurantsListString(List<Restaurant> list){
        String result = "";
        for(Restaurant item : list){
            result = result + item.getName() + ", ";
        }
        if(result.length() >= 2){
            result = result.substring(0, result.length() - 2);  // remove the last ", "
        }
        return result;
    }
}

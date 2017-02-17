package com.pychen0918.wheretoeat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.pychen0918.wheretoeat.common.Restaurant;
import com.pychen0918.wheretoeat.common.Util;

import java.util.List;
import java.util.Locale;

/**
 * Created by pychen0918 on 2016/11/28.
 */

public class ListRestaurantRecyclerAdapter extends RecyclerView.Adapter<ListRestaurantRecyclerAdapter.ViewHolder> implements View.OnClickListener {
    private List<Restaurant> mRestaurantList;
    private Context mContext;
    private boolean mUseIu;
    private OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = null;

    public ListRestaurantRecyclerAdapter(Context context, List<Restaurant> restaurants) {
        this.mRestaurantList = restaurants;
        this.mContext = context;
        this.mUseIu = false;
    }

    public Context getContext() {
        return mContext;
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnRecyclerViewItemClickListener = listener;
    }

    public void setUseIu(boolean useIu) {
        this.mUseIu = useIu;
    }

    @Override
    public ListRestaurantRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View itemView = inflater.inflate(R.layout.restaurant_list_item, parent, false);
        itemView.setOnClickListener(this);

        // Return a new holder instance
        return new ListRestaurantRecyclerAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ListRestaurantRecyclerAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Restaurant restaurant = mRestaurantList.get(position);

        // Set item views based on your views and data model
        holder.icon.setImageResource(getTypeResourceId(restaurant.getTypes()));
        holder.title.setText(restaurant.getName());
        holder.ratingBar.setRating(restaurant.getRating());
        holder.ratingText.setText("(" + String.format(Locale.getDefault(), "%.1f", restaurant.getRating()) + ")");
        holder.subtitle.setText(Util.getDistanceDisplayString(mContext, restaurant.getDistance(), mUseIu));
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mRestaurantList.size();
    }

    @Override
    public void onClick(View view) {
        if (mOnRecyclerViewItemClickListener != null) {
            mOnRecyclerViewItemClickListener.onItemClick(view, (int) view.getTag());
        }
    }

    private int getTypeResourceId(List<String> types) {
        if (types.contains("bakery"))
            return R.drawable.ic_toast;
        if (types.contains("bar"))
            return R.drawable.ic_drink;
        if (types.contains("cafe"))
            return R.drawable.ic_coffee_cup;
        if (types.contains("convenience_store"))
            return R.drawable.ic_time;
        if (types.contains("grocery_or_supermarket"))
            return R.drawable.ic_groceries;
        if (types.contains("meal_delivery"))
            return R.drawable.ic_delivery;
        if (types.contains("meal_takeaway"))
            return R.drawable.ic_paper_bag;
        if (types.contains("night_club"))
            return R.drawable.ic_disco_ball;
        if (types.contains("shopping_mall"))
            return R.drawable.ic_online_store;
        if (types.contains("restaurant"))
            return R.drawable.ic_cutlery;
        if(types.contains("food"))
            return R.drawable.ic_fried_egg;

        return R.drawable.ic_placeholder;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView title;
        public RatingBar ratingBar;
        public TextView ratingText;
        public TextView subtitle;

        public ViewHolder(View itemView) {
            super(itemView);

            icon = (ImageView) itemView.findViewById(R.id.imageview_icon);
            title = (TextView) itemView.findViewById(R.id.item_textview_title);
            ratingBar = (RatingBar) itemView.findViewById(R.id.item_rating_bar);
            ratingText = (TextView) itemView.findViewById(R.id.item_rating_text);
            subtitle = (TextView) itemView.findViewById(R.id.item_textview_subtitle);
        }
    }
}

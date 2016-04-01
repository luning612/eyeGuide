package com.estimote.examples.demos.adapters;

import android.content.Context;
import com.estimote.examples.demos.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.estimote.examples.demos.R;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Displays basic information about nearable.
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class LocationReportAdapter extends BaseAdapter {
//
    private Nearable nearable;
    private Location loc;
    private List<Location> nearbyLocations;
    private LayoutInflater inflater;

    public LocationReportAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.nearbyLocations = new ArrayList<>();
    }
//
//    public void replaceWith(Nearable nearable, Location location, List<Location> newNearbyLocations) {
//        this.loc = location;
//        this.nearable = nearable;
//        this.nearbyLocations.clear();
//        this.nearbyLocations.addAll(newNearbyLocations);
//        notifyDataSetChanged();
//    }

    @Override
    public int getCount() {
        return nearbyLocations.size();
    }

    @Override
    public Nearable getItem(int position) {
        return nearable;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        //view = inflateIfRequired(view, position, parent);
        //bind(getItem(position), view);
        return view;
    }
//
//    private void bind(Nearable nearable, View view) {
//        ViewHolder holder = (ViewHolder) view.getTag();
//        holder.macTextView.setText(String.format("ID: %s (%s)", nearable.identifier, Utils.computeProximity(nearable).toString()));
//        holder.rssiTextView.setText("RSSI: " + nearable.rssi);
//        holder.locTextView.setText("Location: " + loc.getName());
//        holder.loctxtTextView.setText("MPower: " + loc.getText());
//    }
//
//    private View inflateIfRequired(View view, int position, ViewGroup parent) {
//        if (view == null) {
//            view = inflater.inflate(R.layout.nearable_item, null);
//            view.setTag(new ViewHolder(view));
//        }
//        return view;
//    }
//
//    static class ViewHolder {
//        final TextView macTextView;
//        final TextView rssiTextView;
//        final TextView locTextView;
//        final TextView loctxtTextView;
//
//        ViewHolder(View view) {
//            macTextView = (TextView) view.findViewById(R.id.macc);
//            rssiTextView = (TextView) view.findViewById(R.id.rssic);
//            locTextView = (TextView) view.findViewById(R.id.locc);
//            loctxtTextView = (TextView) view.findViewById(R.id.loctxtc);
//        }
//    }
}

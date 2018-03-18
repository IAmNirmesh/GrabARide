package rahul.nirmesh.grabaride.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import rahul.nirmesh.grabaride.R;

/**
 * Created by NIRMESH on 19-Mar-18.
 */

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    View myView;

    public CustomInfoWindow(Context context) {
        myView = LayoutInflater.from(context).inflate(R.layout.custom_rider_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView textPickupInfo = myView.findViewById(R.id.textPickupInfo);
        textPickupInfo.setText(marker.getTitle());

        TextView textPickupSnippet = myView.findViewById(R.id.textPickupSnippet);
        textPickupSnippet.setText(marker.getSnippet());

        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}

package rahul.nirmesh.grabaride;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rahul.nirmesh.grabaride.common.Common;
import rahul.nirmesh.grabaride.remote.IGoogleAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by NIRMESH on 19-Mar-18.
 */

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {
    String mLocation, mDestination;
    boolean isTapOnMap;

    IGoogleAPI mService;

    TextView txtLocation, txtDestination, txtCalculate;

    public static BottomSheetRiderFragment newInstance (String location, String destination, boolean isTapOnMap) {
        BottomSheetRiderFragment f = new BottomSheetRiderFragment();
        Bundle args = new Bundle();
        args.putString("location", location);
        args.putString("destination", destination);
        args.putBoolean("isTapOnMap", isTapOnMap);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");
        isTapOnMap = getArguments().getBoolean("isTapOnMap");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_rider, container, false);

        txtLocation = view.findViewById(R.id.txtLocation);
        txtDestination = view.findViewById(R.id.txtDestination);
        txtCalculate = view.findViewById(R.id.txtCalculate);

        mService = Common.getGoogleService();

        getTotalFare(mLocation, mDestination);

        if (!isTapOnMap) {
            txtLocation.setText(mLocation);
            txtDestination.setText(mDestination);
        }

        return view;
    }

    private void getTotalFare(String mLocation, String mDestination) {
        String requestUrl = null;

        try {
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?"
                        + "mode-driving&"
                        + "transit_routing_preference=less_driving&"
                        + "origin=" + mLocation + "&"
                        + "destination=" + mDestination + "&"
                        + "key=" + getResources().getString(R.string.google_browser_key);

            Log.e("LINK: ", requestUrl);

            mService.getPath(requestUrl).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");
                        Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]", ""));

                        JSONObject duration = legsObject.getJSONObject("duration");
                        String duration_text = duration.getString("text");
                        Integer duration_value = Integer.parseInt(duration_text.replaceAll("\\D+", ""));

                        String final_calculated_fare = String.format("%s + %s = $%.2f",
                                                            distance_text, duration_text,
                                                            Common.getPrice(distance_value, duration_value));

                        txtCalculate.setText(final_calculated_fare);

                        if (isTapOnMap) {
                            String start_address = legsObject.getString("start_address");
                            String end_address = legsObject.getString("end_address");

                            txtLocation.setText(start_address);
                            txtDestination.setText(end_address);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("ERROR: ", t.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

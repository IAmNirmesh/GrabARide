package rahul.nirmesh.grabaride.common;

import rahul.nirmesh.grabaride.model.Rider;
import rahul.nirmesh.grabaride.remote.FCMClient;
import rahul.nirmesh.grabaride.remote.GoogleMapsAPI;
import rahul.nirmesh.grabaride.remote.IFCMService;
import rahul.nirmesh.grabaride.remote.IGoogleAPI;

/**
 * Created by NIRMESH on 19-Mar-18.
 */

public class Common {
    public static boolean isDriverFound = false;
    public static String driverId = "";

    public static Rider currentRider = new Rider();

    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "DriversInformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";
    public static final String rate_details_tbl = "RateDetails";

    public static final String user_field = "rider_usr";
    public static final String password_field = "rider_pwd";

    public static final int PICK_IMAGE_REQUEST = 9999;

    public static final String fcmURL = "https://fcm.googleapis.com/";

    public static IFCMService getFCMService() {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

    public static final String googleAPIUrl = "https://maps.googleapis.com/";

    public static IGoogleAPI getGoogleService() {
        return GoogleMapsAPI.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }

    private static double base_fare = 2.55;
    private static double time_rate = 0.35;
    private static double distance_rate = 1.75;

    public static double getPrice(double km, int min) {
        return (base_fare + (time_rate * min) + (distance_rate * km));
    }
}

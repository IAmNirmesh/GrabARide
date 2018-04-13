package rahul.nirmesh.grabaride;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import rahul.nirmesh.grabaride.common.Common;
import rahul.nirmesh.grabaride.helper.CustomInfoWindow;
import rahul.nirmesh.grabaride.model.FCMResponse;
import rahul.nirmesh.grabaride.model.Notification;
import rahul.nirmesh.grabaride.model.Rider;
import rahul.nirmesh.grabaride.model.Sender;
import rahul.nirmesh.grabaride.model.Token;
import rahul.nirmesh.grabaride.remote.IFCMService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback {

    SupportMapFragment mapFragment;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 1000;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private LocationRequest mLocationRequest;

    Marker mUserMarker, markerDestination;

    ImageView imgExpandable;
    Button btnPickupRequest;

    int radius = 1;
    int distance = 1;
    private static final int LIMIT = 3;

    IFCMService mService;

    DatabaseReference driversAvailable;

    PlaceAutocompleteFragment place_location, place_destination;

    AutocompleteFilter typeFilter;

    String mPlaceLocation, mPlaceDestination;

    CircleImageView imageAvatar;
    TextView txtRiderName, txtStars;

    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = Common.getFCMService();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView = navigationView.getHeaderView(0);

        txtRiderName = navigationHeaderView.findViewById(R.id.txtRiderName);
        txtRiderName.setText(String.format("%s", Common.currentRider.getName()));

        txtStars = navigationHeaderView.findViewById(R.id.txtStars);
        txtStars.setText(String.format("%s", Common.currentRider.getRates()));

        imageAvatar = navigationHeaderView.findViewById(R.id.imageAvatar);
        if (Common.currentRider.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentRider.getAvatarUrl())) {
            Picasso.with(this).load(Common.currentRider.getAvatarUrl()).into(imageAvatar);
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        imgExpandable = findViewById(R.id.imgExpandable);

        btnPickupRequest = findViewById(R.id.btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Common.isDriverFound)
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                else
                    sendRequestToDriver(Common.driverId);
            }
        });

        place_location = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_location);
        place_destination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_destination);

        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceLocation = place.getAddress().toString();

                mMap.clear();

                mUserMarker = mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                        .title("Pickup Here."));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
            }

            @Override
            public void onError(Status status) {

            }
        });

        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceDestination = place.getAddress().toString();

                mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                        .title("Destination"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

                BottomSheetRiderFragment mBottomSheet = BottomSheetRiderFragment.newInstance(mPlaceLocation, mPlaceDestination, false);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }

            @Override
            public void onError(Status status) {

            }
        });

        setUpLocation();

        updateFirebaseToken();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createLocationRequest();
                    displayLocation();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_signOut) {
            signOut();
        } else if (id == R.id.nav_updateInformation) {
            showDialogUpdateInformation();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            boolean isSuccess = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_style_map));

            if (!isSuccess)
                Log.e("ERROR: ", "Map Style Loading Failed!!!");
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (markerDestination != null)
                    markerDestination.remove();

                markerDestination = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                        .position(latLng)
                        .title("Destination"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

                BottomSheetRiderFragment mBottomSheet = BottomSheetRiderFragment.newInstance(
                        String.format("%f, %f", Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()),
                        String.format("%f, %f", latLng.latitude, latLng.longitude),
                        true);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            buildLocationCallBack();
            createLocationRequest();
            displayLocation();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Common.mLastLocation = location;
                if (Common.mLastLocation != null) {
                    LatLng center = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
                    // Heading: 0 - NorthSide, 90 - EastSide, 180 - SouthSide, 270 - WestSide
                    LatLng northSide = SphericalUtil.computeOffset(center, 100000, 0);
                    LatLng southSide = SphericalUtil.computeOffset(center, 100000, 180);

                    LatLngBounds bounds = LatLngBounds.builder()
                            .include(northSide)
                            .include(southSide)
                            .build();

                    place_location.setBoundsBias(bounds);
                    place_location.setFilter(typeFilter);

                    place_destination.setBoundsBias(bounds);
                    place_destination.setFilter(typeFilter);

                    driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
                    driversAvailable.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            loadAllAvailableDrivers(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    final double latitude = Common.mLastLocation.getLatitude();
                    final double longitude = Common.mLastLocation.getLongitude();

                    loadAllAvailableDrivers(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));

                    Log.d("CURRENT-LOCATION: ", String.format("Your Location was changed: %f / %f", latitude, longitude));
                } else {
                    Log.d("ERROR-LOCATION: ", "Cannot Get Your Location.");
                }
            }
        });
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Common.mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size() - 1);
                displayLocation();
            }
        };
    }

    private void loadAllAvailableDrivers(final LatLng location) {
        mMap.clear();

        mUserMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(location)
                .title("You"));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire mGeoFireDriverLocation = new GeoFire(driverLocation);

        GeoQuery geoQuery = mGeoFireDriverLocation.queryAtLocation(
                new GeoLocation(location.latitude, location.longitude), distance);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Because Rider & Users model are exactly same
                                // So we can use Rider's model to fetch User's information
                                Rider rider = dataSnapshot.getValue(Rider.class);

                                // Add Driver to Map
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .flat(true)
                                        .title(rider.getName())
                                        .snippet("Phone: " + rider.getPhone())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance <= LIMIT) {
                    distance++;
                    loadAllAvailableDrivers(location);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void requestPickupHere(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));

        if (mUserMarker.isVisible())
            mUserMarker.remove();

        mUserMarker = mMap.addMarker(new MarkerOptions().title("Pickup Here")
                .snippet("")
                .position(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mUserMarker.showInfoWindow();

        btnPickupRequest.setText("Getting your Driver...");

        findAvailableDrivers();
    }

    private void findAvailableDrivers() {
        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire mGeoFireDrivers = new GeoFire(drivers);

        final GeoQuery geoQuery = mGeoFireDrivers.queryAtLocation(
                new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!Common.isDriverFound) {
                    Common.isDriverFound = true;
                    Common.driverId = key;
                    btnPickupRequest.setText("CALL DRIVER");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!Common.isDriverFound && radius < LIMIT) {
                    radius++;
                    findAvailableDrivers();
                } else {
                    if (!Common.isDriverFound) {
                        Toast.makeText(Home.this, "No Driver Available new You.", Toast.LENGTH_SHORT).show();
                        btnPickupRequest.setText("REQUEST PICKUP");
                        geoQuery.removeAllListeners();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendRequestToDriver(String driverId) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Token token = postSnapshot.getValue(Token.class);

                            String json_lat_lng = new Gson().toJson(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                            String riderToken = FirebaseInstanceId.getInstance().getToken();

                            Notification data = new Notification(riderToken, json_lat_lng);

                            Sender sender = new Sender(token.getToken(), data);

                            mService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if (response.body().success == 1)
                                        Toast.makeText(Home.this, "Request Sent !", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(Home.this, "Request Sent Failed !", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {
                                    Log.e("ERROR: ", t.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

    private void signOut() {

        Paper.init(this);
        Paper.book().destroy();

        FirebaseAuth.getInstance().signOut();
        Intent intentSignOut = new Intent(Home.this, MainActivity.class);
        startActivity(intentSignOut);
        finish();
    }

    private void showDialogUpdateInformation() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
        dialog.setTitle("UPDATE INFORMATION");
        dialog.setMessage("Please Enter Your Information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_update_information = inflater.inflate(R.layout.layout_update_information, null);

        final MaterialEditText editUpdateName = layout_update_information.findViewById(R.id.editUpdateName);
        final MaterialEditText editUpdatePhone = layout_update_information.findViewById(R.id.editUpdatePhone);

        final ImageView imageUpdateUpload = layout_update_information.findViewById(R.id.imageUpdateUpload);
        imageUpdateUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        dialog.setView(layout_update_information);

        dialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                final AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                String name = editUpdateName.getText().toString();
                String phone = editUpdatePhone.getText().toString();

                Map<String, Object> updateInformation = new HashMap<>();
                if (!TextUtils.isEmpty(name))
                    updateInformation.put("name", name);
                if (!TextUtils.isEmpty(phone))
                    updateInformation.put("phone", phone);

                DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl);
                riderInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(updateInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        waitingDialog.dismiss();
                        if (task.isSuccessful())
                            Toast.makeText(Home.this, "Information Updated", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(Home.this, "Information Not Updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void selectImage() {
        Intent intentSelectImage = new Intent();
        intentSelectImage.setType("image/*");
        intentSelectImage.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentSelectImage, "Select Image:"), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri saveUri = data.getData();
            if (saveUri != null) {
                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Uploading...");
                mDialog.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = storageReference.child("images/" + imageName);
                imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mDialog.dismiss();

                        imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map<String, Object> avatarUpdate = new HashMap<>();
                                avatarUpdate.put("avatarUrl", uri.toString());

                                DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl);
                                riderInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .updateChildren(avatarUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            Toast.makeText(Home.this, "Avatar Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(Home.this, "Avatar Uploading Failed.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                mDialog.setMessage(progress + "% Uploaded");
                            }
                        });
            }
        }
    }
}

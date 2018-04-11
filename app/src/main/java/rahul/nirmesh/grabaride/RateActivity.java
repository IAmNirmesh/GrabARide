package rahul.nirmesh.grabaride;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import rahul.nirmesh.grabaride.common.Common;
import rahul.nirmesh.grabaride.model.Rate;

public class RateActivity extends AppCompatActivity {

    MaterialRatingBar ratingBar;
    MaterialEditText editComment;
    Button btnSubmitComment;

    FirebaseDatabase database;
    DatabaseReference rateDetailRef;
    DatabaseReference driverInformationRef;

    double ratingStars = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        database = FirebaseDatabase.getInstance();
        rateDetailRef = database.getReference(Common.rate_details_tbl);
        driverInformationRef = database.getReference(Common.user_driver_tbl);

        ratingBar = findViewById(R.id.ratingBar);
        editComment = findViewById(R.id.editComment);
        btnSubmitComment = findViewById(R.id.btnSubmitComment);

        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingStars = rating;
            }
        });

        btnSubmitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitRatingDetails(Common.driverId);
            }
        });
    }

    private void submitRatingDetails(final String driverId) {
        final AlertDialog alertDialog = new SpotsDialog(this);
        alertDialog.show();

        Rate rate = new Rate();
        rate.setRates(String.valueOf(ratingStars));
        rate.setComments(editComment.getText().toString());

        rateDetailRef.child(driverId)
                .push()
                .setValue(rate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        rateDetailRef.child(driverId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                double averageStars = 0.0;
                                int count = 0;

                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    Rate rate = postSnapshot.getValue(Rate.class);
                                    averageStars += Double.parseDouble(rate.getRates());
                                    count++;
                                }

                                double finalAverage = averageStars / count;
                                DecimalFormat decimalFormat = new DecimalFormat("#.#");
                                String valueUpdate = decimalFormat.format(finalAverage);

                                Map<String, Object> driverUpdatedRate = new HashMap<>();
                                driverUpdatedRate.put("rates", valueUpdate);

                                driverInformationRef.child(Common.driverId)
                                        .updateChildren(driverUpdatedRate)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                alertDialog.dismiss();
                                                Toast.makeText(RateActivity.this, "Thank You for Rating.", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                alertDialog.dismiss();
                                                String message = "Rating is done but not yet updated to Driver's Profile";
                                                Toast.makeText(RateActivity.this, "" + message, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        alertDialog.dismiss();
                        Toast.makeText(RateActivity.this, "Try again to Rate the Driver", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

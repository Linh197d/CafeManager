package com.app.shopfee.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.app.shopfee.MyApplication;
import com.app.shopfee.R;
import com.app.shopfee.model.Rating;
import com.app.shopfee.model.RatingReview;
import com.app.shopfee.utils.Constant;
import com.app.shopfee.utils.GlobalFunction;
import com.app.shopfee.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RatingReviewActivity extends BaseActivity {

    private RatingBar ratingBar;
    private EditText edtReview;
    private TextView tvSendReview;

    private RatingReview ratingReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_review);

        getDataIntent();
        initToolbar();
        initUi();
        loadRatingData();
        initListener();
    }

    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ratingReview = (RatingReview) bundle.getSerializable(Constant.RATING_REVIEW_OBJECT);
        }
    }

    private void initUi() {
        ratingBar = findViewById(R.id.ratingbar);
        edtReview = findViewById(R.id.edt_review);
        tvSendReview = findViewById(R.id.tv_send_review);

        TextView tvMessageReview = findViewById(R.id.tv_message_review);
        if (ratingReview != null) {
            if (RatingReview.TYPE_RATING_REVIEW_DRINK == ratingReview.getType()) {
                tvMessageReview.setText(getString(R.string.label_rating_review_drink));
            } else if (RatingReview.TYPE_RATING_REVIEW_ORDER == ratingReview.getType()) {
                tvMessageReview.setText(getString(R.string.label_rating_review_order));
            }
        }
    }

    private void initToolbar() {
        ImageView imgToolbarBack = findViewById(R.id.img_toolbar_back);
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        imgToolbarBack.setOnClickListener(view -> finish());
        tvToolbarTitle.setText(getString(R.string.ratings_and_reviews));
    }

    private void initListener() {
        tvSendReview.setOnClickListener(v -> {
            float rate = ratingBar.getRating();
            String review = edtReview.getText().toString().trim();
            Rating rating = new Rating(review, rate);
            if (ratingReview != null) {
                if (RatingReview.TYPE_RATING_REVIEW_DRINK == ratingReview.getType()) {
                    sendRatingDrink(rating);
                } else if (RatingReview.TYPE_RATING_REVIEW_ORDER == ratingReview.getType()) {
                    sendRatingOrder(rating);
                }
            }
            finish();
        });
    }

    private void sendRatingDrink(Rating rating) {
        if (ratingReview != null) {
            MyApplication.get(this).getRatingDrinkDatabaseReference(ratingReview.getId())
                    .child(String.valueOf(GlobalFunction.encodeEmailUser()))
                    .setValue(rating, (error, ref) -> {
                        showToastMessage(getString(R.string.msg_send_review_success));
                        ratingBar.setRating(5f);
                        edtReview.setText("");
                        Utils.hideSoftKeyboard(RatingReviewActivity.this);
                    });
        }
    }
    private void sendRatingOrder(Rating rating) {
        if (ratingReview != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("rate", rating.getRate());
            map.put("review", rating.getReview());

            MyApplication.get(this).getOrderDatabaseReference()
                    .child(String.valueOf(ratingReview.getId()))
                    .updateChildren(map, (error, ref) -> {
                        showToastMessage(getString(R.string.msg_send_review_success));
                        ratingBar.setRating(5f);
                        edtReview.setText("");
                        Utils.hideSoftKeyboard(RatingReviewActivity.this);
                    });
        }
    }

    private void loadRatingData() {
        if (ratingReview == null) return;

        if (RatingReview.TYPE_RATING_REVIEW_DRINK == ratingReview.getType()) {
            MyApplication.get(this).getRatingDrinkDatabaseReference(ratingReview.getId())
                    .child(String.valueOf(GlobalFunction.encodeEmailUser()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Rating rating = snapshot.getValue(Rating.class);
                                if (rating != null) {
                                    ratingBar.setRating(rating.getRate());
                                    edtReview.setText(rating.getReview());
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else if (RatingReview.TYPE_RATING_REVIEW_ORDER == ratingReview.getType()) {
            MyApplication.get(this).getOrderDatabaseReference()
                    .child(String.valueOf(ratingReview.getId()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Rating rating = snapshot.getValue(Rating.class);
                                if (rating != null) {
                                    ratingBar.setRating(rating.getRate());
                                    edtReview.setText(rating.getReview());
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

}


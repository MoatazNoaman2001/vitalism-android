package com.example.livenativerppg.models.searchMain.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.livenativerppg.R;
import com.example.livenativerppg.commons.FireBaseFunKt;
import com.example.livenativerppg.component.db.models.ConnectRequest;
import com.example.livenativerppg.component.utility.Variables;
import com.example.livenativerppg.databinding.SearchRecycleItemBinding;
import com.example.livenativerppg.databinding.SearchedUserAccountDetailsBinding;
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class SearchResultRecycleViewAdapter extends PagingDataAdapter<UserInfo, SearchResultRecycleViewAdapter.ViewHolder> {
    private static final String TAG = "SearchResultRRecycleVie";
    private final RequestManager imageLoader;
    private final UserClickInterface clickInterface;
    private final List<String> my_follow_ids;
    private AlertDialog alertDialog;
    private FirebaseUser user;
    private CollectionReference myFollowers;
    private CollectionReference myRequests;

    void setMyFollowIds(List<String> newList) {
        my_follow_ids.clear();
        my_follow_ids.addAll(newList);
    }

    public SearchResultRecycleViewAdapter(RequestManager imageLoader,
                                          CollectionReference myFollowers,
                                          CollectionReference myRequests,
                                          List<String> my_follow_ids, UserClickInterface clickInterface) {
        super(new ItemDiffUtils());
        this.imageLoader = imageLoader;
        this.clickInterface = clickInterface;
        this.my_follow_ids = my_follow_ids;
        this.myFollowers = myFollowers;
        this.myRequests = myRequests;
    }


    @NonNull
    @Override
    public SearchResultRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(SearchRecycleItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserInfo info = getItem(position);
        Log.d(TAG, "onBindViewHolder: " + info.toString());
        holder.binding.userName.setText(info.getName());
        holder.binding.userCountry.setText(info.getCountry());
        user = FirebaseAuth.getInstance().getCurrentUser();


        if (!info.getUid().isEmpty()) {
            FirebaseStorage.getInstance()
                    .getReference(Variables.FireStoreUsersRoot)
                    .child(info.getUid())
                    .child(Variables.UserProfilePic)
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        info.setProfileImgUri(uri.toString());
                        imageLoader.asBitmap()
                                .load(uri)
                                .circleCrop()
                                .transition(BitmapTransitionOptions.withCrossFade())
                                .into(holder.binding.userImageView);
                    })
                    .addOnFailureListener(e -> {
                        imageLoader
                                .asDrawable()
                                .load(R.drawable.patient)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(holder.binding.userImageView);
                    });
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.binding.getRoot().getContext());
            SearchedUserAccountDetailsBinding binding = SearchedUserAccountDetailsBinding.inflate(LayoutInflater.from(
                    holder.binding.getRoot().getContext()
            ));
            binding.UserName.setText(info.getName());
            binding.discription.setText(info.getDisc());
            binding.Uid.setText(info.getEmail());

            binding.heightTextView.setText(String.valueOf(info.getHeight()));
            binding.weightTextView.setText(String.valueOf(info.getWeight()));


            if (Build.VERSION.SDK_INT >= 26) {
                try {
                    binding.ageTextView.setText(String.valueOf(
                            Period.between(Instant.ofEpochMilli(new SimpleDateFormat("dd/MM/yyyy").parse(info.getBirthDay()).getTime()).atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()).getYears()
                    ));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }


            Log.d(TAG, "onBindViewHolder: my requests path: " + myRequests.document(info.getUid()).getPath());
            myRequests.document(info.getUid())
                    .addSnapshotListener((value, error) -> {
                        if (value == null) return;
                        binding.BtnLayout.setVisibility(View.VISIBLE);
                        Log.d(TAG, "onBindViewHolder: value exist: " + value.exists() + "\t is accepted: " + value.toObject(ConnectRequest.class));
                        if (value.exists() && value.toObject(ConnectRequest.class).getAccpeted()) {
                            if (value.toObject(ConnectRequest.class).getReceiverId() == user.getUid()) {
                                binding.partnerfollowmeText.setVisibility(View.VISIBLE);
                                binding.partnerfollowmeText.setText("this person is from your followers");
                            }
                            binding.connectBtn.setText(R.string.followed);
                            binding.rejectBtn.setVisibility(View.GONE);
//                            myFollowers.document(user.getUid())
//                                    .get()
//                                    .addOnSuccessListener(it2 -> {
//                                        if (it2.exists()) {
//                                          binding.FollowBackBtn.setVisibility(View.VISIBLE);
//                                            binding.FollowBackBtn.setOnClickListener(v12 -> {
//                                                clickInterface.OnClick(info, binding.connectBtn, binding.rejectBtn, binding.ConnectionSentProgress, 10, null);
//                                            });
//                                        }
//                                    });

                            binding.connectBtn.setOnClickListener(v1 -> {
                                clickInterface.OnClick(info, binding.connectBtn, binding.connectBtn, binding.ConnectionSentProgress, 11, null);
                            });
                        } else if (value.exists() && !value.toObject(ConnectRequest.class).getAccpeted()) {
                            binding.rejectBtn.setVisibility(View.VISIBLE);
                            if (value.toObject(ConnectRequest.class).getSenderId().equals(user.getUid())) {
                                binding.connectBtn.setText("cancel follow");
                                binding.rejectBtn.setVisibility(View.GONE);
                                binding.connectBtn.setOnClickListener(v1 -> clickInterface.OnClick(info, binding.connectBtn, binding.connectBtn, binding.ConnectionSentProgress, 12, null));
                            } else {
                                binding.connectBtn.setText("accept");
                                binding.rejectBtn.setVisibility(View.VISIBLE);
                                binding.connectBtn.setOnClickListener(v1 -> clickInterface.OnClick(info, binding.connectBtn, binding.connectBtn, binding.ConnectionSentProgress, 2, value.toObject(ConnectRequest.class)));
                                binding.rejectBtn.setOnClickListener(v1 -> clickInterface.OnClick(info, binding.connectBtn, binding.connectBtn, binding.ConnectionSentProgress, 5, value.toObject(ConnectRequest.class)));

                            }
                        } else {
                            FireBaseFunKt.getPartnerFollowerToMeInstance(info.getUid(), user.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        binding.rejectBtn.setVisibility(View.VISIBLE);
                                        if (documentSnapshot.exists()) {
                                            binding.FollowBackBtn.setVisibility(View.VISIBLE);
                                            binding.FollowBackBtn.setOnClickListener(v12 -> {
                                                clickInterface.OnClick(info, binding.connectBtn, binding.rejectBtn, binding.ConnectionSentProgress, 10, null);
                                            });
                                        } else {
                                            binding.connectBtn.setText(R.string.follow);
                                            binding.rejectBtn.setVisibility(View.GONE);
                                            binding.connectBtn.setOnClickListener(v1 -> {
                                                clickInterface.OnClick(info, binding.connectBtn, binding.rejectBtn, binding.ConnectionSentProgress, 10, null);
                                            });
                                        }
                                    });


                        }
                    });


//            if (Objects.equals(info.getUserType(), UserType.Medical.name())) {
//                FirebaseFirestore.getInstance()
//                        .collection(Variables.FireStoreUsersRoot)
//                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                        .collection(Variables.FOLLOWERS)
//                        .document(info.getUid())
//                        .get().addOnSuccessListener(it -> {
//                            if (it.exists()) {
//                                binding.connectBtn.setText(R.string.followed);
//                                binding.rejectBtn.setVisibility(View.GONE);
//                            } else {
//                                binding.connectBtn.setText(R.string.follow);
//                                binding.rejectBtn.setVisibility(View.GONE);
//
//                                binding.connectBtn.setOnClickListener(v1 -> {
//                                    clickInterface.OnClick(info, binding.connectBtn, binding.ConnectionSentProgress, 10, null);
//                                    binding.connectBtn.setVisibility(View.GONE);
//                                    binding.ConnectionSentProgress.setVisibility(View.VISIBLE);
//                                });
//
//                            }
//                        });
//
//            }
//            else {
//                Log.d(TAG, "onBindViewHolder: user info id: " + info.getUid());
//                FirebaseFirestore.getInstance()
//                        .collection(Variables.FireStoreUsersRoot)
//                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                        .collection(Variables.REQUEST)
//                        .document(info.getUid())
//                        .get()
//                        .addOnSuccessListener(documentSnapshot -> {
//                            Log.d(TAG, "onBindViewHolder: " + documentSnapshot.exists());
//                            if (documentSnapshot.exists()) {
//                                ConnectRequest connectRequest = documentSnapshot.toObject(ConnectRequest.class);
//                                if (connectRequest.getAccpeted()) {
//                                    binding.connectBtn.setText("Connected");
//                                    binding.connectBtn.setBackgroundColor(
//                                            ResourcesCompat.getColor(holder.binding.getRoot().getResources(), R.color.white, holder.binding.getRoot().getContext().getTheme())
//                                    );
//                                    binding.connectBtn.setEnabled(false);
//                                    binding.connectBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_round_check_24, 0);
//                                } else {
//                                    if (connectRequest.getSenderId().equals(info.getUid())) {
//                                        binding.connectBtn.setText("Accept");
//                                        binding.rejectBtn.setVisibility(View.VISIBLE);
//                                        binding.connectBtn.setOnClickListener(v1 -> {
//                                            clickInterface.OnClick(info, binding.connectBtn, binding.ConnectionSentProgress, 2, connectRequest);
//                                            binding.connectBtn.setVisibility(View.GONE);
//                                            binding.ConnectionSentProgress.setVisibility(View.VISIBLE);
//                                        });
//                                    } else {
//                                        binding.connectBtn.setText("Cancel Request");
//                                        binding.connectBtn.setOnClickListener(v1 -> {
//                                            clickInterface.OnClick(info, binding.connectBtn, binding.ConnectionSentProgress, 4, connectRequest);
//                                            binding.connectBtn.setVisibility(View.GONE);
//                                            binding.ConnectionSentProgress.setVisibility(View.VISIBLE);
//                                        });
//                                    }
//                                }
//                            } else {
//                                binding.connectBtn.setOnClickListener(v1 -> {
//                                    clickInterface.OnClick(info, binding.connectBtn, binding.ConnectionSentProgress, 8, null);
//                                    binding.ConnectionSentProgress.setVisibility(View.VISIBLE);
//                                    binding.connectBtn.setVisibility(View.GONE);
//                                });
//                            }
//
//                        });
//
//                try {
//                    if (info.getBirthDay() != null && !info.getBirthDay().isEmpty()) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            binding.ageTextView.setText(
//                                    String.valueOf(
//                                            Period.between(Instant.ofEpochMilli(new SimpleDateFormat("dd/MM/yyyy").parse(info.getBirthDay()).getTime())
//                                                    .atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()).getYears()
//                                    )
//                            );
//                        }
//                    }
//                } catch (ParseException e) {
//                    throw new RuntimeException(e);
//                }
//            }

            builder.setView(binding.getRoot());
            imageLoader.load(info.getProfileImgUri()).circleCrop().into(binding.userImage);
            alertDialog = builder.create();
            alertDialog.show();
        });

    }

    Drawable tintDrawable(Drawable drawable, int color, Context context) {
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ResourcesCompat.getColor(context.getResources(), color, context.getTheme()));
        return drawable;
    }

    interface UserClickInterface {
        void OnClick(UserInfo info, MaterialButton connectBtn, MaterialButton btn, CircularProgressIndicator connectionSentProgress, int event, ConnectRequest connectRequest);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        SearchRecycleItemBinding binding;

        public ViewHolder(SearchRecycleItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}


class ItemDiffUtils extends DiffUtil.ItemCallback<UserInfo> {

    @Override
    public boolean areItemsTheSame(@NonNull UserInfo oldItem, @NonNull UserInfo newItem) {
        return oldItem.getEmail().equals(oldItem.getEmail());
    }

    @Override
    public boolean areContentsTheSame(@NonNull UserInfo oldItem, @NonNull UserInfo newItem) {
        return false;
    }
}

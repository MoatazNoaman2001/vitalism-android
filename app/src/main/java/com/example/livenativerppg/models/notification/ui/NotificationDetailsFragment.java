package com.example.livenativerppg.models.notification.ui;

import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.example.livenativerppg.R;
import com.example.livenativerppg.component.base.BaseFragment;
import com.example.livenativerppg.component.db.models.ConnectRequest;
import com.example.livenativerppg.component.db.models.ConnectionType;
import com.example.livenativerppg.component.db.models.Notification;
import com.example.livenativerppg.component.utility.Variables;
import com.example.livenativerppg.databinding.FragmentNotificationDetailsBinding;
import com.example.livenativerppg.models.notification.data.viewModel.NotificationVM;
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo;
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationDetailsFragment extends BaseFragment<FragmentNotificationDetailsBinding> {
    private static final String NOTIFICATION = "notification";
    private Notification notification;

    @Inject
    @Named("followers")
    CollectionReference myFollwersRef;

    public CollectionReference myRequestRef,partnerRequestRef, myConnectionRef, partnerConnectionRef;
    @Inject
    public RequestManager manager;
    public NotificationVM notificationVM;

    public static NotificationDetailsFragment newInstance(Notification notification) {
        Bundle args = new Bundle();
        NotificationDetailsFragment fragment = new NotificationDetailsFragment();
        args.putSerializable(NOTIFICATION, notification);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String TAG = "NotificationDetailsFrag";

    public NotificationDetailsFragment() {
        super(R.layout.fragment_notification_details);
    }

//    @Inject
//    public NotificationDetailsFragment(@MyRequestsRef CollectionReference myRequestRef, @MyConnectionsRef CollectionReference myConnectionRef) {
//        super(R.layout.fragment_notification_details);
//        this.myRequestRef = myRequestRef;
//        this.myConnectionRef = myConnectionRef;
//    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        notification = (Notification) requireArguments().getSerializable(NOTIFICATION);

        notificationVM = new ViewModelProvider(this).get(NotificationVM.class);

        myRequestRef = FirebaseFirestore.getInstance()
                .collection(Variables.FireStoreUsersRoot)
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(Variables.REQUEST);
        partnerRequestRef = FirebaseFirestore.getInstance()
                .collection(Variables.FireStoreUsersRoot)
                .document(notification.getConnRequest().getSenderId())
                .collection(Variables.REQUEST);
        myConnectionRef = FirebaseFirestore.getInstance()
                .collection(Variables.FireStoreUsersRoot)
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(Variables.CONNECTIONS);

        partnerConnectionRef = FirebaseFirestore.getInstance()
                .collection(Variables.FireStoreUsersRoot)
                .document(notification.getConnRequest().getSenderId())
                .collection(Variables.CONNECTIONS);


        ConnectRequest connectRequest = notification.getConnRequest();
        connectRequest.setReaded(true);

        partnerRequestRef.document(notification.getConnRequest().getReceiverId()).set(connectRequest);
        myRequestRef.document(notification.getConnRequest().getSenderId()).set(connectRequest).addOnSuccessListener(command -> {
            notificationVM.getNotification().observe(getViewLifecycleOwner(), listResource -> {
                listResource.getData().stream().filter(noti -> noti.getConnRequest().getRequestDate().equals(notification.getConnRequest().getRequestDate()) &&
                        noti.getConnRequest().getSenderId().equals(notification.getConnRequest().getSenderId())).findFirst().ifPresent(notification1 -> {
                    if (!notification1.getConnRequest().equals(connectRequest)) {
                        notification1.setConnRequest(connectRequest);
                        notificationVM.updateNotification(notification1);
                    }
                });
            });
        });

        Transition transition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move);
        setSharedElementEnterTransition(transition);


        FirebaseFirestore.getInstance()
                .collection(Variables.FireStoreUsersRoot)
                .document(notification.getConnRequest().getSenderId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserInfo info = documentSnapshot.toObject(UserInfo.class);
                        binding.UserName.setText(info.getName());
                        binding.UserEmail.setText(info.getEmail());
                        binding.Message.setText("hallo my name is "+ info.getName() + " iam " + ((info.getUserType().equals(UserType.Medical.name())? " medical user as " + info.getMedicine_specialty() : " as normal user")) + " if you like you could accept me");
                    }
                });

        binding.acceptBtn.setOnClickListener(v -> {
            if (connectRequest.getConnectType().equals(ConnectionType.FOLLOW.name())){
                AcceptFollow(connectRequest);
            }else {
                connectRequest.setAccpeted(true);
                myRequestRef.document(notification.getConnRequest().getSenderId()).set(connectRequest).addOnSuccessListener(command -> {
                    partnerRequestRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete();
                    myConnectionRef.document(connectRequest.getSenderId()).set(new HashMap<String, String>() {{
                        put(connectRequest.getSenderId(), "accepted");
                    }});
                    partnerConnectionRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(new HashMap<String, String>() {{
                        put(FirebaseAuth.getInstance().getCurrentUser().getUid(), "accepted");
                    }});
                    partnerRequestRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(connectRequest);

                    Log.d(TAG, "onInitialized: clouded updated");
                    notificationVM.getNotification().observe(getViewLifecycleOwner(), listResource -> {
                        listResource.getData().stream().filter(noti -> noti.getConnRequest().getRequestDate().equals(notification.getConnRequest().getRequestDate()) &&
                                noti.getConnRequest().getSenderId().equals(notification.getConnRequest().getSenderId())).findFirst().ifPresent(notification1 -> {
                            if (!notification1.getConnRequest().equals(connectRequest)) {
                                notification1.setConnRequest(connectRequest);
                                notificationVM.updateNotification(notification1);
                            }
                        });
                    });
                    notificationVM.updateNotification(notification);
                    binding.acceptBtn.setText("Accepted");
                    binding.acceptBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, requireActivity().getTheme()));
                    binding.rejectBtn.setVisibility(View.GONE);
                    binding.acceptBtn.setEnabled(false);
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "failed my be internet problem", Toast.LENGTH_SHORT).show();
                });
            }
        });
        binding.rejectBtn.setOnClickListener(v -> {
            if (connectRequest.getConnectType().equals(ConnectionType.FOLLOW.name())) {
                RejectFollow(connectRequest);
            }else {
                myRequestRef.document(notification.getConnRequest().getSenderId()).delete().addOnSuccessListener(command -> {
                    binding.rejectBtn.setText("rejected");
                    binding.rejectBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, requireActivity().getTheme()));
                    binding.acceptBtn.setVisibility(View.GONE);
                    binding.rejectBtn.setEnabled(false);
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "failed my be internet problem", Toast.LENGTH_SHORT).show();
                });
                myConnectionRef.document(notification.getConnRequest().getSenderId()).set(connectRequest);
            }
        });

        FirebaseStorage.getInstance()
                .getReference(Variables.FireStoreUsersRoot)
                .child(notification.getConnRequest().getSenderId())
                .child(Variables.UserProfilePic)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    manager.load(uri).circleCrop().into(binding.userImg);
                }).addOnFailureListener(e -> {
                    manager.load(R.drawable.img_person_blue).circleCrop().into(binding.userImg);
                });


    }


    void AcceptFollow(ConnectRequest request){
        request.setAccpeted(true);
        myFollwersRef.document(request.getSenderId()).set(new HashMap<String , Boolean>(){{put(request.getSenderId() , true);}});
        partnerRequestRef.document(request.getReceiverId()).set(request).addOnFailureListener(e -> {
            Log.d(TAG, "AcceptFollow: Failed to add you to partner follow list");
        });
        myRequestRef.document(request.getSenderId()).set(request).addOnSuccessListener(command -> {
            binding.acceptBtn.setText(R.string.accepted);
            binding.acceptBtn.setEnabled(false);
            binding.acceptBtn.setBackgroundColor(ResourcesCompat.getColor(getResources() , R.color.white , requireContext().getTheme()));
            binding.rejectBtn.setVisibility(View.GONE);
        });
    }

    void RejectFollow(ConnectRequest request ){
        binding.acceptBtn.setVisibility(View.GONE);
        myRequestRef.document(request.getSenderId()).delete();
        partnerRequestRef.document(request.getReceiverId()).delete();
        partnerRequestRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete().addOnSuccessListener(command -> {
            binding.rejectBtn.setText("rejected");
            binding.rejectBtn.setBackgroundColor(ResourcesCompat.getColor(getResources() , R.color.white , requireContext().getTheme()));
        });
    }
    @Override
    public void addObservers() {
        super.addObservers();
    }

    @Override
    public void setUpClicks() {

    }
}
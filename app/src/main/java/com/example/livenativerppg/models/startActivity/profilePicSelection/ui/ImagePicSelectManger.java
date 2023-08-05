package com.example.livenativerppg.models.startActivity.profilePicSelection.ui;

import android.net.Uri;

import com.example.livenativerppg.component.utility.Variables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.tasks.Task;

public class ImagePicSelectManger {
    private String currentImage;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private UploadTask task;

    public ImagePicSelectManger() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }


    public UploadTask UploadImage(Uri imageuri) {
        if (currentImage == null) {
            currentImage = imageuri.toString();
            task = FirebaseStorage.getInstance().getReference(Variables.FireStoreUsersRoot)
                    .child(user.getUid())
                    .putFile(imageuri);
            return task;
        } else {
            return null;
        }
    }

    public UploadTask UploadImage(Uri imageuri, byte[] bytes) {
        if (currentImage == null) {
            currentImage = imageuri.toString();
            task = FirebaseStorage.getInstance().getReference(Variables.FireStoreUsersRoot)
                    .child(user.getUid())
                    .child(Variables.UserProfilePic)
                    .putBytes(bytes);
            task.addOnCompleteListener(command -> {
                currentImage = null;
            });
            return task;
        } else {
            return null;
        }
    }

    public Task<Uri> getDownloadUri(){
        if (currentImage != null){
            return FirebaseStorage.getInstance().getReference(Variables.FireStoreUsersRoot)
                    .child(user.getUid())
                    .child(Variables.UserProfilePic)
                    .getDownloadUrl();
        }else {
            return null;
        }
    }

    public boolean isFinished() {
        return currentImage == null;
    }


    public void stopUpload() {
        task.pause();
    }

    public void presideUpload() {
        task.resume();
    }
}

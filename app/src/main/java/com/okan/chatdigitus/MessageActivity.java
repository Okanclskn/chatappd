package com.okan.chatdigitus;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.okan.chatdigitus.Adapter.MessageAdapter;
import com.okan.chatdigitus.Model.Chat;
import com.okan.chatdigitus.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    CircleImageView profile_picture;
    TextView username;
    Uri fileUri;
    FirebaseUser fuser;
    DatabaseReference reference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    String checker="";
    Intent intent;
    private final int PICK_FILE_REQUEST = 438;
    ImageButton button_send;
    ImageButton button_selectFile;
    EditText text_send;
    MessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;
    String userid;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_picture = findViewById(R.id.profile_picture);
        username = findViewById(R.id.username);
        button_send = findViewById(R.id.button_send);
        button_selectFile = findViewById(R.id.button_selectFile);
        text_send = findViewById(R.id.text_send);


        intent = getIntent();

        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();







        button_send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String msg = text_send.getText().toString();


                if (!msg.equals("")){
                    sendMessage(fuser.getUid(), userid, msg);
                }
                text_send.setText("");
            }
        });



        button_selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                        "Images",
                        "PDF Files",
                        "Video"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                builder.setTitle("Dosya Seç");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
                            if (which == 0) {
                                checker = "image";

                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(intent.createChooser(intent, "resmi seçin.."), PICK_FILE_REQUEST);
                            }
                            if (which == 1) {
                                checker = "PDF";

                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("application/pdf");
                                startActivityForResult(intent.createChooser(intent, "PDF seçin.."), PICK_FILE_REQUEST);


                            }
                            if (which == 2) {
                                checker = "video";

                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("video/*");
                                startActivityForResult(intent.createChooser(intent, "video seçin.."), PICK_FILE_REQUEST);


                            }
                        }
                        else{
                            ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.
                                    READ_EXTERNAL_STORAGE},9);
                        }
                    }
                });
                builder.show();
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("Files");
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    profile_picture.setImageResource(R.mipmap.profile_picdefault);
                }
                else{
                    Glide.with(MessageActivity.this).load(user.getImageURL()).into(profile_picture);
                }
                readMesagges(fuser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData()!= null){
            fileUri=data.getData();

            uploadFile(fuser.getUid(),userid,fileUri);

        }
        else{
            Toast.makeText(MessageActivity.this,"Dosya seçin",Toast.LENGTH_SHORT).show();
        }
    }





    private void uploadFile(final String send, final String receive, final Uri file) {

        progressDialog = new ProgressDialog(MessageActivity.this);

        progressDialog.setTitle("Lütfen bekleyiniz..");
        progressDialog.show();

        final String filename = System.currentTimeMillis()+"";

        final StorageReference sRef = storageReference.child(filename);
        sRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                Uri download_url = uri;

                                DatabaseReference dRef = FirebaseDatabase.getInstance().getReference();

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("sender",send);
                                hashMap.put("receiver",receive);
                                hashMap.put("message",String.valueOf(download_url));
                                hashMap.put("type",checker);


                                dRef.child("Chats").push().setValue(hashMap);

                                final DatabaseReference cRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                                        .child(fuser.getUid())
                                        .child(userid);
                                progressDialog.dismiss();
                                Toast.makeText(getBaseContext(), "Dosya gönderildi.." , Toast.LENGTH_SHORT).show();
                                dRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()){
                                            cRef.child("id").setValue(userid);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                cRef.child(filename).setValue(String.valueOf(download_url)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()){

                                        }
                                    }
                                });



                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MessageActivity.this,"dosya yüklenemedi",Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress = (int)(100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());                 progressDialog.setMessage("Gönderiliyor..." +"  "+ currentProgress + "%");
            }

        });


    }


    private void sendMessage(String sender, String receiver, String message){

        reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("type","text");
        reference.child("Chats").push().setValue(hashMap);



        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void readMesagges(final String myid, final String userid, final String imageurl){
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
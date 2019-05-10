package com.okan.chatdigitus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.jar.Attributes;

public class Register extends AppCompatActivity implements View.OnClickListener {
    private DatabaseReference reference;
    private FirebaseAuth firebaseAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextName;
    private EditText editTextSurName;
    private EditText editTextPasswordre;
    private Button buttonSignup;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        firebaseAuth = FirebaseAuth.getInstance();
        editTextEmail = (EditText) findViewById(R.id.rE_Mail);
        editTextPassword = (EditText) findViewById(R.id.rPassword);
        editTextPasswordre = (EditText) findViewById(R.id.rPassworda);
        editTextName = (EditText) findViewById(R.id.Name);
        editTextSurName = (EditText) findViewById(R.id.SurName);
        buttonSignup = (Button) findViewById(R.id.SignUp);
        progressDialog = new ProgressDialog(this);

        buttonSignup.setOnClickListener(this);

    }


    private void registerUser(){
        final String email = editTextEmail.getText().toString().trim();
        final String password  = editTextPassword.getText().toString().trim();
        final String passwordre = editTextPasswordre.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String surname = editTextSurName.getText().toString().trim();


        if(TextUtils.isEmpty(email)|| TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || TextUtils.isEmpty(surname)){
            Toast.makeText(this,"Lütfen tüm boş alanları doldurun..",Toast.LENGTH_LONG).show();
            return;
        }

        if(password.length()<6){
            Toast.makeText(this,"Şifreniz 6 karakterden küçük olamaz..",Toast.LENGTH_LONG).show();
            return;
        }
        if(!password.equals(passwordre)) {
            Toast.makeText(this, "Şifre tekrarı uyuşmuyor", Toast.LENGTH_LONG).show();
            return;
        }



        progressDialog.setMessage("Lütfen Bekleyiniz..");
        progressDialog.show();



        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            String userid=firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String,String> hashmap = new HashMap<>();
                            hashmap.put("id",userid);
                            hashmap.put("name",name);
                            hashmap.put("surName",surname);
                            hashmap.put("e-mail",email);
                            hashmap.put("password",password);
                            hashmap.put("imageURL","default");
                            hashmap.put("username",name +"  "+ surname);

                            reference.setValue(hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(Register.this, "Kayıt Başarılı..", Toast.LENGTH_LONG).show();
                                        Intent goIntent = new Intent(Register.this, LoginActivity.class);
                                        startActivity(goIntent);
                                    }
                                }
                            });

                        }
                        else{
                            Toast.makeText(Register.this,"Lütfen tüm bilgileri kontrol ediniz..",Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onClick(View v) {

        registerUser();
    }
}
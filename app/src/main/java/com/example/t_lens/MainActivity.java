package com.example.t_lens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView mainimg;
     Button upload_photo, click_photo, getsearchresults;
     TextView textView2;
     ImageLabeler labeler;
     InputImage inputImage;

    String highestImageLabel;
    Bitmap photo2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        textView2 = findViewById(R.id.textView2);
        mainimg = findViewById(R.id.mainimg);
        upload_photo = findViewById(R.id.upload_photo);
        click_photo = findViewById(R.id.click_photo);
        getsearchresults = findViewById(R.id.getsearchresults);

        upload_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        click_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraCapture();
            }
        });

        getsearchresults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSearchResults();
            }
        });

//        textView2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

    }

    void imageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 100);
    }

    void cameraCapture(){
        Intent camera_intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, 200);
    }

    void getSearchResults(){
        inputImage = InputImage.fromBitmap(photo2, 0);
        labeler.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(List<ImageLabel> imageLabels) {
//                        String textLabels = "";
                        ArrayList<Integer> imageLabelConfidenceArray = new ArrayList<Integer>();
                        ArrayList<String> imageLabelsArray = new ArrayList<String>();

//
                        for (ImageLabel imageLabel : imageLabels){
                            imageLabelConfidenceArray.add(Math.round(imageLabel.getConfidence()*100));
                            imageLabelsArray.add(imageLabel.getText());
                        }
                        int max_confidence = Collections.max(imageLabelConfidenceArray);
//
                        textView2.setText("");
                        for (int i=0; i<imageLabelsArray.size(); i++) {
                            String imgLabelStr = imageLabelsArray.get(i);
//                            String textView2Data = "";

                            textView2.setText(textView2.getText() + "\n" + imgLabelStr + " (Probability : " + Math.round(imageLabelConfidenceArray.get(i))+"%)");

                            if (imageLabelConfidenceArray.get(i)==max_confidence) {
                                textView2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                        intent.putExtra(SearchManager.QUERY, imgLabelStr);
                                        startActivity(intent);
                                        Toast.makeText(MainActivity.this, "Searching for highest probability image label !", Toast.LENGTH_SHORT).show();
                                    }

                                });
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Failed to get search results !", Toast.LENGTH_SHORT).show();
                            }
                        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {
            if (requestCode == 100) {
                assert data != null;
                Uri selectedImageUri = data.getData();
                if (selectedImageUri!=null){
                    mainimg.setImageURI(selectedImageUri);
                    Bitmap photo;
                    try {
                        photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        photo2 = photo;
                        getsearchresults.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            else if (requestCode == 200){
                assert data!=null;
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                photo2 = photo;
                mainimg.setImageBitmap(photo);
                getsearchresults.setVisibility(View.VISIBLE);

            }

        }
    }
}
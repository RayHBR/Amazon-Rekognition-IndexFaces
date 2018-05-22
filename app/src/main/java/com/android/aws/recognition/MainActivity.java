package com.android.aws.recognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.Attribute;
import com.amazonaws.services.rekognition.model.DetectFacesRequest;
import com.amazonaws.services.rekognition.model.DetectFacesResult;
import com.amazonaws.services.rekognition.model.FaceRecord;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    AmazonRekognitionClient amazonRekognitionClient;
    Image RecognitionImage = new Image();
    DetectFacesRequest detectFaceRequest;
    DetectFacesResult detectFaceResult;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        tv = findViewById(R.id.textView);

        setSupportActionBar(toolbar);
        new AsyncTaskRunner().execute();
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

    public void getFaceDetectResults() {

        try {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jobs);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
            ByteBuffer imageBytes = ByteBuffer.wrap(stream.toByteArray());
            RecognitionImage.withBytes(imageBytes);

            //Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "aws-rekognition-key", // Please update Identity Pool ID before you run the applicationgit p
                    Regions.US_EAST_1 // Region
            );

            //I want "ALL" attributes
            amazonRekognitionClient = new AmazonRekognitionClient(credentialsProvider);
            detectFaceRequest = new DetectFacesRequest()
                    .withAttributes(Attribute.ALL.toString())
                    .withImage(RecognitionImage);
            detectFaceResult = amazonRekognitionClient.detectFaces(detectFaceRequest);

            IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
                    .withImage(RecognitionImage)
                    .withDetectionAttributes(Attribute.ALL.toString())
                    .withCollectionId("ALL");


            IndexFacesResult indexFacesResult = amazonRekognitionClient.indexFaces(indexFacesRequest);
            List<FaceRecord> faceRecords = indexFacesResult.getFaceRecords();
            tv.setText(faceRecords.toString());

        } catch (Exception ex) {
            Log.e("Error on something:", "Message:" + ex.getMessage());
        }
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            getFaceDetectResults();
            return null;
        }
    }
}

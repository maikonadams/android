package com.example.maikon.camedgesv2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    File image;

    Bitmap dest;
    Bitmap bitmap;

    int[][] kernel ={
            {0, -1, 0},
            {-1, 4, -1},
            {0, -1, 0}
    };

    final static int KERNAL_WIDTH = 3;
    final static int KERNAL_HEIGHT = 3;

    private void galleryAddPic() {

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);


        this.sendBroadcast(mediaScanIntent);
    }

    private void animation1()
    {
        BitmapDrawable frame1 = new BitmapDrawable(getResources(),bitmap);
        BitmapDrawable frame2 = new BitmapDrawable(getResources(),dest);

        int reasonableDuration = 350;
        AnimationDrawable mAnimation = new AnimationDrawable();
        mAnimation.addFrame(frame1, reasonableDuration);
        mAnimation.addFrame(frame2, reasonableDuration);

        imageView.setBackgroundDrawable(mAnimation);
        mAnimation.setOneShot(false);
        mAnimation.start();

    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));

                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            galleryAddPic();

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

           bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bitmapOptions);

          //  imageView.setImageBitmap(bitmap);


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        final Button buttonEffect1 = (Button) findViewById(R.id.button1);
        buttonEffect1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             dest=  processingBitmap(bitmap,kernel);
                imageView.setImageBitmap(dest);
            }
        });


        final Button buttonEffect2 = (Button) findViewById(R.id.button2);
        buttonEffect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dest=  processingBitmap(bitmap,kernel);
                animation1();

            }
        });

    }

    private Bitmap processingBitmap(Bitmap src, int[][] knl){
        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(), src.getHeight(), src.getConfig());

        int bmWidth = src.getWidth();
        int bmHeight = src.getHeight();
        int bmWidth_MINUS_2 = bmWidth - 2;
        int bmHeight_MINUS_2 = bmHeight - 2;

        for(int i = 1; i <= bmWidth_MINUS_2; i++){
            for(int j = 1; j <= bmHeight_MINUS_2; j++){

                //get the surround 3*3 pixel of current src[i][j] into a matrix subSrc[][]
                int[][] subSrc = new int[KERNAL_WIDTH][KERNAL_HEIGHT];
                for(int k = 0; k < KERNAL_WIDTH; k++){
                    for(int l = 0; l < KERNAL_HEIGHT; l++){
                        subSrc[k][l] = src.getPixel(i-1+k, j-1+l);
                    }
                }

                //subSum = subSrc[][] * knl[][]
                int subSumA = 0;
                int subSumR = 0;
                int subSumG = 0;
                int subSumB = 0;

                for(int k = 0; k < KERNAL_WIDTH; k++){
                    for(int l = 0; l < KERNAL_HEIGHT; l++){
                        subSumR += Color.red(subSrc[k][l]) * knl[k][l];
                        subSumG += Color.green(subSrc[k][l]) * knl[k][l];
                        subSumB += Color.blue(subSrc[k][l]) * knl[k][l];
                    }
                }

                subSumA = Color.alpha(src.getPixel(i, j));

                if(subSumR <0){
                    subSumR = 0;
                   // subSumR =  Color.red(src.getPixel(i, j));
                }else if(subSumR > 255){
                    subSumR = 255;
                }

                if(subSumG <0){
                     subSumG = 0;
                    //subSumG =  Color.green(src.getPixel(i, j));
                }else if(subSumG > 255){
                    subSumG = 255;
                }

                if(subSumB <0){
                    subSumB = 0;
                   // subSumB =  Color.blue(src.getPixel(i, j));
                }else if(subSumB > 255){
                    subSumB = 255;
                }

                /*if (subSumR != 0)
                {
                    subSumR =  Color.red(src.getPixel(i, j));

                }

                if (subSumG != 0)
                {
                    subSumG =  Color.green(src.getPixel(i, j));

                }

                if (subSumB != 0)
                {
                    subSumB =  Color.blue(src.getPixel(i, j));
                }*/


                dest.setPixel(i, j, Color.argb(
                        subSumA,
                        subSumR,
                        subSumG,
                        subSumB));
            }
        }

        return dest;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}

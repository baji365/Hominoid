package in.specialsoft.hominoid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class UploadImageActivity extends AppCompatActivity {

    ProgressBar progressBar;
    ImageView imageView2,imageView;
    TextInputEditText et_title;

    private String userChoosenTask;
    private Bitmap bm;
    String base64String = "";
    byte[] bb = null;
    String img2,img;
    String type ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);
        et_title = findViewById(R.id.et_title);
    }

    public void uploadImageData(View view) {
        if (validateFields()){
                //Send Details to php API

            Toast.makeText(this, "all Fields are available", Toast.LENGTH_SHORT).show();
            sendWallDataToAPI(img2,img,et_title.getText().toString().trim());
        }
        else {
            Toast.makeText(this, "All fields are require !", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateFields() {
        if (!img2.trim().equals("") && !img.trim().equals("") && !et_title.getText().toString().trim().equals("")) {
            return true;
        }
        return false;
    }

    public void selectThumbnail(View view) {
        type = "thumbnail";
        openOptionDialogue();

    }

    public void selectImage(View view) {
        type = "image";
        openOptionDialogue();
    }

    //Image Selection Methods
    private void openOptionDialogue() {
        final CharSequence[] items = {"Choose from Library","Cancle"};
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadImageActivity.this);
        builder.setTitle("Add Photo !");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("Choose from Library")){
                    userChoosenTask = "Choose from Library";
                    galleryIntent();
                }
                else if (items[which].equals("Cancle")){
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK){
            if (requestCode == 1){
                onSelectFromGalleryResult(data,1);
            }
        }
    }

    private void onSelectFromGalleryResult(Intent data, int i){
        bm = null;
        if (data != null){
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                bm = BitmapFactory.decodeStream(imageStream);
                bm = getResizedBitmap(bm,400);
                //-------------------------------------------------------------------------------
                if (type.equals("thumbnail")){
                    imageView2.setImageBitmap(bm);
                    img2 = getBase64(bm);
                }
                else {
                    imageView.setImageBitmap(bm);
                    img = getBase64(bm);
                }

            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }
        getBase64(bm);
    }

    private Bitmap getResizedBitmap(Bitmap image,int maxSize){
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0){
            width = maxSize;
            height = (int) (width/bitmapRatio);
        }else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    private String getBase64(Bitmap bm1) {
        if (bm1 != null){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bm1.compress(Bitmap.CompressFormat.JPEG,50,bos);

            bb = bos.toByteArray();
            base64String = Base64.encodeToString(bb,Base64.DEFAULT);
        }
        Log.i("Image : ",base64String);
        return base64String;
    }
    //Send data to PHP API
    private void sendWallDataToAPI(final String img2, final String img, final String trim) {
        progressBar.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                //Starting Write and Read data with URL
                //Creating array for parameters
                String[] field = new String[3];
                field[0] = "title";
                field[1] = "thumbnail";
                field[2] = "image";
                //Creating array for data
                String[] data = new String[3];
                data[0] = trim;
                data[1] = img2;
                data[2] = img;
                PutData putData = new PutData("http://hominoid.atwebpages.com/postHandler.php", "POST", field, data);
                if (putData.startPut()) {
                    if (putData.onComplete()) {
                        String result = putData.getResult();
                        //End ProgressBar (Set visibility to GONE)
                        if (result.equals("Wall upload Success"))
                        {
                            progressBar.setVisibility(View.INVISIBLE);
                            showSnackMessage("Wall upload Success");
                        }
                        else
                        {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(UploadImageActivity.this, "title already exist : wall upload Failed", Toast.LENGTH_SHORT).show();
                        }
                        // Log.i("PutData", result);
                    }
                }
                //End Write and Read data with URL
            }
        });
    }

    //Snackbar
    private void showSnackMessage(String snackDisplay) {
        Snackbar.make(findViewById(R.id.rl_img_upload),""+snackDisplay,Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                }).show();
    }
}
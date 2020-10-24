package in.specialsoft.hominoid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ViewWallpaper extends AppCompatActivity {

    ImageView imageFull,downloadImage;
    String title,image,imageFileName;
    DisplayMetrics displayMetrics;
    BitmapDrawable bitmapDrawable;
    Bitmap bitmap;
    WallpaperManager wallpaperManager;
    Button setLockScreen,setHomeScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wallpaper);
        //SetActivity to fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //getData from previous intent
        title = getIntent().getStringExtra("title");
        image = getIntent().getStringExtra("image");
        //find view by ID
        imageFull = findViewById(R.id.imageFull);
        downloadImage = findViewById(R.id.downloadImage);
        setLockScreen = findViewById(R.id.setLockScreen);
        setHomeScreen = findViewById(R.id.setHomeScreen);

        //Load Actual image using Glide - add request listener to it - add errorPlaeHolder
        Glide.with(this).load(image).centerCrop()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                //SetOnclicks on buttons to dowload and image
                                //OnClicks to buttons
                                setLockScreen.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                            setWallpaper("Lock");
                                    }
                                });

                                setHomeScreen.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        setWallpaper("Home");
                                    }
                                });
                                //OnClicks to Image
                                downloadImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //ask permission to write to expernal storage
                                        //Dexter Runtime permissions
                                        Dexter.withContext(getApplicationContext())
                                                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                .withListener(new PermissionListener() {
                                                    @Override
                                                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                                        downloadFromImageView();
                                                    }

                                                    @Override
                                                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                                        Toast.makeText(ViewWallpaper.this, "Permission is require to download image", Toast.LENGTH_SHORT).show();
                                                    }

                                                    @Override
                                                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                                                    }
                                                }).check();
                                    }
                                });
                                return false;
                            }
                        })
                    .error(R.drawable.no_image).placeholder(R.drawable.ic_loading)
                     .into(imageFull);
    }

    private void downloadFromImageView(){
        imageFileName = image.substring(image.lastIndexOf("/") + 1);

        FileOutputStream fileOutputStream;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Hominoid");
        if (!file.exists() && !file.mkdir()){
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
        else {
            File filename = new File(file.getAbsolutePath()+"/"+imageFileName);
            int[] size = getScreenSize();
            try {
                 fileOutputStream = new FileOutputStream(filename);
                 bitmapDrawable = (BitmapDrawable) imageFull.getDrawable();
                 bitmap = bitmapDrawable.getBitmap();
                 bitmap = Bitmap.createScaledBitmap(bitmap,size[0],size[1],false);
                 bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                Toast.makeText(this, "Saved TO galary "+filename, Toast.LENGTH_SHORT).show();
                fileOutputStream.flush();
                fileOutputStream.close();
            }catch (IOException e){e.printStackTrace();}
            refreshGallery(file);
        }
    }

    private void refreshGallery(File file){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }
    private void setWallpaper(String type){
        int[] size = getScreenSize();
        wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        bitmapDrawable = (BitmapDrawable) imageFull.getDrawable();
        bitmap = bitmapDrawable.getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap,size[0],size[1],false);
        //set lock screen and homescreen wallpapers
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                if (type.equals("Lock")){
                    wallpaperManager.setBitmap(bitmap,null,true,wallpaperManager.FLAG_LOCK);
                    Toast.makeText(this, "Applyed to lock screen", Toast.LENGTH_SHORT).show();
                }else {
                    wallpaperManager.setBitmap(bitmap,null,true,wallpaperManager.FLAG_SYSTEM);
                    Toast.makeText(this, "Applyed to HOME screen", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                wallpaperManager.setBitmap(bitmap);
                Toast.makeText(this, "wallpaper Applyed to screen", Toast.LENGTH_SHORT).show();
            }
            wallpaperManager.suggestDesiredDimensions(size[0],size[1]);
        }catch (Exception e){ e.printStackTrace();}
    }
    //get height and width of screen
    private int[] getScreenSize(){
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int[] size = new int[2];
        size[0] = displayMetrics.widthPixels;
        size[1] = displayMetrics.heightPixels;
        return size;
    }
}
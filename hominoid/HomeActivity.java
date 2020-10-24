package in.specialsoft.hominoid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void toWallPapers(View view) {
        Intent intent = new Intent(HomeActivity.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }

    public void toUploadWall(View view) {
        Intent intent = new Intent(HomeActivity.this,UploadImageActivity.class);
        startActivity(intent);
    }
}
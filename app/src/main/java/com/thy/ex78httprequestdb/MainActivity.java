package com.thy.ex78httprequestdb;

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    EditText editName, editMsg;
    ImageView iv;

    String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editName = findViewById(R.id.edit_name);
        editMsg = findViewById(R.id.edit_msg);

        iv = findViewById(R.id.iv);

        //외부저장소 읽고쓰기 권한 퍼미션 체크 및 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                //허용이 안되어 있는 상태이므로 퍼미션요청 다이얼로그 보이기
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 100:
                if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(this, "이미지 선택이 불가능합니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void clickImg(View v){
        //Gallery앱을 실행해서 업로드할 이미지를 선택
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 10);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 10:
                if(resultCode == RESULT_OK){
                    //선택된 이미지의 URI를 얻어오기
                    Uri uri = data.getData();
                    if(uri != null){
                        Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
                        iv.setImageURI(uri);

                        //얻어온 파일의 Uri는 Gallery앱의 DB번호임.
                        //업로드를 하려면 이미지의 절대경로가 필요함.
                        //Uri --> 절대경로(String)로 변환.
                        imgPath = getRealPathFromUri(uri);
                        Toast.makeText(this, ""+imgPath, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //Uri --> 절대경로로 바꿔서 리턴시켜주는 메소드
    public String getRealPathFromUri(Uri uri){
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;

    }

    public void clickUpload(View v){

        String serverUrl = "http://thyun85.dothome.co.kr/android/insertDB.php";

        String name = editName.getText().toString();
        String msg = editMsg.getText().toString();

        //insertDB.php에 보낼 파일전송요청 객체 생성
        SimpleMultiPartRequest multiPartRequest = new SimpleMultiPartRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //insertDB.php의 echo 결과 보여주기
                new AlertDialog.Builder(MainActivity.this).setMessage(response).setPositiveButton("OK", null).create().show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //요청객체에 데이터 추가하기
        multiPartRequest.addStringParam("name", name);
        multiPartRequest.addStringParam("msg", msg);
        if(imgPath != null){
            multiPartRequest.addFile("upload", imgPath);
        }

        //요청큐 객체 생성하기
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //요청큐에 요청객체 추가..
        requestQueue.add(multiPartRequest);

    }

    public void clickLoad(View v){
        //서버의 데이터베이스 게시글 보여주기
        startActivity(new Intent(this, TalkActivity.class));

    }
}

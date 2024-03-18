package misterdneh.ca.appsigner;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ApkSigningHelper signingHelper;
    public InputStream testkey;
    public String alias = "test";
    public String masterpass = "test";
    public String keypass = "test";
    public String getsdcardpath;
    public String inputpath;
    public String outputpath;
    public Uri inputfileuri = null;
    public Uri outputfileuri = null;
    private SharedPreferences data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textview);
        data = getSharedPreferences("data", Activity.MODE_PRIVATE);

        try {
            testkey = getAssets().open("testkey.jks");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        getsdcardpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        //textView.setText(getsdcardpath);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               pickFilePath("*/*", 44);
            }
        });
    }

    private void pickFilePath(final String type, final int requestcode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(type);
        startActivityForResult(intent,requestcode);
    }

    public void trytosignapp(){
        signingHelper = new ApkSigningHelper(
                testkey,
                alias,
                masterpass,
                keypass,inputfileuri,outputfileuri);
        signingHelper.sign();
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((resultCode == RESULT_OK) && (requestCode == 44)){
            //Then do something
            if (data!=null) {
                inputfileuri = data.getData();
                textView.setText(getFilePathFromContentUri(this,inputfileuri));
            }else {
                textView.setText("ERROR");
            }

        }
    }
    public String getFilePathFromContentUri(Context context, Uri contentUri) {
        ContentResolver contentResolver = context.getContentResolver();
        String fileName = getFileName(contentResolver, contentUri);
        if (fileName != null) {
            File filePath = new File(context.getFilesDir(), fileName);
            try {
                InputStream inputStream = contentResolver.openInputStream(contentUri);
                if (inputStream != null) {
                    FileOutputStream outputStream = new FileOutputStream(filePath);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        try {
                            outputStream.write(buffer, 0, length);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    outputStream.close();
                    inputStream.close();
                    return filePath.getAbsolutePath();
                }
            } catch (IOException ignored) {

            }
        }
        return null;
    }

    private String getFileName(ContentResolver contentResolver, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, null, null,
                    null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public void copyUriPaths(Uri pathA,Uri pathB,String filename){
        InputStream in = null;
        OutputStream out = null;
        String error = null;
        DocumentFile pickedDir = DocumentFile.fromTreeUri(getApplicationContext(), pathB);
        String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        try {
            DocumentFile newFile = pickedDir.createFile("application/" + extension, filename);
            out = getContentResolver().openOutputStream(newFile.getUri());
            in = getContentResolver().openInputStream(pathA);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            error = e.getMessage();
        }
    }
    public void checkdirectoryScopedStorage(String directoryName){
        try {
            Uri filePath = Uri.parse(data.getString(directoryName, ""));
            DocumentFile dfile = DocumentFile.fromTreeUri(this, filePath);
            if (!dfile.canRead() || !dfile.canWrite()) {
                _requestFilepathPermission();
            }
            else {
                filePath = Uri.parse(data.getString(directoryName, ""));
                if (_getDirectoriesFromUri(filePath, directoriesListmap)) {
                    int start = data.getString(_directory, "").indexOf("/tree");
                    storagealocation.setText(data.getString(_directory, "").replace("%20", " ").replace("%3A", "/").replace("%2F", "/").substring(start));
                    listview1.setAdapter(new Listview1Adapter(directoriesListmap));
                }
            }
        } catch (Exception e) {
            _requestFilepathPermission();
        }
    }

}
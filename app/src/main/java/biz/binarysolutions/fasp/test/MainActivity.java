package biz.binarysolutions.fasp.test;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 0;

    private void copyFileToInternalFolder() {

        try {
            InputStream from = getAssets().open("demo.pdf");
            Path        to   = new File(getFilesDir(), "demo.pdf").toPath();

            Files.copy(from, to);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileName(ContentResolver resolver, Uri uri) {

        Cursor cursor = resolver.query(uri, null, null, null, null);

        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();

        String fileName = cursor.getString(nameIndex);
        cursor.close();

        return fileName;
    }

    private void saveUriContent(Uri uri) {

        ContentResolver resolver = getContentResolver();

        try {

            FileInputStream input = new FileInputStream(
                resolver.openFileDescriptor(uri, "r").getFileDescriptor());

            String directory = getFilesDir().getAbsolutePath();
            String fileName  = getFileName(resolver, uri);
            Path   path      = Paths.get(directory, fileName);

            Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING);
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        copyFileToInternalFolder();
    }

    public void onButtonClick(View view) {

        String authority = getString(R.string.app_file_provider);
        File   file      = new File(getFilesDir(), "demo.pdf");
        Uri    uri       = FileProvider.getUriForFile(this, authority, file);

        Intent intent = new Intent("biz.binarysolutions.fasp.FILL_AND_SIGN");
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("biz.binarysolutions.fasp.ReturnToCaller", true);
        intent.putExtra("biz.binarysolutions.fasp.ActivationCode", "test2222");

        intent.putExtra("biz.binarysolutions.fasp.acrofield.Text1", "sample field value 1");
        intent.putExtra("biz.binarysolutions.fasp.acrofield.Text2", "sample field value 2");

        intent.putExtra("biz.binarysolutions.fasp.ExportToJSON", true);

        intent.setComponent(
            new ComponentName(
                "biz.binarysolutions.fasp",
                "biz.binarysolutions.fasp.Fill"
            )
        );

        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent intent) {
        super.onActivityResult(request, result, intent);

        if (request != REQUEST_CODE || result != RESULT_OK || intent == null) {
            return;
        }

        String key = "biz.binarysolutions.fasp.PDFOutput";
        Uri    uri = intent.getParcelableExtra(key);

        saveUriContent(uri);

        key = "biz.binarysolutions.fasp.JSONExport";
        uri = intent.getParcelableExtra(key);

        saveUriContent(uri);
    }
}
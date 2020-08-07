package com.meyadamx.qrbarcodescanner;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission_group.CAMERA;


public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int CAMERA_REQUEST_CODE = 1;
    private ZXingScannerView scannerView;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                requestPermission();
            }
        }

    }

    private boolean checkPermission() {

        boolean permission = ContextCompat.checkSelfPermission
                (this , Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        return permission;
    }


    private void requestPermission()
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.CAMERA} , CAMERA_REQUEST_CODE);
       // }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:
            {
                if(grantResults.length > 0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted)
                    {
                      //  Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                        snackbar = Snackbar.make(scannerView , "Permission Granted" ,Snackbar.LENGTH_LONG);
                                snackbar.show();
                    }
                    else
                    {

                        snackbar = Snackbar
                                .make(scannerView, "Permission Denied Unable to use QR Barcode Scanner...", Snackbar.LENGTH_LONG);
                        snackbar.show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(ActivityCompat.shouldShowRequestPermissionRationale(this ,Manifest.permission.CAMERA))
                            {
                                displayAlertDialog("You need to allow for both permission ",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }


                    }
                }
                break;
            }
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if (scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }
       }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    private void displayAlertDialog(String message , DialogInterface.OnClickListener  listener)
    {
     AlertDialog.Builder builder =  new AlertDialog.Builder(this)
               .setMessage(message)
               .setPositiveButton("Ok", listener)
               .setNegativeButton("cancel" , null);

       AlertDialog dialog = builder.create();
              dialog.show();
    }

    @Override
    public void handleResult(Result result) {

        final String scanResult = result.getText();
        AlertDialog.Builder alert = new AlertDialog.Builder(this)
                .setTitle("Scanner Result")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        scannerView.resumeCameraPreview(MainActivity.this);
                    }
                });
        alert.setNeutralButton("Visit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse(scanResult));
                startActivity(intent);

            }
        });

        alert.setMessage(scanResult);
        AlertDialog dialog = alert.create();
        dialog.show();
    }
}

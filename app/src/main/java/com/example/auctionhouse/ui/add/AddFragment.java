package com.example.auctionhouse.ui.add;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.auctionhouse.Prevalent.Prevalent;
import com.example.auctionhouse.R;
import com.example.auctionhouse.ui.home.HomeFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mynameismidori.currencypicker.CurrencyPicker;
import com.mynameismidori.currencypicker.CurrencyPickerListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;


public class AddFragment extends Fragment {

    private Button mCaptureButton;
    private ImageView mImageView;
    private String name, price, description, dateUntilUp, timeUntilUp, currDate, currTime;
    private String downloadImageUrl, currentPhotoPath, imageFileName;
    private TextView mDescription, mName, mPrice, mDateUntilUp, mTimeUntilUp, chooseCurrency;
    private StorageReference productImageRef;
    private DatabaseReference productDbRef, productUsersRef;
    private Uri imageUri;
    private ProgressDialog loadingBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_add, container, false);

        root.setBackgroundColor(getResources().getColor(R.color.colorFragments));

        loadingBar = new ProgressDialog(AddFragment.this.getContext());

        productImageRef = FirebaseStorage.getInstance().getReference().child("Product Image");
        productDbRef = FirebaseDatabase.getInstance().getReference().child("Products");
        productUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mImageView = root.findViewById(R.id.imageView);
        mCaptureButton = root.findViewById(R.id.button_take_picture);
        Button mDoneButton = root.findViewById(R.id.button5);

        chooseCurrency = root.findViewById(R.id.textView8);
        chooseCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CurrencyPicker picker = CurrencyPicker.newInstance("Select Currency");  // dialog title
                picker.setListener(new CurrencyPickerListener() {
                    @Override
                    public void onSelectCurrency(String name, String code, String symbol, int flagDrawableResID) {
                        chooseCurrency.setText(symbol);
                        picker.dismiss();
                    }
                });
                picker.show(getFragmentManager(), "CURRENCY_PICKER");
            }
        });

        mDescription = root.findViewById(R.id.editTextTextMultiLine);
        mName = root.findViewById(R.id.editTextTextPersonName4);
        mPrice = root.findViewById(R.id.editTextNumberDecimal);

        mDateUntilUp = root.findViewById(R.id.editTextDate);
        mDateUntilUp.setInputType(InputType.TYPE_NULL);
        mDateUntilUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                DatePickerDialog picker = new DatePickerDialog(root.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                                mDateUntilUp.setText(i + "/" + (i1 + 1) + "/" + i2);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        mTimeUntilUp = root.findViewById(R.id.editTextTime);
        mTimeUntilUp.setInputType(InputType.TYPE_NULL);
        mTimeUntilUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar time = Calendar.getInstance();
                int hour = time.get(Calendar.HOUR_OF_DAY);
                int min = time.get(Calendar.MINUTE);
                TimePickerDialog pick = new TimePickerDialog(root.getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                mTimeUntilUp.setText(i + ":" + i1);
                            }
                        }, hour, min, false);
                pick.show();
            }
        });

        final ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Do your code from onActivityResult
                        setPic();
                        mCaptureButton.setEnabled(true);
                        mCaptureButton.setText("Retake Picture");
                        File f = new File(currentPhotoPath);
                        imageUri = Uri.fromFile(f);
                    }
                });

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Toast.makeText(getContext(), "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("NU E bine", "onClick: A ajuns aici");
                        return;
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        imageUri = FileProvider.getUriForFile(getContext(),
                                "com.example.auctionhouse",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        mLauncher.launch(takePictureIntent);
                    }
                }
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ValidateProduct() == 1) {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_add, new HomeFragment());
                    fragmentTransaction.commit();
                }
            }
        });

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.hide();


        return root;
    }

    private int ValidateProduct() {
        description = mDescription.getText().toString();
        name = mName.getText().toString();
        price = mPrice.getText().toString();
        dateUntilUp = mDateUntilUp.getText().toString();
        timeUntilUp = mTimeUntilUp.getText().toString();

        if (imageUri == null) {
            Toast.makeText(AddFragment.this.getContext(), "Product image is mandatory...", Toast.LENGTH_SHORT).show();
            return 0;
        } else if (description.isEmpty()) {
            Toast.makeText(AddFragment.this.getContext(), "Please write descriprion...", Toast.LENGTH_SHORT).show();
            return 0;
        } else if (name.isEmpty()) {
            Toast.makeText(AddFragment.this.getContext(), "Please write name...", Toast.LENGTH_SHORT).show();
            return 0;
        } else if (price.isEmpty()) {
            Toast.makeText(AddFragment.this.getContext(), "Please write starting price...", Toast.LENGTH_SHORT).show();
            return 0;
        } else if (dateUntilUp.isEmpty()) {
            Toast.makeText(AddFragment.this.getContext(), "Please choose the date until the item is up...", Toast.LENGTH_SHORT).show();
            return 0;
        } else if (timeUntilUp.isEmpty()) {
            Toast.makeText(AddFragment.this.getContext(), "Please choose the time until the item is up...", Toast.LENGTH_SHORT).show();
            return 0;
        } else {
            new MyAsyncTask().execute();
            return 1;
        }
    }

    private void StoreInfo() {
//        loadingBar.setTitle("Add New Product");
//        loadingBar.setMessage("Please wait, while we are adding the product...");
//        loadingBar.setCanceledOnTouchOutside(false);
//        loadingBar.show();

        Calendar cldr = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy/MMM/dd");
        currDate = currentDate.format(cldr.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
        currTime = currentTime.format(cldr.getTime());

        final String productRandomKey = UUID.randomUUID().toString();

        final StorageReference filePath = productImageRef.child(imageUri.getLastPathSegment() + productRandomKey + ".jpg");

        final UploadTask uploadTask = filePath.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String msg = e.toString();
                Toast.makeText(AddFragment.this.getContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AddFragment.this.getContext(), "Product Image uploaded successfully", Toast.LENGTH_SHORT).show();

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }
                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadImageUrl = task.getResult().toString();

                            SaveProductInfoToDb();
                        }
                    }

                    private void SaveProductInfoToDb() {
                        HashMap<String, Object> productMap = new HashMap<>();

                        productMap.put("pid", productRandomKey);
                        productMap.put("name", name);
                        productMap.put("price", price);
                        productMap.put("dateUp", currDate);
                        productMap.put("timeUp", currTime);
                        productMap.put("dateDown", dateUntilUp);
                        productMap.put("timeDown", timeUntilUp);
                        productMap.put("description", description);
                        productMap.put("image", downloadImageUrl);
                        productMap.put("userUpload", Prevalent.currentOnlineUser.getEmail());
                        productMap.put("userLastBid", Prevalent.currentOnlineUser.getEmail());
                        productMap.put("currency", chooseCurrency.getText().toString());
                        productMap.put("auction", "open");

                        productUsersRef.child(Prevalent.currentOnlineUser.getEmail()).child("uploadedListings").setValue(Prevalent.currentOnlineUser.getUploadedListings() + 1);
                        Prevalent.currentOnlineUser.setUploadedListings(Prevalent.currentOnlineUser.getUploadedListings() + 1);

                        productDbRef.child(productRandomKey).updateChildren(productMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            HomeFragment fragment = new HomeFragment();
                                            FragmentManager fragManager = getActivity().getSupportFragmentManager();
                                            FragmentTransaction ft = fragManager.beginTransaction();
                                            ft.replace(AddFragment.this.getId(), fragment).addToBackStack(null);
                                            ft.commit();

                                            loadingBar.dismiss();
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(AddFragment.this.getContext(), "Product was added to auction!", Toast.LENGTH_SHORT).show();
                                                }
                                            }, 2000);
                                        } else {
                                            loadingBar.dismiss();
                                            String msg = Objects.requireNonNull(task.getException()).toString();
                                            Toast.makeText(AddFragment.this.getContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitMap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitMap);
    }

    class MyAsyncTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            loadingBar.setTitle("Add New Product");
            loadingBar.setMessage("Please wait, while we are adding the product...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            StoreInfo();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            loadingBar.dismiss();
        }
    }

}

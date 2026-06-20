package com.example.weather_trip.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.example.weather_trip.WeatherTripApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageHelper {

    private static final int MAX_IMAGE_SIZE = 1024;
    private static final int COMPRESSION_QUALITY = 80;
    private static final OkHttpClient client = new OkHttpClient();
    private static final String STORAGE_URL = "https://otubvbwmlbztbmqrpavz.supabase.co/storage/v1";
    private static final String BUCKET_NAME = "weatherimages";

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public static void uploadTripImage(Context context, Uri imageUri, long userId, long eventId, UploadCallback callback) {
        new Thread(() -> {
            try {
                File compressedFile = compressImage(context, imageUri);
                byte[] imageBytes = fileToBytes(compressedFile);
                String path = "users/" + userId + "/events/" + eventId + "/cover.jpg";

                String uploadUrl = STORAGE_URL + "/object/" + BUCKET_NAME + "/" + path;
                String serviceRoleKey = WeatherTripApplication.getSupabaseServiceRoleKey();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "cover.jpg",
                                RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                        .build();

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer " + serviceRoleKey)
                        .addHeader("apikey", serviceRoleKey)
                        .addHeader("x-upsert", "true")
                        .build();

                compressedFile.delete();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, java.io.IOException e) {
                        e.printStackTrace();
                        if (callback != null) {
                            callback.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws java.io.IOException {
                        if (response.isSuccessful()) {
                            String publicUrl = STORAGE_URL + "/object/public/" + BUCKET_NAME + "/" + path;
                            response.close();
                            if (callback != null) {
                                callback.onSuccess(publicUrl);
                            }
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                            response.close();
                            if (callback != null) {
                                callback.onError("Upload failed: " + response.code() + " - " + errorBody);
                            }
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public static File compressImage(Context context, Uri imageUri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new Exception("Không thể đọc file ảnh");
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        int width = options.outWidth;
        int height = options.outHeight;
        int sampleSize = 1;

        while ((width / sampleSize) > MAX_IMAGE_SIZE * 2 || (height / sampleSize) > MAX_IMAGE_SIZE * 2) {
            sampleSize *= 2;
        }

        inputStream = context.getContentResolver().openInputStream(imageUri);
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        if (bitmap == null) {
            throw new Exception("Không thể giải mã ảnh");
        }

        int targetWidth = MAX_IMAGE_SIZE;
        int targetHeight = (int) (bitmap.getHeight() * (MAX_IMAGE_SIZE / (double) bitmap.getWidth()));
        if (targetHeight > MAX_IMAGE_SIZE) {
            targetHeight = MAX_IMAGE_SIZE;
            targetWidth = (int) (bitmap.getWidth() * (MAX_IMAGE_SIZE / (double) bitmap.getHeight()));
        }

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);

        File outputFile = new File(context.getCacheDir(), "upload_temp_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream out = new FileOutputStream(outputFile);
        resized.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out);
        out.close();

        if (resized != bitmap) {
            resized.recycle();
        }
        bitmap.recycle();

        return outputFile;
    }

    private static byte[] fileToBytes(File file) throws Exception {
        InputStream inputStream = new java.io.FileInputStream(file);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        return outputStream.toByteArray();
    }

    public static void deleteTripImage(long userId, long eventId) {
        new Thread(() -> {
            try {
                String path = "users/" + userId + "/events/" + eventId + "/cover.jpg";
                String deleteUrl = STORAGE_URL + "/object/" + BUCKET_NAME + "/" + path;
                String serviceRoleKey = WeatherTripApplication.getSupabaseServiceRoleKey();

                Request request = new Request.Builder()
                        .url(deleteUrl)
                        .delete()
                        .addHeader("Authorization", "Bearer " + serviceRoleKey)
                        .addHeader("apikey", serviceRoleKey)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, java.io.IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

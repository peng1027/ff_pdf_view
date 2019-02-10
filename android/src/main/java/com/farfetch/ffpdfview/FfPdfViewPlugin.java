package com.farfetch.ffpdfview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FfPdfViewPlugin */
public class FfPdfViewPlugin implements MethodCallHandler {

    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;
    private ParcelFileDescriptor mFileDescriptor;
    private Registrar registrar;

    public FfPdfViewPlugin(Registrar registrar) {
        this.registrar = registrar;
    }

    /** Plugin registration. */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "ff_pdf_view");
        channel.setMethodCallHandler(new FfPdfViewPlugin(registrar));
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("renderPdf")) {
            try {
                String path = call.argument("path");
                result.success(renderPdf(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            result.notImplemented();
        }
    }

    private List<String> renderPdf(String filePath) throws IOException {
        PdfRenderer renderer = openRenderer(registrar.context(), registrar.lookupKeyForAsset(filePath));
        if (renderer == null) {
            return new ArrayList<>();
        }

        List<Bitmap> bitmaps = new ArrayList<>();
        int count = renderer.getPageCount();
        for (int i = 0; i < count; i++) {
            Bitmap bitmap = createBitmapOfPage(i);
            if (bitmap != null) {
                bitmaps.add(bitmap);
            }
        }
        closeRenderer();

        // Send bitmaps to flutter
        List<String> list = new ArrayList<>();
        for (Bitmap bitmap : bitmaps) {
            String s = saveBitmap(bitmap);
            list.add(s);
        }
        return list;
    }

    private PdfRenderer openRenderer(Context context, String fileName) throws IOException {
        File file = new File(context.getCacheDir(), generateRandomFilename() + ".pdf");
        Log.d("openRenderer", "created file: " + file);
        if (!file.exists()) {
            InputStream asset;
            try {
                asset = context.getAssets().open(fileName);
                FileOutputStream output = null;
                output = new FileOutputStream(file);
                final byte[] buffer = new byte[1024];
                int size;

                while ((size = asset.read(buffer)) != -1) {
                    output.write(buffer, 0, size);
                }
                asset.close();
                output.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        if (mFileDescriptor != null) {
            return mPdfRenderer = new PdfRenderer(mFileDescriptor);
        }

        return null;
    }

    private Bitmap createBitmapOfPage(int index) {
        // Show the first page.
        if (mPdfRenderer.getPageCount() <= index) {
            return null;
        }

        // Make sure to close the current page before opening another one.
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }

        mCurrentPage = mPdfRenderer.openPage(index);

        Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Now render the page onto the Bitmap.
        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        if (null != mCurrentPage) {
            mCurrentPage.close();
            mCurrentPage = null;
        }

        return bitmap;
    }

    private void closeRenderer() throws IOException {
        mPdfRenderer.close();
        mFileDescriptor.close();
    }

    private String saveBitmap(Bitmap bitmap) throws IOException {
        File createdPdfBitmap = new File(registrar.context().getCacheDir(), generateRandomFilename() + ".png");
        FileOutputStream fOut = new FileOutputStream(createdPdfBitmap);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        fOut.flush();
        fOut.close();

        return createdPdfBitmap.getAbsolutePath();
    }

    private String generateRandomFilename() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}

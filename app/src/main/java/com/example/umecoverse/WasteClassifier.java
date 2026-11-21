package com.example.umecoverse;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class WasteClassifier {

    public static class Result {
        public final String label;
        public final float confidence; // 0.0 - 1.0

        Result(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }
    }

    private static final int SIZE = 224;
    private final Interpreter interpreter;
    private final List<String> labels = new ArrayList<>();

    public WasteClassifier(Context context) throws IOException {
        AssetFileDescriptor fd = context.getAssets().openFd("model_unquant.tflite");
        FileInputStream stream = new FileInputStream(fd.getFileDescriptor());
        MappedByteBuffer model = stream.getChannel().map(
                FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
        interpreter = new Interpreter(model);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            // "0 Plastic" -> "Plastic"
            labels.add(line.substring(line.indexOf(' ') + 1).trim());
        }
        reader.close();
    }

    public Result classify(Bitmap bitmap) {
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, SIZE, SIZE, true);
        ByteBuffer input = ByteBuffer.allocateDirect(4 * SIZE * SIZE * 3)
                .order(ByteOrder.nativeOrder());

        int[] pixels = new int[SIZE * SIZE];
        scaled.getPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE);
        for (int p : pixels) {
            input.putFloat((((p >> 16) & 0xFF) / 127.5f) - 1f);
            input.putFloat((((p >> 8) & 0xFF) / 127.5f) - 1f);
            input.putFloat(((p & 0xFF) / 127.5f) - 1f);
        }

        float[][] output = new float[1][labels.size()];
        interpreter.run(input, output);

        int best = 0;
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > output[0][best]) best = i;
        }
        return new Result(labels.get(best), output[0][best]);
    }

    public void close() {
        interpreter.close();
    }
}
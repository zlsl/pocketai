package ru.zlsl.pocketai;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.UUID;

import ru.zlsl.pocketai.databinding.ActivityEditBinding;


public class EditTemplateActivity extends AppCompatActivity {
    private static String filename = "";
    private static String title = "";
    ActivityEditBinding binding;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        if (intent.hasExtra("filename")) {
            filename = intent.getStringExtra("filename");
            title = intent.getStringExtra("title");
            LoadTemplate();
        }

        binding.fab.setOnClickListener(v -> SaveTemplate());
    }

    private void SaveTemplate() {
        try {
            if (filename.isEmpty()) {
                filename = UUID.randomUUID().toString() + ".pai";
            }
            FileWriter out = new FileWriter(new File(App.getContext().getFilesDir(), filename));
            out.write("*" + binding.edTitle.getText().toString() + "*\n");
            Log.i("T1", "*" + binding.edTitle.getText().toString() + "*\n");
            out.write(binding.edContent.getText().toString());
            Log.i("T2", binding.edContent.getText().toString());
            out.close();
            Log.i("Template saved", filename);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void LoadTemplate() {
        try {
            FileInputStream fis = App.getContext().openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("*")) {
                    title = line.replace("*", "");
                } else {
                    if (sb.length() == 0) {
                        sb.append(line);
                    } else {
                        sb.append("\n").append(line);
                    }
                }
            }
            bufferedReader.close();
            isr.close();
            fis.close();
            binding.edContent.setText(sb);
            binding.edTitle.setText(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
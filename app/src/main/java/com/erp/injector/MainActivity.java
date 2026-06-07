package com.erp.injector;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private ListView listView;
    private List<String> builds = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String buildsDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Перевіряємо, чи існує розмітка, або використовуємо просту системну
        try {
            setContentView(R.layout.activity_main);
            listView = findViewById(R.id.listBuilds);
        } catch (Exception e) {
            listView = new ListView(this);
            setContentView(listView);
        }

        buildsDir = getFilesDir() + "/builds/";
        File dir = new File(buildsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, builds);
        listView.setAdapter(adapter);
        loadBuilds();

        // Безпечне підключення кнопки додавання файлу
        try {
            findViewById(R.id.btnAdd).setOnClickListener(v -> pickFile());
        } catch (Exception ignored) {}

        listView.setOnItemClickListener((p, v, pos, id) -> injectBuild(builds.get(pos)));
        listView.setOnItemLongClickListener((p, v, pos, id) -> {
            deleteBuild(builds.get(pos));
            return true;
        });
    }

    private void loadBuilds() {
        builds.clear();
        File[] files = new File(buildsDir).listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().endsWith(".so")) {
                    builds.add(f.getName());
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void pickFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == 1 && res == RESULT_OK && data != null) {
            copyFile(data.getData());
        }
    }

    private void copyFile(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            if (in == null) return;
            
            FileOutputStream out = new FileOutputStream(buildsDir + "build_" + System.currentTimeMillis() + ".so");
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            Toast.makeText(this, "Збірку додано!", Toast.LENGTH_SHORT).show();
            loadBuilds();
        } catch (Exception e) {
            Toast.makeText(this, "Помилка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void injectBuild(String name) {
        try {
            System.load(buildsDir + name);
            Toast.makeText(this, "Запущено: " + name, Toast.LENGTH_SHORT).show();
            
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.rockstar.gtasa");
            if (launch != null) {
                startActivity(launch);
            } else {
                Toast.makeText(this, "GTA SA не встановлена!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Помилка інжекту: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBuild(String name) {
        File file = new File(buildsDir + name);
        if (file.exists() && file.delete()) {
            Toast.makeText(this, "Видалено!", Toast.LENGTH_SHORT).show();
            loadBuilds();
        }
    }
}

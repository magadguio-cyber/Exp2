package com.erp.injector;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    ListView listView;
    List<String> builds = new ArrayList<>();
    ArrayAdapter<String> adapter;
    String buildsDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildsDir = getFilesDir() + "/builds/";
        new File(buildsDir).mkdirs();
        listView = findViewById(R.id.listBuilds);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, builds);
    
        
         listView.setAdapter(adapter);
        loadBuilds();
        findViewById(R.id.btnAdd).setOnClickListener(v -> pickFile());
        listView.setOnItemClickListener((p, v, pos, id) -> injectBuild(builds.get(pos)));
        listView.setOnItemLongClickListener((p, v, pos, id) -> { deleteBuild(builds.get(pos)); return true; });
    }

    void loadBuilds() {
        builds.clear();
        File[] files = new File(buildsDir).listFiles();
        if (files != null) for (File f : files) if (f.getName().endsWith(".so")) builds.add(f.getName());
        adapter.notifyDataSetChanged();
    }

    void pickFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == 1 && res == RESULT_OK && data != null) copyFile(data.getData());
    }

    void copyFile(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            FileOutputStream out = new FileOutputStream(buildsDir + "build_" + System.currentTimeMillis() + ".so");
            byte[] buf = new byte[4096]; int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            in.close(); out.close();
            Toast.makeText(this, "Збірку додано!", Toast.LENGTH_SHORT).show();
            loadBuilds();
        } catch (Exception e) { Toast.makeText(this, "Помилка: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
    }

    void injectBuild(String name) {
        try {
            System.load(buildsDir + name);
            Toast.makeText(this, "Запущено: " + name, Toast.LENGTH_SHORT).show();
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.rockstar.gtasa");
            if (launch != null) startActivity(launch);
            else Toast.makeText(this, "GTA SA не встановлена!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { Toast.makeText(this, "Помилка: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
    }

    void deleteBuild(String name) {
        new File(buildsDir + name).delete();
        Toast.makeText(this, "Видалено!", Toast.LENGTH_SHORT).show();
        loadBuilds();
    }
}

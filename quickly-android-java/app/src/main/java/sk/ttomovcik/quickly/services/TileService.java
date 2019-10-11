package sk.ttomovcik.quickly.services;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import sk.ttomovcik.quickly.activities.AddTask;

@SuppressLint("Registered")
@RequiresApi(api = Build.VERSION_CODES.N)
public class TileService extends android.service.quicksettings.TileService {

    @Override
    public void onTileAdded() {
        Log.d("TileService", "onTileAdded()");
    }

    @Override
    public void onStartListening() {
        Log.d("TileService", "onStartListening()");
    }

    @Override
    public void onClick() {
        Log.d("TileService", "onClick() -> AddTask.class");
        Intent intent = new Intent(this, AddTask.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public void onStopListening() {
        Log.d("TileService", "onStopListening()");
    }

    @Override
    public void onTileRemoved() {
        Log.d("TileService", "onTileRemoved()");
    }
}

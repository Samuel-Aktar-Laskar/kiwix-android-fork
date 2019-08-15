package org.kiwix.kiwixmobile.wifi_hotspot;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import org.kiwix.kiwixmobile.webserver.ServerStateListener;

/**
 * WifiHotstopManager class makes use of the Android's WifiManager and WifiConfiguration class
 * to implement the wifi hotspot feature.
 * Created by Adeel Zafar on 28/5/2019.
 */

public class WifiHotspotManager {
  private final WifiManager wifiManager;
  private final Context context;
  WifiManager.LocalOnlyHotspotReservation hotspotReservation;
  boolean isHotspotEnabled;
  WifiConfiguration currentConfig;
  private static final String TAG = "WifiHotspotManager";

  public WifiHotspotManager(@NonNull Context context) {
    this.context = context;
    wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
  }

  //Workaround to turn on hotspot for Oreo versions
  @RequiresApi(api = Build.VERSION_CODES.O)
  public void turnOnHotspot(@NonNull ServerStateListener serverStateListener) {
    wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

      @Override
      public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
        super.onStarted(reservation);
        hotspotReservation = reservation;
        currentConfig = hotspotReservation.getWifiConfiguration();

        Log.v(TAG, "THE PASSWORD IS: "
            + currentConfig.preSharedKey
            + " \n SSID is : "
            + currentConfig.SSID);

        serverStateListener.hotspotTurnedOn(currentConfig);

        isHotspotEnabled = true;

        Log.v(TAG, "Is hotspot enabled? " + isHotspotEnabled);
      }

      @Override
      public void onStopped() {
        super.onStopped();
        Log.v(TAG, "Local Hotspot Stopped");
        serverStateListener.serverStopped();
      }

      @Override
      public void onFailed(int reason) {
        super.onFailed(reason);
        Log.v(TAG, "Local Hotspot failed to start");
        serverStateListener.hotspotFailed();
        isHotspotEnabled = false;
        Log.v(TAG, "Is hotspot enabled? " + isHotspotEnabled);
      }
    }, new Handler());
  }

  //Workaround to turn off hotspot for Oreo versions
  @RequiresApi(api = Build.VERSION_CODES.O)
  public void turnOffHotspot() {
    if (hotspotReservation != null) {
      hotspotReservation.close();
      hotspotReservation = null;
      isHotspotEnabled = false;
      Log.v(TAG, "Turned off hotspot");
    }
  }

  //This method checks the state of the hostpot for devices>=Oreo
  @RequiresApi(api = Build.VERSION_CODES.O)
  public boolean checkHotspotState() {
    return hotspotReservation != null;
  }
}

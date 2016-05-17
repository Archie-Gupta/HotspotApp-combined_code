package com.example.archiegupta.hotspot_app;

/**
 * Created by archie gupta on 30-Sep-15.
 */
import java.util.ArrayList;

public interface FinishScanListener {
    public void onFinishScan(ArrayList<ClientScanResult> clients);
}

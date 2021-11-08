package com.rose.management;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.rose.ggpo.GGPONetworkStatus;
import com.rose.network.Client;

import java.text.DecimalFormat;

public class PerformanceMonitor {
    private final Table table;
    private GGPONetworkStatus stats;
    private final Label[] net_perf_values;

    private final Label[] sync_perf_values;
    private long last_text_update_time;

    public PerformanceMonitor() {
        table = new Table();
        table.setVisible(true);
        Skin skin = new Skin(Gdx.files.internal("skin/glassy-ui.json"));
        // Network performance
        Label[] net_perf = new Label[4];
        net_perf_values = new Label[net_perf.length];
        // Synchronization performance
        Label[] sync_perf = new Label[2];
        sync_perf_values = new Label[sync_perf.length];

        for(int i = 0; i < net_perf.length; i++) {
            net_perf[i] = new Label("", skin);
            net_perf[i].setColor(Color.LIME);
            net_perf[i].setFontScale(net_perf[i].getFontScaleX() / 2);
            net_perf_values[i] = new Label("0.000", skin);
            net_perf_values[i].setColor(Color.LIME);
            net_perf_values[i].setFontScale(net_perf[i].getFontScaleX());
            table.add(net_perf[i]).fillX().uniform();
            table.add(net_perf_values[i]).right();
            table.row().pad(2,0,0,0);
        }

        net_perf[0].setText("Network latency: ");
        net_perf[1].setText("Frame latency: ");
        net_perf[2].setText("Network bandwidth: ");
        net_perf[3].setText("Packet loss rate: ");

        for(int i = 0; i < sync_perf.length; i++) {
            sync_perf[i] = new Label("", skin);
            sync_perf[i].setColor(Color.MAGENTA);
            sync_perf[i].setFontScale(sync_perf[i].getFontScaleX() / 2);
            sync_perf_values[i] = new Label("0.000", skin);
            sync_perf_values[i].setColor(Color.MAGENTA);
            sync_perf_values[i].setFontScale(sync_perf[i].getFontScaleX());
            table.add(sync_perf[i]).fillX().uniform();
            table.add(sync_perf_values[i]).right();
            table.row().pad(2,0,0,0);
        }

        sync_perf[0].setText("Local status: ");
        sync_perf[1].setText("Remote status: ");
    }

    public void toggleView() {
        table.setVisible(!table.isVisible());
    }

    public Table getTable() {
        return table;
    }

    public void update(Client client) {
        GGPONetworkStatus stats = client.getNetworkStats();
        long now = System.nanoTime();

        if (now > last_text_update_time + 500000000L) {
            // network latency
            net_perf_values[0].setText(Long.toString(stats.network.ping / 1000000L) + " ms");
            // frame latency
            String frameLag = stats.network.ping > 0 ? Double.toString(stats.network.ping * 60.0 / 1000000000L) : "0";
            net_perf_values[1].setText(frameLag + " frames");
            // bandwidth
            DecimalFormat df = new DecimalFormat("#.##");
            net_perf_values[2].setText(df.format((double)(stats.network.kbps_sent / 8.0)) + " kilobytes/sec");
            // packet loss rate
            net_perf_values[3].setText("0");

            // local frames behind
            sync_perf_values[0].setText(Integer.toString(stats.timesync.local_frames_behind) + " frames");
            // remote frames behind
            sync_perf_values[1].setText(Integer.toString(stats.timesync.remote_frames_behind) + " frames");

            last_text_update_time = now;
        }
    }
}

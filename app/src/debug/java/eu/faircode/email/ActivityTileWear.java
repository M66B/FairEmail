package eu.faircode.email;

import android.content.ComponentName;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.wear.tiles.manager.TileUiClient;

public class ActivityTileWear extends ComponentActivity {
    private TileUiClient mTileUiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout rootLayout = new FrameLayout(this);
        FrameLayout.LayoutParams layoutparams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(layoutparams);
        rootLayout.setBackgroundColor(Color.BLACK);

        setContentView(rootLayout);

        mTileUiClient = new TileUiClient(this,
                new ComponentName(this, ServiceTileWear.class), rootLayout);
        mTileUiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTileUiClient.close();
    }
}

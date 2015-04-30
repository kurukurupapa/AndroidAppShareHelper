package com.kurukurupapa.appsharehelper.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.fragment.ShareHistoryFragment;
import com.kurukurupapa.appsharehelper.fragment.NavigationDrawerFragment;
import com.kurukurupapa.appsharehelper.fragment.StandardAppFragment;
import com.kurukurupapa.appsharehelper.model.NavigationDrawerItems;
import com.kurukurupapa.appsharehelper.service.ClipboardService;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ShareHistoryFragment.OnFragmentInteractionListener,
        StandardAppFragment.OnFragmentInteractionListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * クリップボードサービス
     */
    private ClipboardService mClipboardService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // オブジェクト生成
        mClipboardService = new ClipboardService(this);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.d(TAG, "onNavigationDrawerItemSelected called");

        if (position == NavigationDrawerItems.POSITION_SETTINGS) {
            // 設定画面を起動します。
            startSettingsActivity();

        } else if (position == NavigationDrawerItems.POSITION_CLIPBOARD) {
            // 受信アクティビティを起動します。
            startRecvActivityWithClipboard();
            // ナビゲーションドロワーの選択項目を1番目に戻しておきます。
            mNavigationDrawerFragment.selectItem(0);

        } else {
            // フラグメントを作成する。
            Fragment fragment = null;
            switch (position) {
                case NavigationDrawerItems.POSITION_HISTORY:
                    fragment = ShareHistoryFragment.newInstance(null, null);
                    mTitle = getString(R.string.title_nav_history);
                    break;
                case NavigationDrawerItems.POSITION_STANDARD_APP:
                    fragment = StandardAppFragment.newInstance(null, null);
                    mTitle = getString(R.string.title_nav_standard_app);
                    break;
            }

            // update the main content by replacing fragments
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    public void restoreActionBar() {
        // デフォルト設定でアイコン付加されました。
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu called");

        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only view_change items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to view_change in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // 設定画面を呼び出します。
            startSettingsActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStandardAppFragmentInteraction(String id) {
        // TODO
    }

    @Override
    public void onShareHistoryFragmentInteraction(String id) {
        // TODO
    }

    /**
     * 受信アクティビティを呼び出します。
     */
    private void startRecvActivityWithClipboard() {
        // インテントを作成
        Intent intent = mClipboardService.createIntent();
        if (intent == null) {
            Toast.makeText(this, getString(R.string.msg_no_clipdata), Toast.LENGTH_SHORT).show();
            return;
        }

        // 受信アクティビティを呼び出します。
        startActivity(intent);
    }

    /**
     * 設定画面を呼び出します。
     */
    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}

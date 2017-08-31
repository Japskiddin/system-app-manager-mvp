package ru.androidtools.system_app_manager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import ru.androidtools.system_app_manager.model.AppInfo;
import ru.androidtools.system_app_manager.model.AppsInteractorImpl;
import ru.androidtools.system_app_manager.presenter.MainPresenter;
import ru.androidtools.system_app_manager.presenter.MainPresenterImpl;
import ru.androidtools.system_app_manager.view.MainView;

import static ru.androidtools.system_app_manager.SP.SHOW_UNINSTALL_DIALOG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AppsAdapter.ClickListener,
        MainView {
    private int REQUEST_UNINSTALL = 1;
    private SearchView mSearchView;
    private AppsAdapter mAppsAdapter;
    private LinearLayout footer;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MainPresenter mainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = findViewById(R.id.progress_spinner);
        recyclerView = findViewById(R.id.recyclerView);

        mainPresenter = new MainPresenterImpl(this, new AppsInteractorImpl());
        mainPresenter.downloadApps(new GetAppsTask(this));

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null) {
            ActionBarDrawerToggle toggle =
                    new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
                            R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        footer = findViewById(R.id.footer);
        Button delete = findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainPresenter.deleteApp();
            }
        });
        Button clear = findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainPresenter.clearSelection();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                search(query);
                return true;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (footer.getVisibility() == View.VISIBLE) {
                    mainPresenter.clearSelection();
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            if (footer.getVisibility() == View.VISIBLE) {
                mainPresenter.clearSelection();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        mainPresenter.clearSelection();
        if (id == R.id.nav_installed) {
            mainPresenter.changeList(R.string.menu_installed);
        } else if (id == R.id.nav_system) {
            mainPresenter.changeList(R.string.menu_system);
            checkRootDialog();
        } else if (id == R.id.nav_settings) {
            mainPresenter.onSettingsClick();
        } else if (id == R.id.nav_wiki) {
            mainPresenter.onWikiClick();
        } else if (id == R.id.nav_share) {
            mainPresenter.onShareClick(ShareCompat.IntentBuilder.from(this));
        } else if (id == R.id.nav_send) {
            mainPresenter.onSendClick(ShareCompat.IntentBuilder.from(this));
        } else if (id == R.id.nav_apps) {
            mainPresenter.onMarketClick();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null) drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDownloadApps(List<AppInfo> mAppsList, GetIconThread<AppsAdapter.AppViewHolder> getIconThread, int currentList) {
        setAdapter(getIconThread);
        for (AppInfo app : mAppsList) {
            mAppsAdapter.addApp(app);
        }

        mAppsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSetToolbarTitle(String title) {
        setTitle(title);
    }

    @Override
    public boolean isPackageInstalled(String packageName) {
        return (Tools.isPackageInstalled(this, packageName));
    }

    @Override
    public void onSelectionClear() {
        footer.setVisibility(View.GONE);
        mAppsAdapter.clearSelection();
    }

    @Override
    public void onToastReady(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public int getMemoryClassFromActivity() {
        return ((ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    }

    @Override
    public PackageManager getPackageManagerFromActivity() {
        return getPackageManager();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UNINSTALL) {
            // 0 means success, other means failed.
            Log.d("TAG", "got result of uninstall: " + resultCode);
            mainPresenter.updateAppList();
        }
    }

    @Override
    public void onReadyActivityStart(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void onReadyActivityStartForResult(Intent intent) {
        startActivityForResult(intent, REQUEST_UNINSTALL);
    }

    @Override
    public void onUpdatedList(AppInfo ai) {
        mAppsAdapter.remove(ai);
    }

    @Override
    public void onPrepareDeleteDialog(List<AppInfo> selectedList) {
        progressBar.setVisibility(View.VISIBLE);
        mSearchView.setVisibility(View.GONE);
        for (AppInfo ai : selectedList) {
            if (Tools.m521a(ai.publicSourceDir)) mAppsAdapter.remove(ai);
        }
        progressBar.setVisibility(View.GONE);
        mSearchView.setVisibility(View.VISIBLE);
        mainPresenter.updateAppList();
        if (SP.getBoolean(this, SHOW_UNINSTALL_DIALOG, true)) {
            mainPresenter.showUninstallDialog(new AlertDialog.Builder(this));
        }
    }

    @Override
    public String getStringFromResources(int id) {
        return getString(id);
    }

    @Override
    public View getViewForDialog(int layout) {
        LayoutInflater inflater = this.getLayoutInflater();
        return inflater.inflate(layout, null);
    }

    @Override
    public void saveCheckedUninstallDialog() {
        SP.setBoolean(MainActivity.this, SHOW_UNINSTALL_DIALOG, false);
    }

    @Override
    public void onItemClicked(AppInfo ai) {
        mainPresenter.addToSelected(ai);
    }

    @Override
    public void onGoneFooter() {
        footer.setVisibility(View.GONE);
    }

    @Override
    public void onVisibleFooter() {
        footer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSelectionChanged(List<AppInfo> selectedList) {
        mAppsAdapter.updateSelected(selectedList);
    }

    @Override
    protected void onStop() {
        mainPresenter.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mainPresenter.onDestroy();
        super.onDestroy();
    }

    private void checkRootDialog() {
        if (!Tools.checkRooted()) {
            mainPresenter.prepareRootDialog(new AlertDialog.Builder(this));
        }
    }

    public void setAdapter(GetIconThread<AppsAdapter.AppViewHolder> getIconThread) {
        mAppsAdapter = new AppsAdapter(this, getIconThread);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAppsAdapter);
    }

    private void search(String query) {
        if (mAppsAdapter != null) mAppsAdapter.filter(query);
    }
}

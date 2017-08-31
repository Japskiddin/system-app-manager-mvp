package ru.androidtools.system_app_manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.androidtools.system_app_manager.model.AppInfo;

/**
 * Created by volodya on 07.07.2015.
 */
public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppViewHolder> {

    public interface ClickListener {
        void onItemClicked(AppInfo ai);
    }

    private static final String TAG = "AppsAdapter";
    private ClickListener clickListener;
    private List<AppInfo> appList, appListCopy, selectedList;
    private GetIconThread<AppViewHolder> mGetIconThread;

    public AppsAdapter(ClickListener clickListener,
                       GetIconThread<AppViewHolder> mGetIconThread) {//, dbCallBack<WifiInfo> db) {
        appList = new ArrayList<>();
        appListCopy = new ArrayList<>();
        selectedList = new ArrayList<>();
        this.clickListener = clickListener;
        this.mGetIconThread = mGetIconThread;
    }

    public void addApp(AppInfo appInfo) {
        appList.add(appInfo);
    }

    private void updateView() {
        Log.d("TEST", "updateView");
        for (AppInfo ai : selectedList) {
            int pos = appList.indexOf(ai);
            notifyItemChanged(pos);
        }
    }

    public void remove(AppInfo ai) {
        int pos = appList.indexOf(ai);
        appList.remove(ai);
        appListCopy.remove(ai);
        notifyItemRemoved(pos);
    }

    public void filter(String text) {
        appList.clear();
        if (text.isEmpty()) {
            appList.addAll(appListCopy);
        } else {
            text = text.toLowerCase();
            Set<AppInfo> filterSet = new HashSet<>();
            for (AppInfo item : appListCopy) {
                if (item.packageName.toLowerCase().contains(text) || item.title.toLowerCase()
                        .contains(text)) {
                    filterSet.add(item);
                }
            }
            appList = new ArrayList<>(filterSet);
            //Log.d(TAG, "filterSet.size : "+ filterSet.size());
        }
        notifyDataSetChanged();
    }

    public void updateSelected(List<AppInfo> selectedList) {
        Log.d("TEST", "updateSelected");
        this.selectedList.clear();
        this.selectedList.addAll(selectedList);
        updateView();
    }

    public void clearSelection() {
        Log.d("TEST", "clearSelection");
        selectedList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    @Override
    public void onBindViewHolder(final AppViewHolder appVH, int i) {
        final AppInfo ai = appList.get(i);
        appVH.vTitle.setText(ai.title);
        appVH.vPackageName.setText(ai.packageName);
        appVH.vVersion.setText(ai.versionName + " ## " + ai.versionCode);
        appVH.vSize.setText(ai.size);

        mGetIconThread.queueIcon(appVH, ai.packageName);

        if (selectedList.contains(ai)) {
            appVH.vItem.setBackgroundColor(
                    getColor(appVH.vItem.getContext(), R.color.navigationBarColor));
        } else {
            appVH.vItem.setBackgroundColor(0);
        }
        appVH.vItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedList.contains(ai)) {
                    v.setBackgroundColor(getColor(v.getContext(), R.color.navigationBarColor));
                } else {
                    v.setBackgroundColor(0);
                }
                clickListener.onItemClicked(ai);
            }
        });
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.app_item_layout, viewGroup, false);
        return new AppViewHolder(itemView);
    }

    public static int getColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(id);
        } else {
            //noinspection deprecation
            return context.getResources().getColor(id);
        }
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        protected TextView vPackageName, vTitle, vVersion, vSize;
        protected ImageView vIcon;
        protected LinearLayout vItem;

        public AppViewHolder(View v) {
            super(v);
            vItem = v.findViewById(R.id.item);
            vTitle = v.findViewById(R.id.title);
            vPackageName = v.findViewById(R.id.packageName);
            vIcon = v.findViewById(R.id.icon);
            vVersion = v.findViewById(R.id.version);
            vSize = v.findViewById(R.id.size);
        }

        // вот добавил из книги
        public void bindDrawable(Drawable drawable) {
            vIcon.setImageDrawable(drawable);
        }
    }
}
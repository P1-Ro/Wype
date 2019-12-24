package sk.p1ro.wype.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import sk.p1ro.wype.model.Constants;
import sk.p1ro.wype.model.FolderModel;
import sk.p1ro.wype.worker.RemoveFolderWorker;

import static android.content.Context.MODE_PRIVATE;
import static sk.p1ro.wype.model.Constants.FILE_CODE;
import static sk.p1ro.wype.model.Constants.FOLDERS;

interface CommonFunctionality {

    AtomicReference<String> editedFolder = new AtomicReference<>();

    default Integer updateList(Context context, FlexibleAdapter<FolderModel> adapter, boolean isTV) {
        SharedPreferences sp = context.getSharedPreferences(FOLDERS, MODE_PRIVATE);
        String folders = sp.getString(FOLDERS, "");
        adapter.clear();
        int size = 0;

        if (folders != null && !folders.isEmpty()) {
            String[] folderPaths = folders.split(";");

            size = folderPaths.length;

            for (String path : folderPaths) {
                adapter.addItem(new FolderModel(path, isTV));
            }
            adapter.notifyDataSetChanged();
        }

        return size;
    }

    default Boolean handleContextMenuClick(Activity context, FlexibleAdapter<FolderModel> adapter, int id, FolderModel model) {
        if (model != null) {
            switch (id) {
                case 1: //edit
                    editedFolder.set(model.gePath());
                    openFileChooser(context, model.gePath());
                    break;
                case 2: // delete
                    deleteFolderFromList(context, adapter, model.gePath());
                    break;
                default:
                    return false;
            }
        }
        return null;
    }

    default void onActivityResult(Context context, FlexibleAdapter<FolderModel> adapter, int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {

            List<Uri> files = Utils.getSelectedFilesFromResult(intent);
            File dir = Utils.getFileForUri(files.get(0));
            String folder = dir.getAbsolutePath();

            if (editedFolder.get() != null) {
                deleteFolderFromList(context, adapter, editedFolder.get());
                editedFolder.set(null);
            }

            SharedPreferences sp = context.getSharedPreferences(FOLDERS, MODE_PRIVATE);

            String folders = sp.getString(FOLDERS, "");
            LinkedHashSet<String> foldersList = new LinkedHashSet<>(Arrays.asList(Objects.requireNonNull(folders).split(";")));

            foldersList.add(folder);

            saveFolderList(sp, foldersList);

            updateList(context, adapter, context instanceof MainTVActivity);
            scheduleWorker(context);
        }
    }

    default void saveFolderList(SharedPreferences sp, LinkedHashSet<String> foldersList) {
        StringBuilder csvBuilder = new StringBuilder();

        for (String str : foldersList) {
            if (!str.isEmpty()) {
                csvBuilder.append(str);
                csvBuilder.append(";");
            }
        }

        String csv = csvBuilder.toString();
        sp.edit().putString(FOLDERS, csv).apply();
    }

    default void openFileChooser(Activity context) {
        openFileChooser(context, null);
    }

    default void openFileChooser(Activity context, String path) {
        Intent i = new Intent(context, FilePickerActivity.class);

        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        if (path != null) {
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, path);
        }

        context.startActivityForResult(i, FILE_CODE);
    }

    default void deleteFolderFromList(Context context, FlexibleAdapter<FolderModel> adapter, String path) {
        SharedPreferences sp = context.getSharedPreferences(FOLDERS, MODE_PRIVATE);
        String folders = sp.getString(FOLDERS, "");
        if (folders != null) {
            String[] foldersList = folders.split(";");
            LinkedHashSet<String> newFoldersList = new LinkedHashSet<>();

            for (String folder : foldersList) {
                if (!folder.equals(path)) {
                    newFoldersList.add(folder);
                }
            }

            saveFolderList(sp, newFoldersList);
            updateList(context, adapter, context instanceof MainTVActivity);
        }
    }

    default void scheduleWorker(Context context) {
        WorkManager mWorkManager = WorkManager.getInstance(context);

        SharedPreferences sp = context.getSharedPreferences(FOLDERS, MODE_PRIVATE);
        String folders = sp.getString(FOLDERS, "");

        if (folders != null && !folders.isEmpty()) {
            Data data = new Data.Builder()
                    .putString(FOLDERS, folders)
                    .build();

            PeriodicWorkRequest workRequest = new PeriodicWorkRequest
                    .Builder(RemoveFolderWorker.class,
                    1,
                    TimeUnit.DAYS)
                    .setInputData(data)
                    .build();

            mWorkManager.enqueueUniquePeriodicWork(Constants.WORKER_ID, ExistingPeriodicWorkPolicy.REPLACE, workRequest);
        }
    }
}

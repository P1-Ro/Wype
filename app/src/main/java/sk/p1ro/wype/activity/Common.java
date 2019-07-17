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

import eu.davidea.flexibleadapter.FlexibleAdapter;
import sk.p1ro.wype.model.FolderModel;
import sk.p1ro.wype.worker.RemoveFolderWorker;

import static android.content.Context.MODE_PRIVATE;

public class Common {

    private static int FILE_CODE = 9999;
    private static final String FOLDERS = "folders";
    private static String editedFolder = null;


    static void updateList(Context context, FlexibleAdapter<FolderModel> adapter) {
        SharedPreferences sp = context.getSharedPreferences(FOLDERS, MODE_PRIVATE);
        String folders = sp.getString(FOLDERS, "");
        adapter.clear();
        if (folders != null && !folders.isEmpty()) {
            String[] folderPaths = folders.split(";");

            for (String path : folderPaths) {
                adapter.addItem(new FolderModel(path));
            }
            adapter.notifyDataSetChanged();
        }
    }

    static Boolean handleContextMenuClick(Activity context, FlexibleAdapter<FolderModel> adapter, int id, FolderModel model) {
        if (model != null) {
            switch (id) {
                case 1: //edit
                    editedFolder = model.gePath();
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

    static void onActivityResult(Context context, FlexibleAdapter<FolderModel> adapter, int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {

            List<Uri> files = Utils.getSelectedFilesFromResult(intent);
            File dir = Utils.getFileForUri(files.get(0));
            String folder = dir.getAbsolutePath();

            if (editedFolder != null) {
                deleteFolderFromList(context, adapter, editedFolder);
                editedFolder = null;
            }

            SharedPreferences sp = context.getSharedPreferences(FOLDERS, MODE_PRIVATE);

            String folders = sp.getString(FOLDERS, "");
            LinkedHashSet<String> foldersList = new LinkedHashSet<>(Arrays.asList(Objects.requireNonNull(folders).split(";")));

            foldersList.add(folder);

            saveFolderList(sp, foldersList);

            Common.updateList(context, adapter);
            scheduleWorker(context);
        }
    }

    static void saveFolderList(SharedPreferences sp, LinkedHashSet<String> foldersList) {
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

    static void openFileChooser(Activity context) {
        openFileChooser(context, null);
    }

    static void openFileChooser(Activity context, String path) {
        Intent i = new Intent(context, FilePickerActivity.class);

        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        if (path != null) {
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, path);
        }

        context.startActivityForResult(i, FILE_CODE);
    }

    static void deleteFolderFromList(Context context, FlexibleAdapter<FolderModel> adapter, String path) {
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
            updateList(context, adapter);
        }
    }

    static void scheduleWorker(Context context) {
        WorkManager mWorkManager = WorkManager.getInstance();

        SharedPreferences sp = context.getSharedPreferences(FOLDERS, MODE_PRIVATE);
        String folders = sp.getString(FOLDERS, "");

        if (folders != null && !folders.isEmpty()) {
            Data data = new Data.Builder()
                    .putString("folders", folders)
                    .build();

            PeriodicWorkRequest workRequest = new PeriodicWorkRequest
                    .Builder(RemoveFolderWorker.class,
                    1,
                    TimeUnit.DAYS)
                    .setInputData(data)
                    .build();

            mWorkManager.enqueueUniquePeriodicWork("delete", ExistingPeriodicWorkPolicy.REPLACE, workRequest);
        }
    }
}

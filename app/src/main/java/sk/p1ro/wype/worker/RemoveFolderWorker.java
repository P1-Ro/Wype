package sk.p1ro.wype.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.util.Arrays;

import static sk.p1ro.wype.model.Constants.FOLDERS;

public class RemoveFolderWorker extends Worker {

    public RemoveFolderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Data data = getInputData();
        String foldersString = data.getString(FOLDERS);
        if (foldersString != null) {
            String[] folders = foldersString.split(";");
            boolean result = true;

            for (String folder : folders) {
                File dir = new File(folder);
                File[] files = dir.listFiles();

                if (files == null){
                    result = false;
                } else {
                    Log.d("worker", Arrays.toString(files));

                    for (File file : files) {
                        result &= file.delete();
                    }
                }
            }

            if (result) {
                return Result.success();
            } else {
                return Result.retry();
            }
        }

        return Result.success();
    }
}

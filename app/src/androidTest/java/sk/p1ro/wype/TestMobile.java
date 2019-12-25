package sk.p1ro.wype;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.testing.TestWorkerBuilder;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import sk.p1ro.wype.activity.MainPhoneActivity;
import sk.p1ro.wype.model.Constants;
import sk.p1ro.wype.utils.ViewActions;
import sk.p1ro.wype.worker.RemoveFolderWorker;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static sk.p1ro.wype.model.Constants.FOLDERS;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestMobile {

    @Rule
    public ActivityTestRule<MainPhoneActivity> activityRule = new ActivityTestRule<>(MainPhoneActivity.class);
    private Context mContext;
    private Executor mExecutor;

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Test
    public void test1_AppOpened() {
        onView(ViewMatchers.withId(R.id.textView))
                .check(matches(isDisplayed()));

        onView(ViewMatchers.withId(R.id.fab))
                .check(matches(isDisplayed()))
                .check(matches(isClickable()));

        onView(ViewMatchers.withId(R.id.recycler))
                .check(matches(isDisplayed()))
                .check(matches(ViewMatchers.hasChildCount(0)));

        assertWorkersCount(0);
    }

    @Test
    public void test2_AddingFolder() {
        onView(ViewMatchers.withId(R.id.fab)).perform(click());

        onView(ViewMatchers.withText("Android")).perform(click());
        onView(ViewMatchers.withText("OK")).perform(click());

        onView(ViewMatchers.withId(R.id.recycler))
                .check(matches(isDisplayed()))
                .check(matches(ViewMatchers.hasChildCount(1)));

        assertWorkersCount(1);
    }

    @Test
    public void test3_EditingFolder() {


        onView(ViewMatchers.withId(R.id.recycler))
                .perform(ViewActions.clickChildwWithId(R.id.imageView));

        onView(ViewMatchers.withText("Edit")).perform(click());
        onView(ViewMatchers.withText("..")).perform(click());
        onView(ViewMatchers.withText("Alarms")).perform(click());
        onView(ViewMatchers.withText("OK")).perform(click());

        onView(ViewMatchers.withId(R.id.recycler))
                .check(matches(isDisplayed()))
                .check(matches(ViewMatchers.hasChildCount(1)));

        assertWorkersCount(1);
    }

    @Test
    public void test4_TestDeletion() {

        SharedPreferences sp = mContext.getSharedPreferences(FOLDERS, MODE_PRIVATE);
        String folders = sp.getString(FOLDERS, "");

        for (String folder : Objects.requireNonNull(folders).split(";")){
            try {
                new File(folder, "test.tmp").createNewFile();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        Data data = new Data.Builder()
                .putString(FOLDERS, folders)
                .build();


        RemoveFolderWorker worker =
                (RemoveFolderWorker) TestWorkerBuilder.from(mContext,
                        RemoveFolderWorker.class,
                        mExecutor)
                        .setInputData(data)
                        .setTags(Collections.singletonList(Constants.WORKER_ID))
                        .build();

        ListenableWorker.Result result = worker.doWork();
        assertThat("Worker has finished with success", result, is(ListenableWorker.Result.success()));

        for (String folder : Objects.requireNonNull(folders).split(";")){
            assertThat("Count of deleted files", new File(folder).listFiles().length, is(0) );
        }
    }

    @Test
    public void test5_RemoveFolder() {
        onView(ViewMatchers.withId(R.id.recycler))
                .perform(ViewActions.clickChildwWithId(R.id.imageView));

        onView(ViewMatchers.withText("Delete")).perform(click());

        onView(ViewMatchers.withId(R.id.recycler))
                .check(matches(isDisplayed()))
                .check(matches(ViewMatchers.hasChildCount(0)));

        assertWorkersCount(1);
    }

    private void assertWorkersCount(int count) {
        int workers = -1;

        WorkManager mWorkManager = WorkManager.getInstance(mContext);
        try {
            List<WorkInfo> workInfos = mWorkManager.getWorkInfosByTag(Constants.WORKER_ID).get();
            workers++;
            for (WorkInfo workInfo : workInfos) {
                WorkInfo.State state = workInfo.getState();
                if (state == WorkInfo.State.ENQUEUED) {
                    workers++;
                }
            }
        }  catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        assertThat("Worker count should be 0", workers, is(count));
    }
}
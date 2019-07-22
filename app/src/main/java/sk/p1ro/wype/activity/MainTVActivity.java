package sk.p1ro.wype.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import sk.p1ro.wype.R;
import sk.p1ro.wype.model.FolderModel;

import static sk.p1ro.wype.activity.Common.handleContextMenuClick;
import static sk.p1ro.wype.activity.Common.openFileChooser;

public class MainTVActivity extends Activity {

    private FlexibleAdapter<FolderModel> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main_tv);


        Button button = findViewById(R.id.fab);
        button.setOnClickListener(view -> openFileChooser(this));

        List<FolderModel> folderModelList = new ArrayList<>();
        adapter = new FlexibleAdapter<>(folderModelList);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        int size = Common.updateList(this, adapter, true);
        if (size == 0){
            button.requestFocus();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        FolderModel model = adapter.getItem(item.getOrder());

        Boolean x = handleContextMenuClick(this, adapter, id, model);
        if (x != null) return x;
        return true;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Common.onActivityResult(this, adapter, requestCode, resultCode, intent);
    }
}

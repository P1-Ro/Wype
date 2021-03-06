package sk.p1ro.wype.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import sk.p1ro.wype.R;
import sk.p1ro.wype.model.FolderModel;

public class MainTVActivity extends Activity implements CommonFunctionality {

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

        int size = updateList(this, adapter, true);
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
        onActivityResult(this, adapter, requestCode, resultCode, intent);
    }
}

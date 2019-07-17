package sk.p1ro.wype.model;

import android.os.Build;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;
import sk.p1ro.wype.R;

public class FolderModel extends AbstractFlexibleItem<FolderModel.MyViewHolder> {

    private String path;

    public FolderModel(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FolderModel) {
            String p = ((FolderModel) o).path;
            return p != null && p.equals(path);
        }
        return false;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.folder_view;
    }

    @Override
    public MyViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new MyViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, MyViewHolder holder, int position, List<Object> payloads) {
        holder.textView.setText(path);

        holder.menu.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.add(Menu.NONE, 1, position, "Edit");
            menu.add(Menu.NONE, 2, position, "Delete");
        });

        holder.menu.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                holder.menu.showContextMenu();
            } else {
                holder.menu.showContextMenu(holder.menu.getX(), holder.menu.getY());
            }
        });
    }

    public String gePath() {
        return path;
    }

    class MyViewHolder extends FlexibleViewHolder {

        TextView textView;
        ImageButton menu;

        MyViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            textView = view.findViewById(R.id.textView2);
            menu = view.findViewById(R.id.imageView);
        }
    }
}

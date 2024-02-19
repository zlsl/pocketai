package ru.zlsl.pocketai.adapters;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ru.zlsl.pocketai.App;
import ru.zlsl.pocketai.EditTemplateActivity;
import ru.zlsl.pocketai.R;


public class AdapterTemplates extends RecyclerView.Adapter<AdapterTemplates.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> TemplatesList;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    private AdapterTemplates.OnSelectListener onSelectListener;

    public interface OnSelectListener {
        void onSelectListener();
    }

    public void setOnSelectListener(AdapterTemplates.OnSelectListener listener) {
        onSelectListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_title;
        final ImageButton ib_edit;

        public ViewHolder(View v) {
            super(v);
            p_title = v.findViewById(R.id.tv_template_title);
            ib_edit = v.findViewById(R.id.ib_edit);
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterTemplates(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        TemplatesList = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterTemplates.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_template, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                FileWriter out = new FileWriter(new File(App.getContext().getFilesDir(), Math.random() + "_" + Objects.requireNonNull(TemplatesList.get(holder.getAdapterPosition()).get("filename")).toString().replace(".pai", ".pad")));

                FileInputStream fis = App.getContext().openFileInput(Objects.requireNonNull(TemplatesList.get(holder.getAdapterPosition()).get("filename")).toString());
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader reader = new BufferedReader(isr);

                String line;
                while ((line = reader.readLine()) != null) {
                    out.write(line + "\n");
                }
                reader.close();
                isr.close();
                fis.close();
                out.close();
                onSelectListener.onSelectListener();
            } catch (Exception e) {
                Log.e("AT", "eee");
                e.printStackTrace();
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            try {
                new MaterialAlertDialogBuilder(v.getContext())
                        .setNegativeButton("Нет", null)
                        .setPositiveButton("Да", (dialog, which) -> {
                            App.getContext().deleteFile(Objects.requireNonNull(TemplatesList.get(holder.getAdapterPosition()).get("filename")).toString());
                            TemplatesList.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                        })
                        .setTitle("Удалить шаблон?").show();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
        holder.ib_edit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("title", Objects.requireNonNull(TemplatesList.get(holder.getAdapterPosition()).get("title")).toString());
            bundle.putString("filename", Objects.requireNonNull(TemplatesList.get(holder.getAdapterPosition()).get("filename")).toString());
            Intent intent = new Intent(App.getContext(), EditTemplateActivity.class);
            intent.putExtras(bundle);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            App.getContext().startActivity(intent);
        });
        try {
            holder.p_title.setText(Objects.requireNonNull(TemplatesList.get(holder.getAdapterPosition()).get("title")).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return TemplatesList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}
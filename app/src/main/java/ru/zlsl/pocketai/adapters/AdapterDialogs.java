package ru.zlsl.pocketai.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ru.zlsl.pocketai.App;
import ru.zlsl.pocketai.ChatActivity;
import ru.zlsl.pocketai.R;
import ru.zlsl.pocketai.controls.MLRoundedImageView;


public class AdapterDialogs extends RecyclerView.Adapter<AdapterDialogs.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> DialogsList;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_name;
        final MLRoundedImageView iv_author_avatar;

        public ViewHolder(View v) {
            super(v);
            p_name = v.findViewById(R.id.tv_character_name);
            iv_author_avatar = v.findViewById(R.id.iv_character_avatar);
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterDialogs(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        DialogsList = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterDialogs.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_dialog2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("filename", Objects.requireNonNull(DialogsList.get(holder.getAdapterPosition()).get("filename")).toString());
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        holder.itemView.setOnLongClickListener(v -> {

            try {
                new MaterialAlertDialogBuilder(v.getContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
                        .setNegativeButton("Нет", null)
                        .setPositiveButton("Да", (dialog, which) -> {
                            App.getContext().deleteFile(Objects.requireNonNull(DialogsList.get(holder.getAdapterPosition()).get("filename")).toString());
                            DialogsList.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                        })
                        .setTitle("Удалить диалог?").show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });

        try {
            holder.p_name.setText(Objects.requireNonNull(DialogsList.get(holder.getAdapterPosition()).get("title")).toString());

            if (DialogsList.get(holder.getAdapterPosition()).containsKey("avatar_url")) {
                if (!Objects.requireNonNull(DialogsList.get(holder.getAdapterPosition()).get("avatar_url")).toString().isEmpty()) {
                    try {
                        Picasso.with(context)
                                .load(Objects.requireNonNull(DialogsList.get(holder.getAdapterPosition()).get("avatar_url")).toString())
                                .into(holder.iv_author_avatar);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return DialogsList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}
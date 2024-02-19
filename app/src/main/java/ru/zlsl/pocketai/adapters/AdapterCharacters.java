package ru.zlsl.pocketai.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

import ru.zlsl.pocketai.App;
import ru.zlsl.pocketai.R;


public class AdapterCharacters extends RecyclerView.Adapter<AdapterCharacters.ViewHolder> implements View.OnClickListener {

    private final Context context;

    private AdapterCharacters.OnAvatarListener onAvatarListener;

    public interface OnAvatarListener {
        void onAvatarListener(String name);
    }

    public void setOnAvatarListener(AdapterCharacters.OnAvatarListener listener) {
        onAvatarListener = listener;
    }

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_name;
        final AppCompatImageView iv_author_avatar;

        public ViewHolder(View v) {
            super(v);
            p_name = v.findViewById(R.id.tv_character_name);
            iv_author_avatar = v.findViewById(R.id.iv_character_avatar);
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterCharacters(Context context) {
        this.context = context;
    }

    @NotNull
    @Override
    public AdapterCharacters.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        try {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_character, parent, false);
            return new ViewHolder(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.iv_author_avatar.setOnClickListener(v -> onAvatarListener.onAvatarListener(Objects.requireNonNull(App.Characters.get(holder.getAdapterPosition()).name).toString()));

        holder.itemView.setOnLongClickListener(v -> {
            try {
                new MaterialAlertDialogBuilder(v.getContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
                        .setNegativeButton("Нет", null)
                        .setPositiveButton("Да", (dialog, which) -> {
                            App.getContext().deleteFile(Objects.requireNonNull(App.Characters.get(holder.getAdapterPosition()).name) + ".jpg");
                            App.getContext().deleteFile(Objects.requireNonNull(App.Characters.get(holder.getAdapterPosition()).name) + ".jpeg");
                            App.getContext().deleteFile(Objects.requireNonNull(App.Characters.get(holder.getAdapterPosition()).name) + ".png");
                            App.getContext().deleteFile(Objects.requireNonNull(App.Characters.get(holder.getAdapterPosition()).name) + ".webp");
                            App.Characters.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                        })
                        .setTitle("Удалить персонажа?").show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });

        try {
            String name = Objects.requireNonNull(App.Characters.get(holder.getAdapterPosition()).name);
            Log.e("Update chi", "pos: " + holder.getAdapterPosition());
            holder.p_name.setText(name);

            String file_name = name + ".jpg";

            File avatar = new File(App.getContext().getFilesDir(), file_name);
            if (!avatar.exists()) {
                file_name = name + ".jpeg";
                avatar = new File(App.getContext().getFilesDir(), file_name);
            }
            if (!avatar.exists()) {
                file_name = name + ".png";
                avatar = new File(App.getContext().getFilesDir(), file_name);
            }
            if (!avatar.exists()) {
                file_name = name + ".webp";
                avatar = new File(App.getContext().getFilesDir(), file_name);
            }
            if (avatar.exists()) {
                Log.i("chi", file_name);
                try {
                    Picasso.with(context)
                            .load(avatar)
                            .error(R.drawable.default_avatar1)
                            .placeholder(R.drawable.default_avatar1)
                            .into(holder.iv_author_avatar);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.w("Avatar", "none");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return App.Characters.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}
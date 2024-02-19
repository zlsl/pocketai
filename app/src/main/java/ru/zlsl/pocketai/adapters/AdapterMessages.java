package ru.zlsl.pocketai.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;

import ru.zlsl.pocketai.App;
import ru.zlsl.pocketai.R;
import ru.zlsl.pocketai.controls.MLRoundedImageView;
import ru.zlsl.pocketai.types.Message;


public class AdapterMessages extends RecyclerView.Adapter<AdapterMessages.ViewHolder> implements View.OnClickListener {


    private static final int CM_USER = 0, CM_CHAR = 1, CM_BG = 3;

    private final List<Message> MessagesList;
    private final Context context;

    private static boolean working = false;

    @Override
    public void onClick(View view) {

    }

    public void Lock() {
        working = true;
    }

    public void UnLock() {
        working = false;
    }

    private OnRegenerateListener onRegenerateListener;

    public interface OnRegenerateListener {
        void onRegenerateListener(boolean self);
    }

    public void setOnRegenerateListener(OnRegenerateListener listener) {
        onRegenerateListener = listener;
    }

    private OnDeleteListener onDeleteListener;

    public interface OnDeleteListener {
        void onDeleteListener(int index);
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        onDeleteListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_name;
        final TextView p_message;
        final LinearLayout bg;
        final MLRoundedImageView iv_character_avatar;

        public ViewHolder(View v) {
            super(v);
            p_name = v.findViewById(R.id.tv_character_name);
            p_message = v.findViewById(R.id.tv_message);
            iv_character_avatar = v.findViewById(R.id.iv_character_avatar);
            bg = v.findViewById(R.id.bg);
        }
    }

    public AdapterMessages(Context context, List<Message> data) {
        this.context = context;
        MessagesList = data;
    }

    @Override
    public int getItemViewType(int position) {
        return switch (MessagesList.get(position).type) {
            case BACKGROUND -> CM_BG;
            case USER -> CM_USER;
            case CHARACTER -> CM_CHAR;
        };
    }

    @NotNull
    @Override
    public AdapterMessages.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == CM_BG) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_background, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message2, parent, false);
        }

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            if (working) {
                return;
            }
            try {
                if (onRegenerateListener != null) {
                    onRegenerateListener.onRegenerateListener(MessagesList.get(holder.getAdapterPosition()).name.equals("Я"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (working) {
                return false;
            }
            try {
                if (onDeleteListener != null) {
                    onDeleteListener.onDeleteListener(position);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });


        String name = Objects.requireNonNull(Objects.requireNonNull(MessagesList.get(holder.getAdapterPosition()).name));
        try {
            if (holder.p_name != null) {
                holder.p_name.setText(Objects.requireNonNull(MessagesList.get(holder.getAdapterPosition()).name));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int color = Color.TRANSPARENT;
                    Drawable background = holder.bg.getBackground();
                    if (background instanceof ColorDrawable) {
                        color = ((ColorDrawable) background).getColor();
                    }

                    holder.p_name.setTextColor(App.GetColor(name, Color.valueOf(color)).toArgb());
                }
            }
            holder.p_message.setText(FormatContent(Objects.requireNonNull(MessagesList.get(holder.getAdapterPosition()).content)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (!name.isEmpty()) {
                holder.iv_character_avatar.setOnLongClickListener(v -> {
                    App.NewCharacter(name);
                    Toast.makeText(App.getContext(), "Character added", Toast.LENGTH_LONG).show();

                    return true;
                });

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
                    try {
                        Picasso.with(context)
                                .load(avatar)
                                .placeholder(R.drawable.default_avatar1)
                                .error(R.drawable.default_avatar1)
                                .into(holder.iv_character_avatar);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (name.equals(App.modelParameters.UserName)) {
                        holder.iv_character_avatar.setImageResource(R.drawable.default_avatar1);
                    } else {
                        int ava;
                        if (App.IsFemale(name)) {
                            ava = R.drawable.avatar_female1;
                        } else {
                            ava = R.drawable.avatar_male1;
                        }
                        holder.iv_character_avatar.setImageResource(ava);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return MessagesList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    private String FormatContent(String content) {
        String[] lines = content.split("\n");
        StringBuilder tmp = new StringBuilder();
        for (String line : lines) {
            if (tmp.length() == 0) {
                tmp = new StringBuilder(line.trim());
            } else {
                tmp.append("\n\n").append(line.trim());
            }
        }
        return tmp.toString();
    }
}
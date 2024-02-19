package ru.zlsl.pocketai;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.zlsl.pocketai.adapters.AdapterMessages;
import ru.zlsl.pocketai.databinding.ActivityChatBinding;
import ru.zlsl.pocketai.types.Message;


public class ChatActivity extends AppCompatActivity {

    private static String api_model = "";
    private static String api_reason = "";
    private static int usage_prompt_tokens = 0;
    private static int usage_completion_tokens = 0;
    private static int usage_total_tokens = 0;


    private static final List<String> Characters = new ArrayList<>();

    private static String filename = "";
    private static String title = "";

    private static boolean working = false;

    ActivityChatBinding binding;

    private static final List<Message> MessagesList = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        LinearLayoutManager lm = new LinearLayoutManager(this);
        binding.rvMessages.setLayoutManager(lm);
        AdapterMessages rv_adapter = new AdapterMessages(this, MessagesList);
        binding.rvMessages.setAdapter(rv_adapter);
        ((AdapterMessages) Objects.requireNonNull(binding.rvMessages.getAdapter())).setOnRegenerateListener((boolean self) -> {
            if (!self) {
                MessagesList.remove(MessagesList.size() - 1);
                binding.rvMessages.post(() -> binding.rvMessages.getAdapter().notifyItemRemoved(MessagesList.size()));
            }
            LoadMessages();
        });
        ((AdapterMessages) binding.rvMessages.getAdapter()).setOnDeleteListener((int position) -> {
            Log.i("Delete Message", "pos: " + position + ", size: " + MessagesList.size());
            try {
                if (MessagesList.size() > position) {
                    if (MessagesList.get(position).name.equals(App.modelParameters.UserName)) {
                        binding.eMessage.setText(MessagesList.get(position).content);
                    }
                    MessagesList.remove(position);
                    binding.rvMessages.post(() -> {
                        binding.rvMessages.getAdapter().notifyItemRemoved(position);
                        binding.rvMessages.getAdapter().notifyItemRangeChanged(position, MessagesList.size());
                    });
                } else {
                    if (MessagesList.get(MessagesList.size() - 1).name.equals(App.modelParameters.UserName)) {
                        binding.eMessage.setText(MessagesList.get(MessagesList.size() - 1).content);
                    }
                    MessagesList.remove(MessagesList.size() - 1);
                    binding.rvMessages.post(() -> binding.rvMessages.getAdapter().notifyItemRemoved(MessagesList.size() - 1));
                }

                SaveDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.bSend.setOnLongClickListener(view1 -> {
            FragmentManager fm = getSupportFragmentManager();
            InfoDialog id = new InfoDialog();
            id.show(fm, "");
            return true;
        });

        binding.bSend.setOnClickListener(view1 -> {
            if (binding.eMessage.getText().toString().isEmpty() || working) {
                return;
            }
            Message m = new Message();
            m.type = App.message_type.USER;
            m.name = App.modelParameters.UserName;
            m.content = binding.eMessage.getText().toString();
            if ((!m.content.endsWith(".")) && (!m.content.endsWith("!")) && (!m.content.endsWith("?"))) {
                m.content = m.content + ".";
            }
            m.content = Character.toUpperCase(m.content.charAt(0)) + m.content.substring(1);
            MessagesList.add(m);
            binding.eMessage.setText("");
            binding.rvMessages.getAdapter().notifyItemInserted(MessagesList.size() - 1);
            binding.rvMessages.smoothScrollToPosition(MessagesList.size() - 1);
            LoadMessages();
        });

        Intent intent = getIntent();
        filename = intent.getStringExtra("filename");
        title = intent.getStringExtra("title");


        LoadDialog(filename);

        binding.rvMessages.getAdapter().notifyDataSetChanged();
        binding.rvMessages.scrollToPosition(MessagesList.size() - 1);
    }

    private void AddMessage(String name, String content) {
        Message m = new Message();
        if (name.isEmpty()) {
            m.type = App.message_type.BACKGROUND;
        } else {
            if (name.equals(App.modelParameters.UserName)) {
                m.type = App.message_type.USER;
            } else {
                m.type = App.message_type.CHARACTER;
                AddCharacter(name);
            }
        }
        m.name = name;
        m.content = content;
        MessagesList.add(m);
    }

    private void LoadDialog(String filename) {
        MessagesList.clear();
        Characters.clear();
        try {
            FileInputStream fis = App.getContext().openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("*")) {
                    title = line.replace("*", "");
                } else {
                    if (!line.contains("|")) {
                        AddMessage("", line.trim().replace("{~}", "\n"));
                    } else {
                        String[] parts = line.split("\\|");
                        AddMessage(parts[0].trim(), parts[1].trim().replace("{~}", "\n"));
                    }
                }
            }

            bufferedReader.close();
            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void LoadMessages() {
        if (working) {
            return;
        }

        StartWait();
        try {
            new Thread(
                    () -> {
                        try {
                            Response response = App.httpclient.newCall(App.getJSONRequestBuilder(App.getAPIServer() + "/v1/completions", App.GetAPIObject(GetPrompt()))).execute();
                            ResponseBody body = response.body();
                            String bodyx = "";
                            if (body != null) {
                                bodyx = body.string();
                            }
                            if (response.code() != 200) {
                                Log.e("API ERROR", bodyx);
                                binding.rvMessages.post(() -> Toast.makeText(App.getContext(), "Server error", Toast.LENGTH_LONG).show());
                                StopWait();
                            } else {
                                Log.d("RESPONSE RAW", bodyx);
                                int last_size = MessagesList.size();

                                JSONObject js = new JSONObject(bodyx);
                                Log.d("API RESPONSE", js.toString().replace(",\"", ",\n\""));
                                JSONArray ja = js.getJSONArray("choices");

                                if (js.has("model")) {
                                    api_model = js.getString("model");
                                }
                                if (js.has("usage")) {
                                    JSONObject ju = js.getJSONObject("usage");
                                    usage_completion_tokens = ju.getInt("completion_tokens");
                                    usage_total_tokens = ju.getInt("total_tokens");
                                    usage_prompt_tokens = ju.getInt("prompt_tokens");
                                }
                                api_reason = ja.getJSONObject(0).getString("finish_reason");

                                String raw = GetRandomCharacter() + ": " + ja.getJSONObject(0).getString("text");
                                SplitResponse(raw);
                                StopWait();

                                int added = MessagesList.size() - last_size;

                                binding.rvMessages.post(() -> {
                                            binding.rvMessages.getAdapter().notifyItemRangeInserted(last_size - 1, added);
                                            binding.rvMessages.smoothScrollToPosition(MessagesList.size());
                                            SaveDialog();
                                        }
                                );
                            }
                            response.close();
                        } catch (Exception e) {
                            StopWait();
                            e.printStackTrace();
                        }
                    }
            ).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String GetMessageChar(String text) {
        if (text.contains(":")) {
            String name = text.split(":")[0].trim();
            if (name.length() > 32) {
                return "";
            }
            if (
                    (name.contains("сказал")) || (name.contains("говор")) || (name.contains("Он")) ||
                            (name.chars().filter(ch -> ch == ' ').count() > 1) || // only one space allowed
                            (name.contains(".")) || (name.contains(",")) || (name.contains("-"))
            ) {
                Log.i("GetMessageChar", "invalid new char, ignore char name");
                return "";
            }
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1).trim();
            if (App.modelParameters.FixedCharacters) {
                if (!Characters.contains(name)) { // new character
                    Log.i("SplitResponse", "Denied new character \"" + name + "\", replace with existing");
                    if (!name.equals(App.modelParameters.UserName)) {
                        name = GetRandomCharacter();
                        AddCharacter(name);
                    }
                }
            } else {
                if (!name.equals(App.modelParameters.UserName)) {
                    AddCharacter(name);
                    Log.i("SplitResponse", "Added new character \"" + name + "\"");
                }
            }

            return name;
        }
        return "";
    }

    private String GetMessageContent(String text) {
        Log.d("MessageContent", text);
        if (text.contains(":")) {
            text = text + " ";
            String tmp = text.split(":")[1].trim();

            tmp = App.Beautify(tmp);
            return tmp;
        }
        return App.Beautify(text);
    }

    private String GetPrompt() {
        StringBuilder sb = new StringBuilder();
        for (Message m : MessagesList) {
            switch (m.type) {
                case USER, CHARACTER -> {
                    if (!sb.toString().isEmpty()) {
                        sb.append("\n");
                    }
                    if (App.modelParameters.UseCharToken) {
                        sb.append(m.name).append(": <char>").append(m.content);
                    } else {
                        sb.append(m.name).append(": ").append(m.content);
                    }
                }
                default -> {
                    if (!sb.toString().isEmpty()) {
                        sb.append("\n");
                    }
                    sb.append(m.content);
                }
            }
        }
        return sb + "\n" + GetRandomCharacter() + ":";
    }

    private void SplitResponse(String source) {
        String name = "";
        StringBuilder content = new StringBuilder();
        String[] lines = source.split("\n");
        for (String line : lines) {
            if (!GetMessageChar(line).isEmpty()) {
                if (name.isEmpty() && (!name.equals(GetMessageChar(line)))) { // first char
                    name = GetMessageChar(line);
                    if ((!App.modelParameters.AllowOwn) && (name.equals(App.modelParameters.UserName))) {
                        Log.i("SplitResponse", "Found generated own message, stop on it");
                        return;
                    }
                    content = new StringBuilder(GetMessageContent(line));
                } else { // new char
                    if (!content.toString().trim().isEmpty()) {
                        AddMessage(name, content.toString().trim());
                        name = GetMessageChar(line);
                        if ((!App.modelParameters.AllowOwn) && (name.equals(App.modelParameters.UserName))) {
                            Log.i("SplitResponse", "Found generated own message, stop on it");
                            return;
                        }
                        content = new StringBuilder(GetMessageContent(line));
                    }
                }
            } else { // continue content
                String mc = GetMessageContent(line);
                if (!mc.isEmpty()) {
                    content.append("\n").append(mc);
                }
            }
        }
        if (content.length() > 0) {
            AddMessage(name, content.toString());
        }
    }

    private void StartWait() {
        binding.pbTyping.post(() -> {
            Log.d("WAIT", "start");
            binding.pbTyping.setVisibility(View.VISIBLE);
            ((AdapterMessages) Objects.requireNonNull(binding.rvMessages.getAdapter())).Lock();
            working = true;
        });
    }

    private void StopWait() {
        binding.pbTyping.post(() -> {
            Log.d("WAIT", "stop");
            binding.pbTyping.setVisibility(View.INVISIBLE);
            ((AdapterMessages) Objects.requireNonNull(binding.rvMessages.getAdapter())).UnLock();
            working = false;
        });
    }

    private void AddCharacter(String name) {
        if (!Characters.contains(name) && !name.equals(App.modelParameters.UserName)) {
            Log.i("Character", "Add new " + name);
            Characters.add(name);
            Log.d("Chars", Characters.toString());
        }
    }

    private String GetRandomCharacter() {
        if (Characters.size() == 0) {
            return "Она";
        }
        String c = Characters.get((int) ((Math.random() * (Characters.size()))));
        for (String cc : Characters) {
            if (MessagesList.get(MessagesList.size() - 1).content.contains(cc)) {
                c = cc;
            }
        }
        return c;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void SaveDialog() {
        try {
            FileWriter out = new FileWriter(new File(App.getContext().getFilesDir(), filename));
            out.write("*" + title + "*\n");
            for (Message m : MessagesList) {
                if (m.name.isEmpty()) {
                    out.write(m.content + "\n");
                } else {
                    out.write(m.name + "|" + m.content.replace("\n", "{~}") + "\n");
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class InfoDialog extends DialogFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialog_model_info, container, false);
            Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            getDialog().setTitle("Generation info");
            ((TextView) view.findViewById(R.id.tv_model_name)).setText(api_model);
            ((TextView) view.findViewById(R.id.tv_total_tokens)).setText(String.format("%d", usage_total_tokens));
            ((TextView) view.findViewById(R.id.tv_completion_tokens)).setText(String.format("%d", usage_completion_tokens));
            ((TextView) view.findViewById(R.id.tv_prompt_tokens)).setText(String.format("%d", usage_prompt_tokens));
            return view;
        }
    }

}
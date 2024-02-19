package ru.zlsl.pocketai;

import static ru.zlsl.pocketai.App.GetDialogInfo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.zlsl.pocketai.adapters.AdapterDialogs;


public class FragmentChats extends Fragment {

    private ArrayList<HashMap<String, Object>> DialogsList = new ArrayList<>();
    private RecyclerView rvDialogs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_list, container, false);

        rvDialogs = rootView.findViewById(R.id.rv_dialogs);

        rvDialogs.setHasFixedSize(true);
        rvDialogs.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        AdapterDialogs rv_adapter = new AdapterDialogs(rootView.getContext(), DialogsList);
        rvDialogs.setAdapter(rv_adapter);

        Check();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        LoadDialogs();
    }

    private void LoadDialogs() {
        DialogsList.clear();

        String[] templates = App.getContext().getFilesDir().list();
        for (String t : templates) {
            if (t.contains(".pad")) {
                Log.e("Dialog", t);
                HashMap m = new HashMap<>();
                m.put("filename", t);
                m.put("title", GetDialogInfo(t));
                DialogsList.add(m);
            }
        }

        rvDialogs.getAdapter().notifyDataSetChanged();
    }

    private void Check() {
        try {
            ExecutorService executors = Executors.newFixedThreadPool(1);
            Runnable runnable = () -> {

                Response response;
                try {
                    response = App.httpclient.newCall(App.getRequestBuilder("http://10.32.0.6:5000/v1/models")).execute();
                    String body;
                    ResponseBody bodyx = response.body();
                    if (bodyx != null) {
                        try {
                            body = bodyx.string();
                            Log.i("R", body);
                            response.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            executors.submit(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
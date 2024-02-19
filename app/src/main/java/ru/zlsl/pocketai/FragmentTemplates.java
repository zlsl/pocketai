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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import ru.zlsl.pocketai.adapters.AdapterTemplates;


public class FragmentTemplates extends Fragment {

    private ArrayList<HashMap<String, Object>> TemplatesList = new ArrayList<>();
    private RecyclerView rvTemplates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_template_list, container, false);

        rvTemplates = rootView.findViewById(R.id.rv_templates);
        rvTemplates.setHasFixedSize(true);
        rvTemplates.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        AdapterTemplates rv_adapter = new AdapterTemplates(rootView.getContext(), TemplatesList);
        rvTemplates.setAdapter(rv_adapter);

        ((AdapterTemplates) Objects.requireNonNull(rvTemplates.getAdapter())).setOnSelectListener(() -> ((MainActivity) requireActivity()).ShowChats());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        LoadTemplates();
    }

    private void LoadTemplates() {
        TemplatesList.clear();
        String[] templates = App.getContext().getFilesDir().list();
        for (String t : Objects.requireNonNull(templates)) {
            if (t.contains(".pai")) {
                Log.e("Template", t);
                if (App.getContext().getFileStreamPath(t).canRead()) {
                    Log.e("Template loaded", t);
                    HashMap m = new HashMap<>();
                    m.put("filename", t);
                    m.put("title", GetDialogInfo(t));
                    TemplatesList.add(m);
                } else {
                    Log.e("Cant read", t);
                }
            }
        }
        Objects.requireNonNull(rvTemplates.getAdapter()).notifyDataSetChanged();
    }

}
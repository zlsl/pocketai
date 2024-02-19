package ru.zlsl.pocketai;

import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.zlsl.pocketai.adapters.AdapterCharacters;
import ru.zlsl.pocketai.types.Character;


public class FragmentCharacters extends Fragment {

    String last_name = "";
    int SELECT_PICTURE = 200;

    AdapterCharacters rv_adapter;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    Log.i("Set character photo", selectedImageUri.toString());
                    try {
                        for (Character c : App.Characters) {
                            if (c.name.equals(last_name)) {
                                ContentResolver cr = App.getContext().getContentResolver();
                                MimeTypeMap mime = MimeTypeMap.getSingleton();

                                App.getContext().deleteFile(last_name + ".jpg");
                                App.getContext().deleteFile(last_name + ".jpeg");
                                App.getContext().deleteFile(last_name + ".png");
                                App.getContext().deleteFile(last_name + ".webp");
                                String filename = last_name + "." + mime.getExtensionFromMimeType(cr.getType(selectedImageUri));
                                Log.i("Photo save", filename);
                                App.saveFileFromUri(selectedImageUri, filename);
                                int idx = App.Characters.indexOf(last_name);
                                rv_adapter.notifyItemChanged(idx);
                                rv_adapter.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private RecyclerView rvCharacters;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_characters_list, container, false);

        rvCharacters = rootView.findViewById(R.id.rv_characters);
        rvCharacters.setHasFixedSize(true);
        rvCharacters.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        rv_adapter = new AdapterCharacters(rootView.getContext());
        rvCharacters.setAdapter(rv_adapter);

        rv_adapter.setOnAvatarListener(name -> {
            last_name = name;
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
        });

        String[] s = App.getContext().getFilesDir().list();
        for (String f : s) {
            Log.e("File", f);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
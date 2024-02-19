package ru.zlsl.pocketai;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;

import ru.zlsl.pocketai.databinding.FragmentParametersBinding;


public class FragmentParameters extends Fragment {

    FragmentParametersBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParametersBinding.inflate(getLayoutInflater());

        binding.edOwnName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    return;
                }
                App.modelParameters.UserName = s.toString().trim();
                App.modelParameters.SaveState();
            }
        });

        binding.swAllowOwn.setOnCheckedChangeListener((buttonView, isChecked) -> App.modelParameters.AllowOwn = isChecked);
        binding.swFixedCharacters.setOnCheckedChangeListener((buttonView, isChecked) -> App.modelParameters.FixedCharacters = isChecked);

        binding.swAllowOwn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            App.modelParameters.AllowOwn = isChecked;
            App.modelParameters.SaveState();
        });

        binding.swFixedCharacters.setOnCheckedChangeListener((buttonView, isChecked) -> {
            App.modelParameters.FixedCharacters = isChecked;
            App.modelParameters.SaveState();
        });

        binding.swCharToken.setOnCheckedChangeListener((buttonView, isChecked) -> {
            App.modelParameters.UseCharToken = isChecked;
            App.modelParameters.SaveState();
        });

        binding.slMaxNewTokens.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                binding.tvMaxNewTokens.setText(String.format("%d", Math.round(slider.getValue())));
                App.modelParameters.max_tokens = Math.round(slider.getValue());
                App.modelParameters.SaveState();
            }
        });

        binding.slTemperature.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                binding.tvTemperature.setText(String.format("%.2f", slider.getValue()));
                App.modelParameters.temperature = slider.getValue();
                App.modelParameters.SaveState();
            }
        });

        binding.slFrequencyPenalty.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                binding.tvFrequencyPenalty.setText(String.format("%.2f", slider.getValue()));
                App.modelParameters.frequency_penalty = slider.getValue();
                App.modelParameters.SaveState();
            }
        });

        binding.slRepetitionPenalty.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                binding.tvRepetitionPenalty.setText(String.format("%.2f", slider.getValue()));
                App.modelParameters.repetition_penalty = slider.getValue();
                App.modelParameters.SaveState();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        binding.edOwnName.setText(App.modelParameters.UserName);

        binding.swAllowOwn.setChecked(App.modelParameters.AllowOwn);
        binding.swFixedCharacters.setChecked(App.modelParameters.FixedCharacters);
        binding.swCharToken.setChecked(App.modelParameters.UseCharToken);

        binding.slMaxNewTokens.setValue(App.modelParameters.max_tokens);
        binding.tvMaxNewTokens.setText(String.format("%d", App.modelParameters.max_tokens));

        binding.slTemperature.setValue(App.modelParameters.temperature);
        binding.tvTemperature.setText(String.format("%.2f", App.modelParameters.temperature));

        binding.slRepetitionPenalty.setValue(App.modelParameters.repetition_penalty);
        binding.tvRepetitionPenalty.setText(String.format("%.2f", App.modelParameters.repetition_penalty));

        binding.slFrequencyPenalty.setValue(App.modelParameters.frequency_penalty);
        binding.tvFrequencyPenalty.setText(String.format("%.2f", App.modelParameters.frequency_penalty));
    }
}
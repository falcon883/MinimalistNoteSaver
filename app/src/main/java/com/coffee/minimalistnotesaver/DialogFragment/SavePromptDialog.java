package com.coffee.minimalistnotesaver.DialogFragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.coffee.minimalistnotesaver.databinding.SaveNotePromptBinding;

public class SavePromptDialog extends DialogFragment {

    private AlertDialog alertDialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        return alertDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SaveNotePromptBinding binding = SaveNotePromptBinding.inflate(getLayoutInflater());

        View dview = binding.getRoot();

        final EditText noteName = binding.noteName;

        noteName.setFilters(new InputFilter[]{new EmojiExcludeFilter()});
        noteName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (!(str.matches("[a-zA-Z0-9-_ ]*"))) {
                    str = removeIllegalChar(str).trim(); //trim whitespaces
                    noteName.setText(str);
                    noteName.setSelection(str.length());  //use only if u want to set cursor to end
                }
            }
        });

        binding.saveNote.setOnClickListener(v -> {
            if (TextUtils.isEmpty(noteName.getText())) {
                Toast.makeText(getActivity(), "Please Enter Note Name", Toast.LENGTH_SHORT).show();
            } else {
                ((SavePromptListener) getActivity()).onSaveYes(noteName.getText().toString());
            }
        });
        binding.cancelNote.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.setView(dview);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private String removeIllegalChar(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!(String.valueOf(str.charAt(i)).matches("[a-zA-Z0-9-_ ]*"))) {
                //as the callback is called for each character entered, we can return on first non-match
                //maybe show a short toast
                Toast.makeText(getActivity(), "Character not allowed", Toast.LENGTH_SHORT).show();
                return str.substring(0, i) + str.substring(i + 1);
            }
        }
        return str;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        if (!(context instanceof SavePromptListener)) {
            throw new ClassCastException(context.toString() + "must implement SavePromptListener");
        }
        super.onAttach(context);
    }

    public interface SavePromptListener {
        void onSaveYes(String noteName);
    }

    private class EmojiExcludeFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        }
    }
}
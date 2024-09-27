package com.japanese.words;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFragment extends Fragment {
    private Button buttonSubmit;
    private EditText editTextEnglish, editTextJapanese, editTextPronounce;
    private WordViewModel wordViewModel;



    public AddFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = requireActivity();

        //ahking, 获取view model.
        wordViewModel = ViewModelProviders.of(activity).get(WordViewModel.class);
        buttonSubmit = activity.findViewById(R.id.buttonSubmit);
        editTextEnglish = activity.findViewById(R.id.editTextEnglish);
        editTextJapanese = activity.findViewById(R.id.editTextJapanese);
        editTextPronounce = activity.findViewById(R.id.editTextPronounce);
        buttonSubmit.setEnabled(false);
        editTextEnglish.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextEnglish,0);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String english = editTextEnglish.getText().toString().trim();
                String japanese = editTextJapanese.getText().toString().trim();
                buttonSubmit.setEnabled(!english.isEmpty() && !japanese.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        editTextEnglish.addTextChangedListener(textWatcher);
        editTextJapanese.addTextChangedListener(textWatcher);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String english = editTextEnglish.getText().toString().trim();
                String japanese = editTextJapanese.getText().toString().trim();
                String pronounce = editTextPronounce.getText().toString().trim();
                Word word = new Word(english,japanese, pronounce);
                wordViewModel.insertWords(word);
                NavController navController = Navigation.findNavController(v);

                //ahking, 返回到之前的页面.
                navController.navigateUp();
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });
    }
}

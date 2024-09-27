package com.japanese.words;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
//ViewModel对整个应用程序的数据进行统一的管理, 整个类继承自AndroidViewModel.

public class WordViewModel extends AndroidViewModel {
    private WordRepository wordRepository;
    public WordViewModel(@NonNull Application application) {
        super(application);
        wordRepository = new WordRepository(application);
    }

    LiveData<List<Word>> getAllWordsLive() {
        return wordRepository.getAllWordsLive();
    }
    LiveData<List<Word>> findWordsWithPattern(String patten) {
        return wordRepository.findWordsWithPattern(patten);
    }

    void insertWords(Word... words) {
        wordRepository.insertWords(words);
    }
    void updateWords(Word... words) {
        wordRepository.updateWords(words);
    }
    void deleteWords(Word... words) {
        wordRepository.deleteWords(words);
    }
    void deleteAllWords() {
        wordRepository.deleteAllWords();
    }


}

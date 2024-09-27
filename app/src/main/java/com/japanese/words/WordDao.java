package com.japanese.words;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
//ahking, DAO database access object.
//对数据库的操作, 正删改查, DAO 这个类是操作入口类.
@Dao   // Database access object
public interface WordDao {
    @Insert
    void insertWords(Word... words);

    @Update
    void updateWords(Word... words);

    @Delete
    void deleteWords(Word... words);

    //ahking, 这个API的调用, 对应着注解中的sql语句.
    @Query("DELETE FROM WORD")
    void deleteAllWords();

    @Query("SELECT * FROM WORD ORDER BY ID DESC")
    //List<Word> getAllWords();
    LiveData<List<Word>>getAllWordsLive();

    @Query("SELECT * FROM WORD WHERE english_word LIKE :pattern ORDER BY ID DESC")
    LiveData<List<Word>>findWordsWithPattern(String pattern);

}

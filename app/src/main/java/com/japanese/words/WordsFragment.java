package com.japanese.words;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.japanese.words.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class WordsFragment extends Fragment {
    private WordViewModel wordViewModel;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter1, myAdapter2;
    private LiveData<List<Word>> filteredWords;
    private static final String VIEW_TYPE_SHP = "view_type_shp";
    private static final String IS_USING_CARD_VIEW = "is_using_card_view";
    private List<Word> allWords;
    private boolean undoAction;
    private DividerItemDecoration dividerItemDecoration;


    public WordsFragment() {
        // Required empty public constructor
        //ahking, 显示菜单栏
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearData:
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle("清空数据");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wordViewModel.deleteAllWords();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create();
                builder.show();
                break;
            case R.id.switchViewType:
                SharedPreferences shp = requireActivity().getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
                boolean isUsingCardView = shp.getBoolean(IS_USING_CARD_VIEW, false);
                SharedPreferences.Editor editor = shp.edit();
                if (isUsingCardView) {
                    recyclerView.setAdapter(myAdapter1);
                    recyclerView.addItemDecoration(dividerItemDecoration);
                    editor.putBoolean(IS_USING_CARD_VIEW, false);
                } else {
                    recyclerView.setAdapter(myAdapter2);
                    recyclerView.removeItemDecoration(dividerItemDecoration);
                    editor.putBoolean(IS_USING_CARD_VIEW, true);
                }
                editor.apply();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setMaxWidth(1000);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //ahking, 类似这种方法, return true和false的区别是, 如果你这里把这个事件处理完了, 不需要继续传递下去就return true, 如果
                //还希望系统把这个事件继续传递下去让其他逻辑去处理的话, 就return false.
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String pattern = newText.trim();
                //ahking, 先移除之前在onActivityCreated中添加的observer给移除掉.
                filteredWords.removeObservers(getViewLifecycleOwner());
                filteredWords = wordViewModel.findWordsWithPattern(pattern);
                //返回的是live data. 这里可以给它添加一个观察.
                filteredWords.observe(getViewLifecycleOwner(), new Observer<List<Word>>() {
                    @Override
                    public void onChanged(List<Word> words) {
                        int temp = myAdapter1.getItemCount();
                        allWords = words;
                        if (temp != words.size()) {
                            //ahking, 这是listAdapter提供的方法, 等同于recyclerview.adapter一般会写一个setData()这样的方法来设置数据
                            //提供数据列表, 系统会进行差异化比较, 根据比对结果来刷新界面.
                            //也同样, 不需要调用notifyDataSetChanged这个方法了也.
                            myAdapter1.submitList(words);
                            myAdapter2.submitList(words);
                        }
                    }
                });
                //ahking, 类似这种方法, return true和false的区别是, 如果你这里把这个事件处理完了, 不需要继续传递下去就return true, 如果
                //还希望系统把这个事件继续传递下去让其他逻辑去处理的话, 就return false.
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_words, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        wordViewModel = ViewModelProviders.of(requireActivity()).get(WordViewModel.class);
        recyclerView = requireActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        myAdapter1 = new MyAdapter(false, wordViewModel);
        myAdapter2 = new MyAdapter(true, wordViewModel);

        //ahking, 当动画结束之后, 来刷新序列号.
        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public void onAnimationFinished(@NonNull RecyclerView.ViewHolder viewHolder) {
                super.onAnimationFinished(viewHolder);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (linearLayoutManager != null) {
                    int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
                    for (int i = firstPosition; i <= lastPosition; i++) {
                        MyAdapter.MyViewHolder holder = (MyAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                        if (holder != null) {
                            holder.textViewNumber.setText(String.valueOf(i + 1));

                        }
                    }
                }

            }
        });
        SharedPreferences shp = requireActivity().getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
        boolean isUsingCardView = shp.getBoolean(IS_USING_CARD_VIEW, false);
        dividerItemDecoration = new DividerItemDecoration(requireActivity(),DividerItemDecoration.VERTICAL);

        if (isUsingCardView) {
            recyclerView.setAdapter(myAdapter2);
        } else {
            recyclerView.setAdapter(myAdapter1);
            recyclerView.addItemDecoration(dividerItemDecoration);
        }

        //recyclerView.setAdapter(myAdapter1);
        filteredWords = wordViewModel.getAllWordsLive();
        //ahking, 不用requireActivity()参数作为观察的owner, 不然搜索的时候会有闪动的问题.
        filteredWords.observe(getViewLifecycleOwner(), new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                int temp = myAdapter1.getItemCount();
                allWords = words;
                if (temp != words.size()) {
                    if (temp < words.size() && !undoAction) {
                        //ahking, 添加新数据后向下滚动200dp, 让用户感知到有新数据进来.
                        recyclerView.smoothScrollBy(0,-200);
                    }
                    undoAction = false;
                    myAdapter1.submitList(words);
                    myAdapter2.submitList(words);
                }
            }
        });

        //ahking, 这里处理item的滑动操作.
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override

            //拖拽排序的操作由于动作的和数据更新不同步，所以不能这样简单处理。拖拽速度快了就出错了。

            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                //ahking, 这里代码不放开, 是因为快速拖动的情况下, 数据会乱. 应该向b站那样, 有个单独的界面用来排序, 用户操作的时候, 只修改视图展示, 不做数据处理, 等用户
                //点击完成的时候, 再统一一次性对排序后的数据进行保存.
//                Word wordFrom = allWords.get(viewHolder.getAdapterPosition());
//                Word wordTo = allWords.get(target.getAdapterPosition());
//                int idTemp = wordFrom.getId();
//                wordFrom.setId(wordTo.getId());
//                wordTo.setId(idTemp);
//                wordViewModel.updateWords(wordFrom,wordTo);
//                myAdapter1.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
//                myAdapter2.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final Word wordToDelete = allWords.get(viewHolder.getAdapterPosition());
                wordViewModel.deleteWords(wordToDelete);
                Snackbar.make(requireActivity().findViewById(R.id.wordsFragmentView),"删除了一个词汇",Snackbar.LENGTH_SHORT)
                        .setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                undoAction = true;
                                wordViewModel.insertWords(wordToDelete);
                            }
                        })
                        .show();

            }
            //在滑动的时候，画出浅灰色背景和垃圾桶图标，增强删除的视觉效果

            Drawable icon = ContextCompat.getDrawable(requireActivity(),R.drawable.ic_delete_forever_black_24dp);
            Drawable background = new ColorDrawable(Color.LTGRAY);
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;

                int iconLeft,iconRight,iconTop,iconBottom;
                int backTop,backBottom,backLeft,backRight;
                backTop = itemView.getTop();
                backBottom = itemView.getBottom();
                iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) /2;
                iconBottom = iconTop + icon.getIntrinsicHeight();
                if (dX > 0) {
                    backLeft = itemView.getLeft();
                    backRight = itemView.getLeft() + (int)dX;
                    background.setBounds(backLeft,backTop,backRight,backBottom);
                    iconLeft = itemView.getLeft() + iconMargin ;
                    iconRight = iconLeft + icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft,iconTop,iconRight,iconBottom);
                } else if (dX < 0){
                    backRight = itemView.getRight();
                    backLeft = itemView.getRight() + (int)dX;
                    background.setBounds(backLeft,backTop,backRight,backBottom);
                    iconRight = itemView.getRight()  - iconMargin;
                    iconLeft = iconRight - icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft,iconTop,iconRight,iconBottom);
                } else {
                    background.setBounds(0,0,0,0);
                    icon.setBounds(0,0,0,0);
                }
                background.draw(c);
                icon.draw(c);
            }

        }).attachToRecyclerView(recyclerView);

        FloatingActionButton floatingActionButton = requireActivity().findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_wordsFragment_to_addFragment);
            }
        });

    }

}

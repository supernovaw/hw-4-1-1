package ru.netology.lists;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListViewActivity extends AppCompatActivity {
    private static final String TITLE_KEY = "title";
    private static final String SUBTITLE_KEY = "subtitle";
    private static final String LENGTH_KEY = "length";
    private static final String EXCLUDED = "excluded";

    private ArrayList<Integer> excludedIndices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        readExcludedIndices(savedInstanceState);

        ListView list = findViewById(R.id.list);

        List<HashMap<String, String>> originalValues = prepareContent();
        List<HashMap<String, String>> values = new ArrayList<>(originalValues);
        exclude(values);

        BaseAdapter listContentAdapter = createAdapter(values);

        list.setAdapter(listContentAdapter);
        list.setOnItemClickListener((adapterView, view, i, l) -> {
            excludedIndices.add(originalValues.indexOf(values.get(i))); // cancel shift
            values.remove(i);
            listContentAdapter.notifyDataSetChanged();
        });

        SwipeRefreshLayout refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(() -> {
            excludedIndices.clear();
            values.clear();
            values.addAll(prepareContent());
            listContentAdapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveExcludedIndices(outState);
    }

    private void readExcludedIndices(Bundle b) {
        excludedIndices = new ArrayList<>();
        if (b == null)
            return;
        String data = b.getString(EXCLUDED, "");
        if (data != null && !data.isEmpty()) {
            String[] split = data.split(";");
            for (String s : split)
                excludedIndices.add(Integer.parseInt(s));
        }
    }

    private void saveExcludedIndices(Bundle b) {
        StringBuilder toSave;
        if (excludedIndices.isEmpty())
            toSave = new StringBuilder();
        else {
            toSave = new StringBuilder(Integer.toString(excludedIndices.get(0)));
            for (int i = 1; i < excludedIndices.size(); i++)
                toSave.append(";").append(excludedIndices.get(i));
        }
        b.putString(EXCLUDED, toSave.toString());
    }

    private void exclude(List<HashMap<String, String>> list) {
        List<HashMap<String, String>> resultList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (!excludedIndices.contains(i))
                resultList.add(list.get(i));
        }
        list.clear();
        list.addAll(resultList);
    }

    @NonNull
    private BaseAdapter createAdapter(List<HashMap<String, String>> values) {
        String[] from = {TITLE_KEY, SUBTITLE_KEY, LENGTH_KEY};
        int[] to = {R.id.elementTitle, R.id.elementText, R.id.elementTextLength};
        return new SimpleAdapter(this, values, R.layout.list_element, from, to);
    }

    @NonNull
    private List<HashMap<String, String>> prepareContent() {
        String[] content = getString(R.string.large_text).split("\n\n");
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        for (int i = 0; i < content.length / 2; i++) {
            String text = content[i * 2 + 1];
            int wordsAmt = text.split(" ").length;
            String length = getString(R.string.text_length, wordsAmt, text.length());

            HashMap<String, String> map = new HashMap<>(2);
            map.put(TITLE_KEY, content[i * 2]);
            map.put(SUBTITLE_KEY, text);
            map.put(LENGTH_KEY, length);
            list.add(map);
        }
        return list;
    }
}

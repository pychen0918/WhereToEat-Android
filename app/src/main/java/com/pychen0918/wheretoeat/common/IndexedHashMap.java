package com.pychen0918.wheretoeat.common;

import android.content.Context;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pychen0918 on 2016/12/7.
 */

public class IndexedHashMap {
    private List<String> list;
    private HashMap<String, String> hashMap;

    public IndexedHashMap() {
        list = new ArrayList<>();
        hashMap = new HashMap<>();
    }

    // get key by index
    public String getKey(int index) {
        return list.get(index);
    }

    // get value by index
    public String get(int index) {
        return hashMap.get(getKey(index));
    }

    // get value by key
    public String get(String key) {
        return hashMap.get(key);
    }

    public void put(String key, String value) {
        list.add(key);
        hashMap.put(key, value);
    }

    public void clearAll() {
        list.clear();
        hashMap.clear();
    }

    public boolean containsKey(String key) {
        return hashMap.containsKey(key);
    }

    // get keys in order
    public List<String> getKeys() {
        List<String> items = new ArrayList<>();
        for (String key : list) {
            items.add(key);
        }
        return items;
    }

    // get values in order
    public List<String> getValues() {
        List<String> items = new ArrayList<>();
        for (String key : list) {
            items.add(hashMap.get(key));
        }
        return items;
    }

    public int getIndex(String key) {
        return list.indexOf(key);
    }

    public int size(){
        return list.size();
    }

    public static IndexedHashMap readXmlToIndexedHashMap(Context context, int resId) {
        XmlResourceParser xrp;
        IndexedHashMap indexedHashMap = new IndexedHashMap();

        xrp = context.getResources().getXml(resId);
        try {
            int eventType = xrp.getEventType();
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    if (xrp.getName().equals("entry")) {
                        String key = xrp.getAttributeValue(0);
                        String value = xrp.getAttributeValue(1);
                        indexedHashMap.put(key, value);
                    }
                }
                eventType = xrp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        xrp.close();
        return indexedHashMap;
    }
}

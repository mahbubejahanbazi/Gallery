package ir.mjahanbazi.mygallary;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class MyListAdapter<T> extends BaseAdapter {

    protected final List<T> list;

    public MyListAdapter() {
        this(new ArrayList<T>());
    }

    public MyListAdapter(List<T> list) {
        this.list = list;
    }

    public int getCount() {
        return list.size();
    }

    public T getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public List<T> getList() {
        return list;
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);

    public void add(int location, T object) {
        list.add(location, object);
        notifyDataSetChanged();
    }

    public void add(T object) {
        list.add(object);
        notifyDataSetChanged();
    }

    public void addAll(int location, Collection<T> collection) {
        list.addAll(location, collection);
        notifyDataSetChanged();
    }

    public void addAll(Collection<T> collection) {
        list.addAll(collection);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public void remove(int location) {
        list.remove(location);
        notifyDataSetChanged();
    }

    public void remove(T object) {
        list.remove(object);
        notifyDataSetChanged();
    }

    public void removeAll(Collection<T> collection) {
        list.removeAll(collection);
        notifyDataSetChanged();
    }

}

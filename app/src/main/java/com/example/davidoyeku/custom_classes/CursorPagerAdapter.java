package com.example.davidoyeku.custom_classes;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * I modified this code slightly, This class was not created by me, it was posted on making meetup
 http://making.meetup.com/post/25874320545/cursor-loader-view-pager-android *
 * it simply takes the data from a cursor row and passes it onto a fragment as an extra.
 */
public class CursorPagerAdapter<F extends Fragment> extends FragmentStatePagerAdapter {
    private final Class<? extends F> mFragmentClass;
    private Cursor mCursor;

    public CursorPagerAdapter(FragmentManager fm, Class<? extends F> fragmentClass, Cursor cursor) {
        super(fm);
        this.mFragmentClass = fragmentClass;
        this.mCursor = cursor;
    }

    @Override
    public F getItem(int position) {
        if (mCursor == null) // shouldn't happen
            return null;

        mCursor.moveToPosition(position);
        F frag;
        try {
            frag = mFragmentClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        Bundle args = new Bundle();
        for (int i = 0; i < mCursor.getColumnNames().length; ++i) {
            // Get the type of field so the arguments can be properly set.
            int type = mCursor.getType(i);
            switch (type) {
                case Cursor.FIELD_TYPE_BLOB:
                    args.putByteArray(mCursor.getColumnName(i), mCursor.getBlob(i));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    args.putFloat(mCursor.getColumnName(i), mCursor.getLong(i));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    args.putLong(mCursor.getColumnName(i), mCursor.getLong(i));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    args.putString(mCursor.getColumnName(i), mCursor.getString(i));
                    break;

                default:
                    break;
            }
        }
        frag.setArguments(args);
        return frag;
    }

    @Override
    public int getCount() {
        if (mCursor == null)
            return 0;
        else
            return mCursor.getCount();
    }

    public void changeCursor(Cursor c) {
        Cursor old = swapCursor(c);
        if (null != old) old.close();
    }

    public Cursor swapCursor(Cursor c) {
        if (mCursor == c)
            return null;
        Cursor oldCursor = mCursor;

        this.mCursor = c;
        notifyDataSetChanged();
        return oldCursor;
    }

    public Cursor getCursor() {
        return mCursor;
    }
}

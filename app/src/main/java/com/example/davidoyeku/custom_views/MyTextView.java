package com.example.davidoyeku.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.example.davidoyeku.m_diary.R;

/**
 * Created by DavidOyeku on 16/03/15.
 */
public class MyTextView extends TextView {
    /**
     * An <code>LruCache</code> for previously loaded typefaces.
     */
    private static LruCache<String, Typeface> sTypefaceCache =
            new LruCache<String, Typeface>(12);

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Get our custom attributes
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.TypefaceTextView, 0, 0);

        try {
            String typefaceName = a.getString(
                    R.styleable.TypefaceTextView_typeface);

            if (!isInEditMode() && !TextUtils.isEmpty(typefaceName)) {
                Typeface typeface = sTypefaceCache.get(typefaceName);

                if (typeface == null) {
                    typeface = Typeface.createFromAsset(context.getAssets(),
                            String.format("fonts/%s-Family.otf", typefaceName));

                    // Cache the Typeface object
                    sTypefaceCache.put(typefaceName, typeface);
                }
                setTypeface(typeface);

                // Note: This flag is required for proper typeface rendering
                setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
            }
        } finally {
            a.recycle();
        }
    }
}

package com.tummosoft.svgfx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class SVGImageView extends ImageView {
     private static Method setLayerTypeMethod = null;
    private boolean cache;          // Flag that declares whether drawable should be cached or not.
    private Object key;             // Key for mapCache. We need to use Object here because it can be a String, URI or ResourceId based on what is set.
    private static HashMap<Object, Drawable> mapCache = new HashMap<Object, Drawable>();        // This is the object that maps Keys to Drawables thereby implementing the Drawable map.

    {
        try {
            setLayerTypeMethod = View.class.getMethod("setLayerType", Integer.TYPE, Paint.class);
        } catch (NoSuchMethodException e) { /* do nothing */ }
    }


    public SVGImageView(Context context) {
        super(context);
    }


    public SVGImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(attrs, 0);
    }


    public SVGImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }


    private void init(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().getTheme()
                .obtainStyledAttributes(attrs, R.styleable.SVGImageView, defStyle, 0);
        try {
            String asset = a.getString(R.styleable.SVGImageView_asset);
            cache = a.getBoolean(R.styleable.SVGImageView_cache, false);

            // NOTE: By this logic svg:asset OVERRIDES svg:svg so that if both attributes are present only svg:asset is used.

            if (asset != null)      // This indicates that the user has explicitly asked for the asset to be used by using svg:asset rather than svg:svg attribute.
            {
                key = asset;
                setImageAsset(asset);
            }
            else
            {
                int resourceId = a.getResourceId(R.styleable.SVGImageView_svg, -1);
                if (resourceId != -1) {
                    key = resourceId;
                    setImageResource(resourceId);
                    return;
                }

                String url = a.getString(R.styleable.SVGImageView_svg);

                if (url != null)
                {
                    Uri uri = Uri.parse(url);
                    key = uri;

                    if (internalSetImageURI(uri))
                        return;

                    // Last chance, try loading it as an asset filename
                    key = url;
                    setImageAsset(url);
                }
            }

        } finally {
            a.recycle();
        }
    }


    /**
     * Load an SVG image from the given resource id.
     */
    @Override
    public void setImageResource(int resourceId) {
        try {
            if (cache && mapCache.containsKey(key))         // If cache is set and mapCache contains the key used we pull the drawable from the cache to use rather than from the svg
            {
                setDrawableFromCache();
            }
            else
            {
                SVG svg = SVG.getFromResource(getContext(), resourceId);
                setSoftwareLayerType();
                Drawable drawable = new PictureDrawable(svg.renderToPicture());
                if (cache) { cacheDrawable(drawable); }
                setImageDrawable(drawable);
            }
        } catch (SVGParseException e) {
            Log.w("SVGImageView", "Unable to find resource: " + resourceId, e);
        }
    }


    /**
     * Load an SVG image from the given resource URI.
     */
    @Override
    public void setImageURI(Uri uri) {
        internalSetImageURI(uri);
    }


    public void setImageAsset(String filename, boolean _cache)      // Method call when one wants to use an asset and enable caching along with it
    {
        cache = _cache;
        key = filename;         // Since a different asset may have been specified we update the 'key' associate with this View

        setImageAsset(filename);
    }


    /**
     * Load an SVG image from the given asset filename.
     */
    public void setImageAsset(String filename) {
        try {
            if (cache && mapCache.containsKey(key))         // If cache is set and mapCache contains the key used we pull the drawable from the cache to use rather than from the svg
            {
                setDrawableFromCache();
            }
            else
            {
                SVG svg = SVG.getFromAsset(getContext().getAssets(), filename);
                setSoftwareLayerType();
                Drawable drawable = new PictureDrawable(svg.renderToPicture());
                if (cache) { cacheDrawable(drawable); }
                setImageDrawable(drawable);
            }
        } catch (Exception e) {
            Log.w("SVGImageView", "Unable to find asset file: " + filename, e);
        }
    }


    /*
     * Attempt to set a picture from a Uri. Return true if it worked.
     */
    private boolean internalSetImageURI(Uri uri) {
        InputStream is = null;
        try {
            if (cache && mapCache.containsKey(key))
            {
                setDrawableFromCache();
            }
            else
            {
                is = getContext().getContentResolver().openInputStream(uri);
                SVG svg = SVG.getFromInputStream(is);
                setSoftwareLayerType();
                Drawable drawable = new PictureDrawable(svg.renderToPicture());
                if (cache) { cacheDrawable(drawable); }
                setImageDrawable(drawable);
            }
            return true;
        }
        catch (FileNotFoundException e) { return false; }
        catch (Exception e) {
            Log.w("ImageView", "Unable to open content: " + uri, e);
            return false;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) { /* do nothing */ }
        }
    }

    private void cacheDrawable(Drawable drawable)
    {
        mapCache.put(key, drawable);
    }


    private void setDrawableFromCache()
    {
        Drawable drawable = mapCache.get(key);
        setSoftwareLayerType();
        setImageDrawable(drawable);
    }


    /*
     * Use reflection to call an API 11 method from this library (which is configured with a minSdkVersion of 8)
     */
    private void setSoftwareLayerType() {
        if (setLayerTypeMethod == null)
            return;

        try {
            int LAYER_TYPE_SOFTWARE = View.class.getField("LAYER_TYPE_SOFTWARE").getInt(new View(getContext()));            // Abid Edit: Added this line for API < 10 since View.LAYER_TYPE_SOFTWARE was added in API 11
            setLayerTypeMethod.invoke(this, LAYER_TYPE_SOFTWARE, null);
        } catch (Exception e) {
            Log.w("SVGImageView", "Unexpected failure calling setLayerType", e);
        }
    }
}

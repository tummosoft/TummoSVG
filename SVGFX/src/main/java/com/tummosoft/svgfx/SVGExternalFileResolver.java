package com.tummosoft.svgfx;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import com.caverock.androidsvg.SVG.Style;

public abstract class SVGExternalFileResolver {
      public Typeface resolveFont(String fontFamily, int fontWeight, String fontStyle) {
        return null;
    }

    /**
     * Called by renderer to resolve image file references in &lt;image&gt; elements.
     * <p/>
     * Return a {@code Bitmap} instance, or null if you want the renderer to ignore
     * this image.
     * <p/>
     * Note that AndroidSVG does not attempt to cache Bitmap references.  If you want
     * them cached, for speed or memory reasons, you should do so yourself.
     *
     * @param filename the filename as provided in the xlink:href attribute of a &lt;image&gt; element.
     * @return an Android Bitmap object, or null if the image could not be found.
     */
    public Bitmap resolveImage(String filename) {
        return null;
    }

    /**
     * Called by renderer to determine whether a particular format is supported.  In particular,
     * this method is used in &lt;switch&gt; elements when processing {@code requiredFormats}
     * conditionals.
     *
     * @param mimeType A MIME type (such as "image/jpeg").
     * @return true if your {@code resolveImage()} implementation supports this file format.
     */
    public boolean isFormatSupported(String mimeType) {
        return false;
    }
}

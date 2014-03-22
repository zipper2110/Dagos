package com.dagos.threshold;

import com.dagos.graphics.Image;
import com.dagos.graphics.Mask;

/**
 * Created by Dmitry on 02.03.14.
 */
public interface SegmentationI {
    Mask getMask(Image image);
}

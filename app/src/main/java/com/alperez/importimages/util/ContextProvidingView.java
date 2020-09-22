package com.alperez.importimages.util;

import android.content.Context;

/**
 * Created by stanislav.perchenko on 15.09.2020 at 22:10.
 */
public interface ContextProvidingView {
    Context getContext();
    void close();
}

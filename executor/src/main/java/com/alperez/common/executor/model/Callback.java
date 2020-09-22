package com.alperez.common.executor.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alperez.common.error.AppError;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 22:12.
 */
public interface Callback<T> {

    void onSubscribe(@NonNull Disposable d);

    void onSuccess(@Nullable T result);

    void onError(@NonNull AppError reason);

    void onComplete(@NonNull Disposable d);
}

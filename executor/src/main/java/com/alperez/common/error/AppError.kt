package com.alperez.common.error

/**
 * Created by stanislav.perchenko on 22.09.2020 at 22:14.
 */
interface AppError {
    fun getInternalMessage(): String?
    fun getUserMessage(): String?
    fun getException(): Exception?
}

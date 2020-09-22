package com.alperez.common.error

import android.content.Context
import com.alperez.common.R
import java.io.IOException

/**
 * Created by stanislav.perchenko on 22.09.2020 at 22:15.
 */

/***************************************************************************************************
 *
 */
class LocalError private constructor() : AppError {

    companion object {
        fun create(ctx: Context?, e: Exception? ): LocalError {
            val err = LocalError()
            err.userMessage = ctx?.resources?.getString(R.string.msg_err_internal_app_error) ?: "Internal app error"
            err.internMessage = e?.message
            err.ex = e
            return err
        }

        fun create(ctx: Context, internalMessage: String? ): LocalError {
            val err = LocalError()
            err.userMessage = ctx.resources.getString(R.string.msg_err_internal_app_error)
            err.internMessage = internalMessage
            return err
        }
    }

    private var userMessage: String? = null
    private var internMessage: String? = null
    private var ex: Exception? = null


    override fun getInternalMessage(): String? {
        return internMessage
    }

    override fun getUserMessage(): String? {
        return userMessage
    }

    override fun getException(): Exception? {
        return ex
    }
}



/***************************************************************************************************
 *
 */
class IORestError private constructor(ex: IOException) : AppError {

    companion object {
        fun create(ctx: Context, ex: IOException): IORestError {
            val err = IORestError(ex)
            err.userMessage = ctx.resources.getString(R.string.msg_err_connection_problem)
            err.internMessage = ex.message
            return err
        }
    }

    private var userMessage: String? = null
    private var internMessage: String? = null
    private var ex: IOException

    init {
        this.ex = ex;
    }

    fun getIOException(): IOException {
        return ex;
    }

    override fun getInternalMessage(): String? {
        return internMessage
    }

    override fun getUserMessage(): String? {
        return userMessage
    }

    override fun getException(): Exception? {
        return ex;
    }
}


/***************************************************************************************************
 *
 */
class ServerRestError private constructor(usrMsg: String, httpCode: Int, httpMsg: String) : AppError {
    companion object {
        fun create(ctx: Context, httpCode: Int, httpMsg: String): ServerRestError {
            return ServerRestError(ctx.resources.getString(R.string.msg_err_server_error), httpCode, httpMsg)
        }
    }

    private val userMessage: String = usrMsg
    private val internMessage: String = httpMsg
    private val httpCode: Int = httpCode

    fun getHttpCode(): Int {
        return httpCode
    }

    fun getHttpMessage(): String {
        return internMessage
    }

    override fun getInternalMessage(): String? {
        return internMessage
    }

    override fun getUserMessage(): String? {
        return userMessage
    }

    override fun getException(): Exception? {
        return null
    }
}


/***************************************************************************************************
 *
 */
class ParseRestError private constructor(usrMsg: String, internMsg: String?, ex: Exception?) : AppError {

    companion object {
        fun create(ctx: Context, detailMsg: String?): ParseRestError {
            return ParseRestError(ctx.resources.getString(R.string.msg_err_api_wrong_data), detailMsg, null)
        }

        fun create(ctx: Context, ex: Exception): ParseRestError {
            return ParseRestError(ctx.resources.getString(R.string.msg_err_api_wrong_data), ex.message, ex)
        }
    }

    private val userMessage: String = usrMsg;
    private val internalMessage: String? = internMsg
    private val ex: Exception? = ex

    override fun getInternalMessage(): String? {
        return internalMessage
    }

    override fun getUserMessage(): String? {
        return userMessage
    }

    override fun getException(): Exception? {
        return ex
    }
}

/***************************************************************************************************
 *
 */
class ApiRestError private constructor(usrMsg: String, internMsg: String?) : AppError {

    companion object {
        fun createWithDefaultPrefix(ctx: Context, detailMsg: String?): ApiRestError {
            return ApiRestError(ctx.resources.getString(R.string.msg_err_server_error), detailMsg)
        }
        fun createNoPrefix(detailMsg: String): ApiRestError {
            return ApiRestError(detailMsg, null)
        }
    }

    private val userMessage: String = usrMsg
    private val internalMessage: String? = internMsg

    override fun getInternalMessage(): String? {
        return internalMessage
    }

    override fun getUserMessage(): String? {
        return userMessage
    }

    override fun getException(): Exception? {
        return null
    }

}

/***************************************************************************************************
 *
 */
class BadResponseError private constructor(usrMsg: String, internMsg: String?) : AppError {
    companion object {
        fun create(ctx: Context, detailMsg: String?): BadResponseError {
            return BadResponseError(ctx.resources.getString(R.string.msg_err_api_wrong_data), detailMsg)
        }
    }

    private val userMessage: String = usrMsg
    private val internalMessage: String? = internMsg

    override fun getInternalMessage(): String? {
        return internalMessage
    }

    override fun getUserMessage(): String? {
        return userMessage
    }

    override fun getException(): Exception? {
        return null
    }
}




















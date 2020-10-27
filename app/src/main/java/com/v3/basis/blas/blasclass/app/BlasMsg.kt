package com.v3.basis.blas.blasclass.app

import android.util.Log
import com.v3.basis.blas.R
import com.v3.basis.blas.blasclass.rest.BlasRest.Companion.context
import com.v3.basis.blas.blasclass.rest.BlasRestErrCode.Companion.AUTH_INVALID_TOKEN
import java.lang.Exception

open class BlasMsg() {

    companion object {
        val res = context.getResources();

        val msg = mapOf(
            1 to res.getString(R.string.token_invalid),
            2 to res.getString(R.string.token_expired),
            3 to res.getString(R.string.internal_error),
            4 to res.getString(R.string.already_logout),
            5 to res.getString(R.string.account_error),
            6 to res.getString(R.string.create_token_error),
            7 to res.getString(R.string.temp_user_error),
            8 to res.getString(R.string.user_lock_error),
            100 to res.getString(R.string.perm_no_error),
            200 to res.getString(R.string.not_found_record),
            201 to res.getString(R.string.add_record_error),
            202 to res.getString(R.string.update_record_error),
            203 to res.getString(R.string.delete_record_error),
            300 to res.getString(R.string.not_found_param),
            301 to res.getString(R.string.not_found_id),
            302 to res.getString(R.string.set_param_error),
            303 to res.getString(R.string.invalid_param),
            304 to res.getString(R.string.not_found_image),
            305 to res.getString(R.string.out_of_range),
            306 to res.getString(R.string.data_dupli_error),
            307 to res.getString(R.string.set_fx_error),
            308 to res.getString(R.string.set_refx_error),
            400 to res.getString(R.string.not_entry_takeout),
            401 to res.getString(R.string.diff_user),
            402 to res.getString(R.string.already_set),
            403 to res.getString(R.string.already_check),
            404 to res.getString(R.string.dont_takeout),
            405 to res.getString(R.string.fx_not_entry),
            406 to res.getString(R.string.already_takeout),
            407 to res.getString(R.string.not_entry_id),
            408 to res.getString(R.string.already_return),
            409 to res.getString(R.string.already_removal_rentry),
            410 to res.getString(R.string.not_entry_removal),
            411 to res.getString(R.string.not_removal_serial),
            412 to res.getString(R.string.already_removal_comp),
            500 to res.getString(R.string.image_cant_access),
            501 to res.getString(R.string.image_save_error),
            502 to res.getString(R.string.image_cant_del),
            900 to res.getString(R.string.apl_queue_save),
            901 to res.getString(R.string.apl_queue_max),
            1001 to res.getString(R.string.network_error),
            2001 to res.getString(R.string.json_parse_error),
            3001 to res.getString(R.string.file_read_error),
            10001 to res.getString(R.string.version_error)
        )
    }

    val androidMsg = mapOf(
        "getFail" to res.getString(R.string.other_recieve_error)
    )

    open fun getMessage(errorCode : Int, aplCode : Int ) : String? {

        var resMessage : String? = null

        if (BlasMsg.msg.contains(errorCode)) {
            resMessage = BlasMsg.msg[errorCode]
        }else{
            resMessage = res.getString(R.string.server_error, errorCode)
        }

        if (aplCode != 0){
            if (BlasMsg.msg.contains(aplCode)) {
                resMessage = resMessage + BlasMsg.msg[aplCode]
            }
        }

        return resMessage
    }


    open fun createErrorMessage(errorType:String): String? {
        val errorMessage = androidMsg[errorType]
        return errorMessage
    }


}
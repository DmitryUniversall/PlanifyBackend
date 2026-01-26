package com.planify.planifyspring.main.common.entities

data class ApplicationResponse<T>(
    val ok: Boolean,
    val appCode: Int,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun success(): ApplicationResponse<Nothing> {
            return ApplicationResponse(ok = true, appCode = 1000, message = "Success", data = null)
        }

        fun <T> success(data: T): ApplicationResponse<T> {
            return ApplicationResponse(ok = true, appCode = 1000, message = "Success", data = data)
        }
    }
}

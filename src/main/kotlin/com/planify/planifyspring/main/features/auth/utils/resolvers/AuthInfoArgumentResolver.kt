package com.planify.planifyspring.main.features.auth.utils.resolvers

import com.planify.planifyspring.main.features.auth.domain.exceptions.AuthorizationFailedHttpException
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthInfoArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(AuthInfo::class.java) && parameter.parameterType == AuthInfo::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        return webRequest.getAttribute(
            "AuthInfo",
            RequestAttributes.SCOPE_REQUEST
        ) ?: throw AuthorizationFailedHttpException("Authentication failed unexpectedly")
    }
}

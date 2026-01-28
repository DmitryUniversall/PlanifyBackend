package com.planify.planifyspring.main.features.profile.routing;

import com.planify.planifyspring.main.features.profile.domain.services.ProfilesService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{userId}/profile")
class ProfileFeatureController(
    private val profilesService: ProfilesService
) {

}

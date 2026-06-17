package com.constructionmanager.update

import retrofit2.http.GET
import retrofit2.http.Url

/** Fetches the self-hosted update manifest from an absolute, user-configurable URL. */
interface UpdateApiService {
    @GET
    suspend fun fetchManifest(@Url url: String): UpdateManifest
}

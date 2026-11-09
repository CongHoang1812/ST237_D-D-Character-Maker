package com.fantasy.oc.maker.customize.data.repository

import com.fantasy.oc.maker.customize.core.common.DispatcherProvider
import com.fantasy.oc.maker.customize.core.common.Prepared
import com.fantasy.oc.maker.customize.core.common.Resource
import com.fantasy.oc.maker.customize.core.common.resourceFlow
import com.fantasy.oc.maker.customize.data.model.CosplayChallenge
import com.fantasy.oc.maker.customize.data.remote.MakerRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface CosplayRepository {
    /** Xin đề mới (nút Random); đề này trở thành đề hiện tại. */
    fun randomChallenge(): Flow<Resource<CosplayChallenge>>

    /** Đề hiện tại (vào lại màn Cosplay vẫn ra đúng đề cũ); chưa có đề nào thì xin đề mới. */
    fun currentChallenge(): Flow<Resource<CosplayChallenge>> = randomChallenge()

    /** Bắt đầu tính trước đề Cosplay kế tiếp ở nền (gọi khi vào màn) để lần random gần như có ngay. */
    fun prepare() {}
}

/** Singleton: đề hiện tại và kết quả tính trước sống qua các lần vào màn (mất khi process bị kill). */
@Singleton
class CosplayRepositoryImpl @Inject constructor(
    private val remote: MakerRemoteDataSource,
    private val dispatchers: DispatcherProvider,
) : CosplayRepository {

    private val prepared = Prepared(CoroutineScope(SupervisorJob() + dispatchers.io)) { remote.getRandomCosplayChallenge() }

    override fun prepare() = prepared.prepare()

    @Volatile
    private var current: CosplayChallenge? = null

    override fun randomChallenge() = resourceFlow { prepared.take().also { current = it } }.flowOn(dispatchers.io)

    override fun currentChallenge(): Flow<Resource<CosplayChallenge>> {
        val challenge = current?.takeIf { hasReferenceImage(it) } ?: return randomChallenge()
        return flowOf(Resource.Success(challenge))
    }

    /** Ảnh tham chiếu nằm trong cacheDir, hệ thống có thể xoá: khi đó không dùng lại đề để tránh ảnh trắng. */
    private fun hasReferenceImage(challenge: CosplayChallenge): Boolean {
        val file = File(challenge.referenceImageUrl)
        return !file.isAbsolute || file.exists()
    }
}

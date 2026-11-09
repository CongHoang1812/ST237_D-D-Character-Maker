package com.fantasy.oc.maker.customize.data.repository

import com.fantasy.oc.maker.customize.core.common.DispatcherProvider
import com.fantasy.oc.maker.customize.core.common.Prepared
import com.fantasy.oc.maker.customize.core.common.Resource
import com.fantasy.oc.maker.customize.core.common.resourceFlow
import com.fantasy.oc.maker.customize.data.model.LuckyRollResult
import com.fantasy.oc.maker.customize.data.remote.MakerRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface LuckyRollRepository {
    fun roll(): Flow<Resource<LuckyRollResult>>

    /** Bắt đầu tính trước Lucky roll kế tiếp ở nền (gọi khi vào màn) để lần random gần như có ngay. */
    fun prepare() {}

    /** Hiện lại đúng kết quả đã random. Ảnh ghép đã bị xoá khỏi cache thì ghép lại từ config của nó. */
    fun restore(result: LuckyRollResult): Flow<Resource<LuckyRollResult>> = flowOf(Resource.Success(result))
}

/** Singleton: kết quả tính trước sống qua các lần vào màn. */
@Singleton
class LuckyRollRepositoryImpl @Inject constructor(
    private val remote: MakerRemoteDataSource,
    private val dispatchers: DispatcherProvider,
) : LuckyRollRepository {

    private val prepared = Prepared(CoroutineScope(SupervisorJob() + dispatchers.io)) { remote.getLuckyRoll() }

    override fun prepare() = prepared.prepare()

    override fun roll() = resourceFlow { prepared.take() }.flowOn(dispatchers.io)

    override fun restore(result: LuckyRollResult): Flow<Resource<LuckyRollResult>> = flow {
        val file = File(result.imageUrl)
        if (!file.isAbsolute || file.exists()) {
            emit(Resource.Success(result)) // không qua Loading: quay lại app không bị nháy skeleton
        } else {
            emitAll(resourceFlow { remote.composeLuckyRoll(result.config) })
        }
    }.flowOn(dispatchers.io)
}

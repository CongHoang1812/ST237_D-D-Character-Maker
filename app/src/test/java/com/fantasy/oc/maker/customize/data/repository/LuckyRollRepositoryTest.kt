package com.fantasy.oc.maker.customize.data.repository

import com.fantasy.oc.maker.customize.core.common.DispatcherProvider
import com.fantasy.oc.maker.customize.core.common.Resource
import com.fantasy.oc.maker.customize.data.model.CharacterConfig
import com.fantasy.oc.maker.customize.data.model.LuckyRollResult
import com.fantasy.oc.maker.customize.data.remote.MakerRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class LuckyRollRepositoryTest {

    private val config = CharacterConfig("t", emptyMap())
    private val composedConfigs = mutableListOf<CharacterConfig>()

    private val unusedRemote = Proxy.newProxyInstance(
        MakerRemoteDataSource::class.java.classLoader,
        arrayOf(MakerRemoteDataSource::class.java),
    ) { _, method, _ -> error("Không được gọi ${method.name}") } as MakerRemoteDataSource

    private val remote = object : MakerRemoteDataSource by unusedRemote {
        override suspend fun composeLuckyRoll(config: CharacterConfig): LuckyRollResult {
            composedConfigs += config
            return LuckyRollResult("recomposed", config)
        }
    }

    private val dispatchers = object : DispatcherProvider {
        override val io: CoroutineDispatcher = UnconfinedTestDispatcher()
        override val default: CoroutineDispatcher = io
    }

    private val repo = LuckyRollRepositoryImpl(remote, dispatchers)

    @Test
    fun restore_fileExists_returnsSameResultWithoutLoading() = runTest {
        val file = File.createTempFile("lucky_roll", ".png")
        try {
            val result = LuckyRollResult(file.absolutePath, config)
            assertEquals(listOf(Resource.Success(result)), repo.restore(result).toList())
            assertEquals(emptyList<CharacterConfig>(), composedConfigs)
        } finally {
            file.delete()
        }
    }

    @Test
    fun restore_fileDeleted_recomposesSameConfig() = runTest {
        val file = File.createTempFile("lucky_roll", ".png").apply { delete() }
        val emissions = repo.restore(LuckyRollResult(file.absolutePath, config)).toList()
        assertEquals(listOf(Resource.Loading, Resource.Success(LuckyRollResult("recomposed", config))), emissions)
        assertEquals(listOf(config), composedConfigs)
    }

    @Test
    fun restore_remoteUrl_returnsSameResult() = runTest {
        val result = LuckyRollResult("https://example.com/a.png", config)
        assertEquals(listOf(Resource.Success(result)), repo.restore(result).toList())
    }
}

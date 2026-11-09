package com.fantasy.oc.maker.customize.feature.cosplay

import com.fantasy.oc.maker.customize.core.common.DispatcherProvider
import com.fantasy.oc.maker.customize.core.common.NetworkMonitor
import com.fantasy.oc.maker.customize.core.common.Resource
import com.fantasy.oc.maker.customize.data.model.CharacterConfig
import com.fantasy.oc.maker.customize.data.model.CosplayChallenge
import com.fantasy.oc.maker.customize.data.remote.MakerRemoteDataSource
import com.fantasy.oc.maker.customize.data.repository.CosplayRepositoryImpl
import com.fantasy.oc.maker.customize.data.repository.SettingsRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class CosplayViewModelTest {

    private inline fun <reified T : Any> unused(): T = Proxy.newProxyInstance(
        T::class.java.classLoader,
        arrayOf(T::class.java),
    ) { _, method, _ -> error("Không được gọi ${method.name}") } as T

    /** Mỗi lần xin đề ra một ảnh khác; [imageFor] quyết định đường dẫn ảnh. */
    private var imageFor: (Int) -> String = { "https://example.com/$it.png" }
    private var produced = 0

    private val remote = object : MakerRemoteDataSource by unused<MakerRemoteDataSource>() {
        override suspend fun getRandomCosplayChallenge(): CosplayChallenge {
            produced++
            return CosplayChallenge("c$produced", imageFor(produced), "t", CharacterConfig("t", emptyMap()))
        }
    }

    private val dispatchers = object : DispatcherProvider {
        override val io: CoroutineDispatcher = UnconfinedTestDispatcher()
        override val default: CoroutineDispatcher = io
    }

    private val online = object : NetworkMonitor { override val isOnline = flowOf(true) }

    /** Repository là singleton: cùng một instance qua các lần vào màn. */
    private val repository = CosplayRepositoryImpl(remote, dispatchers)

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    /** Mỗi lần vào màn Cosplay từ Home là một ViewModel mới. */
    private fun TestScope.enterScreen() =
        CosplayViewModel(repository, unused<SettingsRepository>(), Gson(), online).also { vm ->
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.challenge.collect {} }
        }

    private fun shownId(vm: CosplayViewModel) = (vm.challenge.value as Resource.Success).data.id

    @Test
    fun reenterFromHome_keepsSameChallenge() = runTest {
        val first = shownId(enterScreen())
        val second = shownId(enterScreen())
        assertEquals(first, second)
    }

    @Test
    fun random_changesChallenge_andReenterKeepsTheNewOne() = runTest {
        val vm = enterScreen()
        val before = shownId(vm)

        vm.random()
        val afterRandom = shownId(vm)
        assertNotEquals(before, afterRandom)

        assertEquals(afterRandom, shownId(enterScreen()))
    }

    @Test
    fun reenter_referenceFileDeleted_getsNewChallenge() = runTest {
        imageFor = { "/khong-ton-tai/$it.png" }
        val first = shownId(enterScreen())
        assertNotEquals(first, shownId(enterScreen()))
    }
}

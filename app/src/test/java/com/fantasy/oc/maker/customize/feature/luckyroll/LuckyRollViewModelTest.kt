package com.fantasy.oc.maker.customize.feature.luckyroll

import androidx.lifecycle.SavedStateHandle
import com.fantasy.oc.maker.customize.core.common.NetworkMonitor
import com.fantasy.oc.maker.customize.core.common.Resource
import com.fantasy.oc.maker.customize.data.model.CharacterConfig
import com.fantasy.oc.maker.customize.data.model.LuckyRollResult
import com.fantasy.oc.maker.customize.data.model.PartSelection
import com.fantasy.oc.maker.customize.data.repository.LuckyRollRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LuckyRollViewModelTest {

    private val result = LuckyRollResult(
        imageUrl = "img",
        config = CharacterConfig("t", mapOf("hair" to PartSelection("h1", "red"))),
    )

    /** Chưa có API: Loading rồi treo mãi. */
    private val pendingRepo = object : LuckyRollRepository {
        override fun roll(): Flow<Resource<LuckyRollResult>> = flow {
            emit(Resource.Loading)
            awaitCancellation()
        }
    }

    private val successRepo = object : LuckyRollRepository {
        override fun roll(): Flow<Resource<LuckyRollResult>> = flowOf(Resource.Success(result))
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(repo: LuckyRollRepository, handle: SavedStateHandle = SavedStateHandle()) =
        LuckyRollViewModel(repo, Gson(), handle, onlineMonitor).also { vm ->
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        }

    @Test
    fun idleUntilRandom_thenLoadingForeverWithoutApi() = runTest {
        val vm = viewModel(pendingRepo)
        assertEquals(LuckyRollUiState.Idle, vm.state.value)
        vm.roll()
        assertEquals(LuckyRollUiState.Rolled(Resource.Loading), vm.state.value)
    }

    @Test
    fun success_configJsonRoundTrips_andRollSurvivesRecreate() = runTest {
        val handle = SavedStateHandle()
        val vm = viewModel(successRepo, handle)
        vm.roll()
        assertEquals(LuckyRollUiState.Rolled(Resource.Success(result)), vm.state.value)
        assertEquals(result.config, Gson().fromJson(vm.configJson(result), CharacterConfig::class.java))

        val recreated = viewModel(successRepo, handle)
        assertEquals(LuckyRollUiState.Rolled(Resource.Success(result)), recreated.state.value)
    }

    /** Mỗi lần roll() ra một ảnh khác, như repository thật. */
    private class CountingRepo : LuckyRollRepository {
        var rolls = 0
        override fun roll(): Flow<Resource<LuckyRollResult>> = flow {
            rolls++
            emit(Resource.Success(LuckyRollResult("img_$rolls", CharacterConfig("t", emptyMap()))))
        }
    }

    private fun success(vm: LuckyRollViewModel) =
        ((vm.state.value as LuckyRollUiState.Rolled).result as Resource.Success).data

    @Test
    fun backgroundLongerThanStopTimeout_keepsSameImage() = runTest {
        val repo = CountingRepo()
        val vm = LuckyRollViewModel(repo, Gson(), SavedStateHandle(), onlineMonitor)
        val firstVisit = launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        vm.roll()
        val before = success(vm)

        firstVisit.cancel()
        advanceTimeBy(60_000)
        runCurrent()
        val secondVisit = launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

        assertEquals(before, success(vm))
        assertEquals(1, repo.rolls)
        secondVisit.cancel()
    }

    @Test
    fun processRecreate_keepsSameImage() = runTest {
        val repo = CountingRepo()
        val handle = SavedStateHandle()
        val vm = viewModel(repo, handle)
        vm.roll()
        val before = success(vm)

        assertEquals(before, success(viewModel(repo, handle)))
        assertEquals(1, repo.rolls)
    }

    @Test
    fun retry_keepsSameImage_butRollChangesIt() = runTest {
        val repo = CountingRepo()
        val vm = viewModel(repo)
        vm.roll()
        val before = success(vm)

        vm.retry()
        assertEquals(before, success(vm))

        vm.roll()
        assertEquals("img_2", success(vm).imageUrl)
    }

    @Test
    fun retryWithoutResult_rolls() = runTest {
        val repo = CountingRepo()
        val vm = viewModel(repo)
        vm.retry()
        assertEquals("img_1", success(vm).imageUrl)
    }
}

private val onlineMonitor = object : NetworkMonitor { override val isOnline = flowOf(true) }

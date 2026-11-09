package com.fantasy.oc.maker.customize.feature.language

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.util.Consumer
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fantasy.oc.maker.customize.R
import com.fantasy.oc.maker.customize.core.common.AppLanguage
import com.fantasy.oc.maker.customize.core.ui.applyStatusBarPadding
import com.fantasy.oc.maker.customize.core.ui.base.BaseFragment
import com.fantasy.oc.maker.customize.core.ui.collectWhenStarted
import com.fantasy.oc.maker.customize.core.ui.setLightStatusBar
import com.fantasy.oc.maker.customize.core.ui.setOnSingleClickListener
import com.fantasy.oc.maker.customize.databinding.FragmentLanguageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Chọn ngôn ngữ. [ARG_FROM_SETTING] = false: lần đầu (→ intro), true: từ setting (→ quay lại).
 */
@AndroidEntryPoint
class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    private val viewModel: LanguageViewModel by viewModels()
    private val fromSetting by lazy { arguments?.getBoolean(ARG_FROM_SETTING, false) ?: false }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.content.applyStatusBarPadding()
        setLightStatusBar(!fromSetting)

        binding.ivBackground.isVisible = fromSetting
        binding.vOverlay.isVisible = fromSetting
        binding.btnBack.isVisible = fromSetting
        binding.tvTitle.isVisible = !fromSetting

        val adapter = LanguageAdapter(viewModel.selected.value) { viewModel.select(it) }
        binding.rvLanguages.adapter = adapter
        binding.btnBack.setOnSingleClickListener { findNavController().popBackStack() }
        binding.btnCheck.setOnSingleClickListener { viewModel.confirm() }

        collectWhenStarted(viewModel.selected) {
            adapter.selected = it
            binding.btnCheck.isVisible = it != null
        }
        collectWhenStarted(viewModel.confirmed) { onConfirmed(it) }
    }

    private suspend fun onConfirmed(language: AppLanguage) {
        applyLanguage(language)
        val nav = findNavController()
        if (fromSetting) {
            nav.popBackStack()
        } else {
            nav.navigate(R.id.action_language_to_intro)
        }
    }

    /**
     * Đổi locale rồi chờ resources cập nhật, để màn đích inflate đúng ngôn ngữ. Android 13+ báo về
     * bất đồng bộ qua hệ thống, máy cũ báo ngay trong lúc gọi nên phải đăng ký listener trước.
     */
    private suspend fun applyLanguage(language: AppLanguage) {
        if (AppLanguage.from(resources.configuration) == language) {
            AppLanguage.apply(language)
            return
        }
        val activity = requireActivity()
        val applied = CompletableDeferred<Unit>()
        val listener = Consumer<Configuration> {
            if (AppLanguage.from(it) == language) applied.complete(Unit)
        }
        activity.addOnConfigurationChangedListener(listener)
        try {
            AppLanguage.apply(language)
            withTimeoutOrNull(LOCALE_APPLY_TIMEOUT_MS) { applied.await() }
        } finally {
            activity.removeOnConfigurationChangedListener(listener)
        }
    }

    companion object {
        const val ARG_FROM_SETTING = "fromSetting"
        private const val LOCALE_APPLY_TIMEOUT_MS = 1000L
    }
}

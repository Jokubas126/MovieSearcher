package com.example.mmdb.navigation.configproviders

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.mmdb.R
import com.example.mmdb.extensions.requireNavController
import com.example.mmdb.navigation.ConfigProvider
import com.example.mmdb.navigation.NavigationController
import com.example.mmdb.navigation.actions.InnerDetailsAction
import com.example.mmdb.ui.details.DetailsFragmentConfig

class DetailsFragmentConfigProvider : ConfigProvider<DetailsFragmentConfig> {

    override fun config(fragment: Fragment): DetailsFragmentConfig {

        val navController = fragment.requireNavController()

        return DetailsFragmentConfig(
            loadInitialView = { idWrapper ->
                navController.resolveFragment(InnerDetailsAction.Overview(idWrapper))?.let { fragment ->
                    handleDetailsFragment(
                        fragmentManager = fragment.childFragmentManager,
                        fragment = fragment
                    )
                }
            },
            onBottomNavigationAction = { action ->
                navController.resolveFragment(action)?.let { fragment ->
                    handleDetailsFragment(
                        fragmentManager = fragment.childFragmentManager,
                        fragment = fragment,
                        animation = null //TODO figure out animation
                    )
                }
            }
        )
    }

    private fun handleDetailsFragment(
        fragmentManager: FragmentManager,
        fragment: Fragment,
        animation: NavigationController.Animation? = null
    ) {
        fragmentManager.beginTransaction().apply {
            animation?.let { anim ->
                setCustomAnimations(anim.enter, anim.exit, anim.popEnter, anim.popExit)
            }
            replace(R.id.detailsContentContainer, fragment)

        }.commit()
    }
}
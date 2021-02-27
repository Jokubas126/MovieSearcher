package com.example.mmdb.ui.details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.mmdb.R
import com.example.mmdb.navigation.NavigationController
import com.jokubas.mmdb.util.extensions.popSafe
import java.util.*

class DetailsNavigationController {

    val detailsNavigationStack: Stack<FragmentManager> = Stack()

    fun goTo(
        fragmentManager: FragmentManager,
        fragment: Fragment,
        animation: NavigationController.Animation?
    ) {
        fragmentManager.beginTransaction().apply {
            animation?.let { anim ->
                setCustomAnimations(anim.enter, anim.exit, anim.popEnter, anim.popExit)
            }
            replace(R.id.detailsContentContainer, fragment, fragment.tag)
        }.commit()
    }

    fun attachToNavigationController(fragmentManager: FragmentManager) {
        detailsNavigationStack.push(fragmentManager)
    }

    fun detachFromNavigationController(){
        detailsNavigationStack.popSafe()
    }
}
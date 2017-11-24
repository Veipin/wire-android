/**
 * Wire
 * Copyright (C) 2017 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient.appentry

import android.os.Bundle
import android.view.View.OnClickListener
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{FrameLayout, ImageButton}
import com.waz.zclient.appentry.CreateTeamFragment._
import com.waz.zclient.appentry.scenes._
import com.waz.zclient.pages.BaseFragment
import com.waz.zclient.{AppEntryController, FragmentHelper, OnBackPressedListener}
import com.waz.zclient.AppEntryController._
import com.waz.zclient.R
import com.waz.ZLog.ImplicitTag.implicitLogTag
import com.waz.zclient.utils.DefaultTransition

class CreateTeamFragment extends BaseFragment[Container] with FragmentHelper with OnBackPressedListener {

  lazy val appEntryController = inject[AppEntryController]

  var previousStage = Option.empty[AppEntryStage]

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View =
    inflater.inflate(R.layout.app_entry_fragment, container, false)

  override def onViewCreated(view: View, savedInstanceState: Bundle): Unit = {

    val backButton = findById[ImageButton](R.id.back_button)
    val container = findById[FrameLayout](R.id.container)

    backButton.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = appEntryController.createTeamBack()
    })

    appEntryController.entryStage.onUi { state =>
      val inflator = LayoutInflater.from(getActivity)
      val viewHolder = state match {
        case NoAccountState(FirstScreen) => FirstScreenViewHolder(inflator.inflate(R.layout.app_entry_scene, container, false))(getContext, this, injector)
        case NoAccountState(RegisterTeamScreen) => TeamNameViewHolder(inflator.inflate(R.layout.create_team_name_scene, container, false))(getContext, this, injector)
        case SetTeamEmail => SetEmailViewHolder(inflator.inflate(R.layout.set_email_scene, container, false))(getContext, this, injector)
        case VerifyTeamEmail => VerifyEmailViewHolder(inflator.inflate(R.layout.verify_email_scene, container, false))(getContext, this, injector)
        case SetUsersNameTeam => SetNameViewHolder(inflator.inflate(R.layout.set_name_scene, container, false))(getContext, this, injector)
        case SetPasswordTeam => SetPasswordViewHolder(inflator.inflate(R.layout.set_password_scene, container, false))(getContext, this, injector)
        case SetUsernameTeam => SetUsernameViewHolder(inflator.inflate(R.layout.set_username_scene, container, false))(getContext, this, injector)
      }

      val forward = previousStage.fold(true)(_.depth < state.depth)
      val sameDepth = previousStage.fold(false)(_.depth == state.depth)
      val transition = DefaultTransition()

      val previousViews = (0 until container.getChildCount).map(container.getChildAt)
      previousViews.foreach { pv =>
        if (!sameDepth)
          transition.outAnimation(pv, container, forward = forward).withEndAction(new Runnable {
            override def run(): Unit = container.removeView(pv)
          }).start()
        else
          container.removeView(pv)
      }

      container.addView(viewHolder.root)
      if (previousViews.nonEmpty && !sameDepth)
        transition.inAnimation(viewHolder.root, container, forward = forward).start()
      viewHolder.onCreate()

      if(state != NoAccountState(FirstScreen) && state != SetUsernameTeam)
        backButton.setVisibility(View.VISIBLE)
      else
        backButton.setVisibility(View.GONE)

      previousStage = Some(state)
    }

  }

  override def onBackPressed(): Boolean = {
    if (appEntryController.entryStage.currentValue.exists(_ != NoAccountState(FirstScreen))) {
      appEntryController.createTeamBack()
      true
    } else
      false
  }
}

object CreateTeamFragment {
  val TAG: String = classOf[CreateTeamFragment].getName

  def newInstance = new CreateTeamFragment

  trait Container

  case class ARunnable(f: () => Unit) extends Runnable {
    override def run(): Unit = f()
  }
}

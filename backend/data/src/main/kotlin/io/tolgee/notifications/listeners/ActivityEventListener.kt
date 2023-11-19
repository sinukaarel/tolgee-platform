/**
 * Copyright (C) 2023 Tolgee s.r.o. and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tolgee.notifications.listeners

import io.tolgee.events.OnProjectActivityStoredEvent
import io.tolgee.model.Notification
import io.tolgee.model.UserAccount
import io.tolgee.model.activity.ActivityRevision
import io.tolgee.notifications.NotificationService
import io.tolgee.service.project.ProjectService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ActivityEventListener(
  private val projectService: ProjectService,
  private val notificationService: NotificationService,
) {
  @EventListener
  fun onActivityRevision(e: OnProjectActivityStoredEvent) {
    val id = e.activityRevision.projectId ?: return

    val users = getUsersConcernedByRevision(e.activityRevision)
    val notification = Notification(
      projectService.get(id),
      e.activityRevision,
    )

    notificationService.dispatchNotification(notification, users)
  }

  private fun getUsersConcernedByRevision(revision: ActivityRevision): Set<UserAccount> {
    // TODO!!
    return emptySet()
  }
}
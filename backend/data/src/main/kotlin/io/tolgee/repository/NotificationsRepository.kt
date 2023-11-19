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

package io.tolgee.repository

import io.tolgee.model.Notification
import io.tolgee.model.Project
import io.tolgee.model.UserAccount
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NotificationsRepository : JpaRepository<Notification, Long> {
  fun findAllByMarkedDoneAtNullAndRecipient(recipient: UserAccount, pageable: Pageable): Collection<Notification>

  fun findAllByMarkedDoneAtNotNullAndRecipient(recipient: UserAccount, pageable: Pageable): Collection<Notification>

  fun findAllByUnreadTrueAndTypeAndProjectAndRecipientIn(
    type: Notification.NotificationType,
    project: Project,
    recipients: Collection<UserAccount>,
  ): Collection<Notification>

  fun countNotificationsByRecipientAndUnreadTrue(recipient: UserAccount): Int

  @Query("UPDATE Notification n SET n.unread = false WHERE n.recipient = ?1 AND n.id IN ?2")
  fun markAsRead(recipient: UserAccount, notifications: Collection<Long>)

  @Query("UPDATE Notification n SET n.unread = false WHERE n.recipient = ?1")
  fun markAllAsRead(recipient: UserAccount)

  @Query("UPDATE Notification n SET n.unread = true WHERE n.recipient = ?1 AND n.id IN ?2")
  fun markAsUnread(recipient: UserAccount, notifications: Collection<Long>)

  @Query("UPDATE Notification n SET n.unread = true WHERE n.recipient = ?1")
  fun markAllAsUnread(recipient: UserAccount)

  @Query("UPDATE Notification n SET n.unread = false, n.markedDoneAt = NOW() WHERE n.recipient = ?1 AND n.id IN ?2")
  fun markAsDone(recipient: UserAccount, notifications: Collection<Long>)

  @Query("UPDATE Notification n SET n.unread = false, n.markedDoneAt = NOW() WHERE n.recipient = ?1")
  fun markAllAsDone(recipient: UserAccount)

  @Query("UPDATE Notification n SET n.markedDoneAt = null WHERE n.recipient = ?1 AND n.id IN ?2")
  fun unmarkAsDone(recipient: UserAccount, notifications: Collection<Long>)

  @Query("DELETE FROM notifications WHERE marked_done_at < NOW() - INTERVAL '30 DAY'", nativeQuery = true)
  fun pruneOldNotifications() // Native query since HQL can't do "INTERVAL"
}
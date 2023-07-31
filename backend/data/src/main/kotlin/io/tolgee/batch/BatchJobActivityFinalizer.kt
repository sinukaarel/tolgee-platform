package io.tolgee.batch

import io.tolgee.activity.ActivityHolder
import io.tolgee.batch.events.OnBatchJobCancelled
import io.tolgee.batch.events.OnBatchJobFailed
import io.tolgee.batch.events.OnBatchJobSucceeded
import io.tolgee.batch.state.BatchJobStateProvider
import io.tolgee.fixtures.waitFor
import io.tolgee.util.Logging
import io.tolgee.util.logger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import javax.persistence.EntityManager

@Component
class BatchJobActivityFinalizer(
  private val entityManager: EntityManager,
  private val activityHolder: ActivityHolder,
  private val batchJobStateProvider: BatchJobStateProvider,
) : Logging {
  @EventListener(OnBatchJobSucceeded::class)
  fun finalizeActivityWhenJobSucceeded(event: OnBatchJobSucceeded) {
    finalizeActivityWhenJobCompleted(event.job)
  }

  @EventListener(OnBatchJobFailed::class)
  fun finalizeActivityWhenJobFailed(event: OnBatchJobFailed) {
    finalizeActivityWhenJobCompleted(event.job)
  }

  @EventListener(OnBatchJobCancelled::class)
  fun finalizeActivityWhenJobCancelled(event: OnBatchJobCancelled) {
    finalizeActivityWhenJobCompleted(event.job)
  }

  fun finalizeActivityWhenJobCompleted(job: BatchJobDto) {
    val activityRevision =
      activityHolder.activityRevision ?: throw IllegalStateException("Activity revision is not set")

    activityRevision.afterFlush = afterFlush@{
      waitForOtherChunksToComplete(job)
      val revisionIds = getRevisionIds(job.id)

      val activityRevisionIdToMergeInto = revisionIds.firstOrNull() ?: return@afterFlush
      revisionIds.remove(activityRevisionIdToMergeInto)

      mergeDescribingEntities(activityRevisionIdToMergeInto, revisionIds)
      mergeModifiedEntities(activityRevisionIdToMergeInto, revisionIds)
      deleteUnusedRevisions(revisionIds)
      setJobIdAndAuthorIdToRevision(activityRevisionIdToMergeInto, job)
    }
  }

  private fun waitForOtherChunksToComplete(job: BatchJobDto) {
    waitFor(20000) {
      val committedChunks = batchJobStateProvider.get(job.id).values
        .count { it.retry == false && it.transactionCommitted && it.status.completed }
      logger.debug("Waitinng for completed chunks ($committedChunks) to be equal to all other chunks count (${job.totalChunks - 1})")
      committedChunks == job.totalChunks - 1
    }
  }

  private fun setJobIdAndAuthorIdToRevision(activityRevisionIdToMergeInto: Long, job: BatchJobDto) {
    entityManager.createNativeQuery(
      """
        update activity_revision 
        set
         batch_job_chunk_execution_id = null, 
         batch_job_id = :jobId, 
         author_id = :authorId
        where id = :activityRevisionIdToMergeInto
        """
    )
      .setParameter("activityRevisionIdToMergeInto", activityRevisionIdToMergeInto)
      .setParameter("jobId", job.id)
      .setParameter("authorId", job.authorId)
      .executeUpdate()
  }

  private fun deleteUnusedRevisions(revisionIds: MutableList<Long>) {
    entityManager.createNativeQuery(
      """
          delete from activity_revision where id in (:revisionIds)
        """
    )
      .setParameter("revisionIds", revisionIds)
      .executeUpdate()
  }

  private fun mergeModifiedEntities(
    activityRevisionIdToMergeInto: Long,
    revisionIds: MutableList<Long>
  ) {
    entityManager.createNativeQuery(
      """
        update activity_modified_entity set activity_revision_id = :activityRevisionIdToMergeInto
        where activity_revision_id in (:revisionIds)
        """
    )
      .setParameter("activityRevisionIdToMergeInto", activityRevisionIdToMergeInto)
      .setParameter("revisionIds", revisionIds)
      .executeUpdate()
  }

  private fun mergeDescribingEntities(
    activityRevisionIdToMergeInto: Long,
    revisionIds: MutableList<Long>
  ) {
    removeDuplicityDescribingEntities(activityRevisionIdToMergeInto, revisionIds)

    entityManager.createNativeQuery(
      """
        update activity_describing_entity set activity_revision_id = :activityRevisionIdToMergeInto
        where activity_revision_id in (:revisionIds)
        """
    )
      .setParameter("activityRevisionIdToMergeInto", activityRevisionIdToMergeInto)
      .setParameter("revisionIds", revisionIds)
      .executeUpdate()
  }

  private fun removeDuplicityDescribingEntities(
    activityRevisionIdToMergeInto: Long,
    revisionIds: MutableList<Long>
  ) {
    entityManager.createNativeQuery(
      """
        delete from activity_describing_entity
        where (entity_class, entity_id) in
              (select entity_class, entity_id
               from activity_describing_entity
               where activity_revision_id in (:revisionIds)
                  or activity_revision_id = :activityRevisionIdToMergeInto
               group by entity_class, entity_id
               having count(*) > 1)
        and
            (activity_revision_id, entity_class, entity_id) not in (
            select min(activity_revision_id), entity_class, entity_id
                from activity_describing_entity
                where activity_revision_id in (:revisionIds)
                    or activity_revision_id = :activityRevisionIdToMergeInto
                group by entity_class, entity_id
                having count(*) > 1)
      """.trimIndent()
    )
      .setParameter("activityRevisionIdToMergeInto", activityRevisionIdToMergeInto)
      .setParameter("revisionIds", revisionIds)
      .executeUpdate()
  }

  private fun getRevisionIds(jobId: Long): MutableList<Long> = entityManager.createQuery(
    """
        select ar.id
        from ActivityRevision ar
        join ar.batchJobChunkExecution b
        where b.batchJob.id = :jobId
      """,
    Long::class.javaObjectType
  )
    .setParameter("jobId", jobId)
    .resultList
}

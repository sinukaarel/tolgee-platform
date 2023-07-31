package io.tolgee.batch.processors

import io.tolgee.batch.BatchJobDto
import io.tolgee.batch.BatchTranslationTargetItem
import io.tolgee.batch.ChunkProcessor
import io.tolgee.batch.request.PreTranslationByTmRequest
import io.tolgee.model.batch.params.PreTranslationByTmJobParams
import io.tolgee.service.LanguageService
import org.springframework.stereotype.Component
import kotlin.coroutines.CoroutineContext

@Component
class PreTranslationByTmChunkProcessor(
  private val languageService: LanguageService,
  private val genericAutoTranslationChunkProcessor: GenericAutoTranslationChunkProcessor
) : ChunkProcessor<PreTranslationByTmRequest, PreTranslationByTmJobParams, Long> {
  override fun process(
    job: BatchJobDto,
    chunk: List<Long>,
    coroutineContext: CoroutineContext,
    onProgress: (Int) -> Unit
  ) {
    val parameters = getParams(job)
    val languages = languageService.findByIdIn(parameters.targetLanguageIds)

    val preparedChunk = chunk.map { keyId ->
      languages.map { language ->
        BatchTranslationTargetItem(keyId as Long, language.id)
      }
    }.flatten()

    genericAutoTranslationChunkProcessor.process(
      job,
      preparedChunk,
      coroutineContext,
      onProgress,
      GenericAutoTranslationChunkProcessor.Type.PRE_TRANSLATION_BY_TM,
    )
  }

  override fun getTargetItemType(): Class<Long> {
    return Long::class.java
  }

  override fun getTarget(data: PreTranslationByTmRequest): List<Long> {
    return data.keyIds
  }

  override fun getParamsType(): Class<PreTranslationByTmJobParams> {
    return PreTranslationByTmJobParams::class.java
  }

  override fun getParams(data: PreTranslationByTmRequest): PreTranslationByTmJobParams {
    return PreTranslationByTmJobParams().apply {
      this.targetLanguageIds = data.targetLanguageIds
    }
  }
}

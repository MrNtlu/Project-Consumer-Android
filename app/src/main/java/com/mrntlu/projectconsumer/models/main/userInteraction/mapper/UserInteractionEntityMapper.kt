package com.mrntlu.projectconsumer.models.main.userInteraction.mapper

import com.mrntlu.projectconsumer.interfaces.EntityMapper
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterContent
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.models.main.userInteraction.entity.ConsumeLaterContentEntity
import com.mrntlu.projectconsumer.models.main.userInteraction.entity.ConsumeLaterEntity

object UserInteractionEntityMapper: EntityMapper<List<ConsumeLaterResponse>, List<ConsumeLaterEntity>> {
    override fun asEntity(model: List<ConsumeLaterResponse>,tag: String, page: Int): List<ConsumeLaterEntity> {
        return model.map { consumeLater ->
            ConsumeLaterEntity(
                consumeLater.id,
                consumeLater.userID,
                consumeLater.contentID,
                consumeLater.contentExternalID,
                consumeLater.contentExternalIntID,
                consumeLater.contentType,
                consumeLater.selfNote,
                consumeLater.createdAt,
                ConsumeLaterContentEntity(
                    consumeLater.content.titleOriginal,
                    consumeLater.content.imageURL,
                    consumeLater.content.description,
                ),
                tag,
                page,
            )
        }
    }

    override fun asModel(entity: List<ConsumeLaterEntity>): List<ConsumeLaterResponse> {
        return entity.map { consumeLaterEntity ->
            ConsumeLaterResponse(
                consumeLaterEntity.id,
                consumeLaterEntity.userID,
                consumeLaterEntity.contentID,
                consumeLaterEntity.contentExternalID,
                consumeLaterEntity.contentExternalIntID,
                consumeLaterEntity.contentType,
                consumeLaterEntity.selfNote,
                consumeLaterEntity.createdAt,
                ConsumeLaterContent(
                    consumeLaterEntity.content.titleOriginal,
                    consumeLaterEntity.content.imageURL,
                    consumeLaterEntity.content.description,
                ),
            )
        }
    }
}

fun List<ConsumeLaterResponse>.asEntity(tag: String, page: Int): List<ConsumeLaterEntity> {
    return UserInteractionEntityMapper.asEntity(this, tag, page)
}

fun List<ConsumeLaterEntity>.asModel(): List<ConsumeLaterResponse> {
    return UserInteractionEntityMapper.asModel(this)
}
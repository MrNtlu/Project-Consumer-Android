package com.mrntlu.projectconsumer.models.main.tv.mapper

import com.mrntlu.projectconsumer.interfaces.EntityMapper
import com.mrntlu.projectconsumer.models.common.Translation
import com.mrntlu.projectconsumer.models.common.entity.TranslationEntity
import com.mrntlu.projectconsumer.models.main.tv.Network
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.models.main.tv.entity.NetworkEntity
import com.mrntlu.projectconsumer.models.main.tv.entity.TVSeriesEntity

object TVSeriesEntityMapper: EntityMapper<List<TVSeries>, List<TVSeriesEntity>> {
    override fun asEntity(model: List<TVSeries>, tag: String, page: Int): List<TVSeriesEntity> {
        return model.map { tv ->
            TVSeriesEntity(
                tv.id,
                tv.description,
                tv.actors,
                tv.genres,
                tv.networks?.map { NetworkEntity(it.logo, it.name, it.originCountry) },
                tv.seasons,
                tv.translations?.map { TranslationEntity(it.lanCode, it.lanName, it.lanNameEn, it.title, it.description) },
                tv.status,
                tv.streaming,
                tv.backdrop,
                tv.imageURL,
                tv.firstAirDate,
                tv.title,
                tv.titleOriginal,
                tv.tmdbID,
                tv.tmdbPopularity,
                tv.tmdbVote,
                tv.tmdbVoteCount,
                tv.totalEpisodes,
                tv.totalSeasons,
                tv.productionCompanies,
                tag,
                page,
            )
        }
    }

    override fun asModel(entity: List<TVSeriesEntity>): List<TVSeries> {
        return entity.map { tvEntity ->
            TVSeries(
                tvEntity.id,
                tvEntity.description,
                tvEntity.actors,
                tvEntity.genres,
                tvEntity.networks?.map { Network(it.logo, it.name, it.originCountry) },
                tvEntity.seasons,
                tvEntity.translations?.map { Translation(it.lanCode, it.lanName, it.lanNameEn, it.title, it.description) },
                tvEntity.status,
                tvEntity.streaming,
                tvEntity.backdrop,
                tvEntity.imageURL,
                tvEntity.firstAirDate,
                tvEntity.title,
                tvEntity.titleOriginal,
                tvEntity.tmdbID,
                tvEntity.tmdbPopularity,
                tvEntity.tmdbVote,
                tvEntity.tmdbVoteCount,
                tvEntity.totalEpisodes,
                tvEntity.totalSeasons,
                tvEntity.productionCompanies,
            )
        }
    }
}

fun List<TVSeries>.asEntity(tag: String, page: Int): List<TVSeriesEntity> {
    return TVSeriesEntityMapper.asEntity(this, tag, page)
}

fun List<TVSeriesEntity>.asModel(): List<TVSeries> {
    return TVSeriesEntityMapper.asModel(this)
}
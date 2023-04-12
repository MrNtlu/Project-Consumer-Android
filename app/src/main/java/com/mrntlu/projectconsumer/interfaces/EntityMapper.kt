package com.mrntlu.projectconsumer.interfaces

import java.util.Date

interface EntityMapper<Model, Entity> {

    fun asEntity(
        model: Model, tag: String, page: Int,
    ): Entity

    fun asModel(entity: Entity): Model
}
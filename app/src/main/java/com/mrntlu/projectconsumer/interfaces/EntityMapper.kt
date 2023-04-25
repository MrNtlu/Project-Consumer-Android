package com.mrntlu.projectconsumer.interfaces

interface EntityMapper<Model, Entity> {

    fun asEntity(
        model: Model, tag: String, page: Int,
    ): Entity

    fun asModel(entity: Entity): Model
}
package uk.matvey.slon.plugin

import org.gradle.api.model.ObjectFactory
import org.jooq.meta.jaxb.ForcedType
import javax.inject.Inject

abstract class SlonExtension @Inject constructor(objects: ObjectFactory) {

    val imageName = objects.property(String::class.java)
    val inputSchema = objects.property(String::class.java)
    val flywayDir = objects.directoryProperty()
    val jooqDir = objects.directoryProperty()
    val packageName = objects.property(String::class.java)
    val forcedTypes = objects.listProperty(ForcedType::class.java)
}
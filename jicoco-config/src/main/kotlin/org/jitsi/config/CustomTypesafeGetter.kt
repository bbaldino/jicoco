/*
 * Copyright @ 2018 - present 8x8, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jitsi.config

import com.typesafe.config.Config
import kotlin.reflect.KType

/**
 * An interface for defining a custom retrieval function which will get a type [T] instance from
 * a [Config].
 */
interface CustomTypesafeGetter <out T : Any> {
    /**
     * Get a value of type [T] from [config] for the property at [key].
     */
    fun get(key: String, config: Config): T
}

/**
 * A mapping of [KType] instances to [CustomTypesafeGetter] instances which can retrieve a value as that type
 * given a config property key and a [Config] instance.  [CustomTypesafeGetter] instances can be added to this map
 * and they'll be checked by the [TypesafeConfigSource] when looking up getters.
 */
val CustomTypesafeGetters = mutableMapOf<KType, CustomTypesafeGetter<Any>>()

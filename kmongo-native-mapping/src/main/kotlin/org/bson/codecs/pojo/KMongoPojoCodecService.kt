/*
 * Copyright (C) 2017 Litote
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bson.codecs.pojo

import org.bson.codecs.configuration.CodecRegistry

/**
 *
 */
internal object KMongoPojoCodecService {

    val codecProvider: KMongoPojoCodecProvider  by lazy { KMongoPojoCodecProvider() }
    val codecRegistry: CodecRegistry by lazy { codecProvider.codecRegistry }

    val codecProviderWithNullSerialization: KMongoPojoCodecProvider  by lazy {
        KMongoPojoCodecProvider(
                object : PropertySerialization<Any> {
                    override fun shouldSerialize(value: Any?): Boolean = true
                })
    }
    val codecRegistryWithNullSerialization: CodecRegistry by lazy { codecProviderWithNullSerialization.codecRegistry }
}
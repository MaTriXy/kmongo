/*
 * Copyright (C) 2016 Litote
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

package org.litote.kmongo.async

import com.mongodb.client.model.UpdateOptions
import org.junit.Test
import org.litote.kmongo.MongoOperator.exists
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.MongoOperator.unset
import org.litote.kmongo.async.model.Friend
import kotlin.test.assertEquals

/**
 *
 */
class UpdateTest : KMongoAsyncBaseTest<Friend>() {

    @Test
    fun canUpdateMulti() {
        col.insertMany(listOf(Friend("John"), Friend("John")), {
            r, t ->
            col.updateMany("{name:'John'}", "{$unset:{name:1}}") { r, t ->
                col.count("{name:{$exists:true}}", { r, t ->
                    asyncTest {
                        assertEquals(r, r)
                    }
                })
            }
        })
    }

    @Test
    fun canUpdateByObjectId() {
        val friend = Friend("Paul")
        col.insertOne(friend, { r, t ->
            col.updateOne(friend._id!!, "{$set:{name:'John'}}", { r, t ->
                col.findOne("{name:'John'}", { r, t ->
                    asyncTest {
                        assertEquals("John", r!!.name)
                        assertEquals(friend._id, r._id)
                    }
                })
            })
        })
    }

    @Test
    fun canUpsert() {
        col.updateOne("{}", "{$set:{name:'John'}}", UpdateOptions().upsert(true), { r, t ->
            col.findOne("{name:'John'}", { r, t ->
                asyncTest {
                    assertEquals("John", r!!.name)
                }
            })
        })
    }

    @Test
    fun canPartiallyUdpateWithAPreexistingDocument() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend, { r, t ->
            val preexistingDocument = Friend(friend._id!!, "Johnny")
            col.updateOne("{name:'John'}", preexistingDocument, { r, t ->
                col.findOne("{name:'Johnny'}", { r, t ->
                    asyncTest {
                        assertEquals("Johnny", r!!.name)
                        assertEquals("123 Wall Street", r.address)
                        assertEquals(friend._id, r._id)
                    }
                })
            })
        })
    }

    @Test
    fun canPartiallyUdpateWithANewDocument() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend, { r, t ->
            val newDocument = Friend("Johnny")
            col.updateOne("{name:'John'}", newDocument, { r, t ->
                col.findOne("{name:'Johnny'}", { r, t ->
                    asyncTest {
                        assertEquals("Johnny", r!!.name)
                        assertEquals("123 Wall Street", r.address)
                    }
                })
            })
        })
    }

    @Test
    fun canUpdateTheSameDocument() {
        val friend = Friend("John", "123 Wall Street")
        col.insertOne(friend, { r, t ->
            friend.name = "Johnny"
            col.updateOne(friend, { r, t ->
                col.findOne("{name:'Johnny'}", { r, t ->
                    asyncTest {
                        assertEquals("Johnny", r!!.name)
                        assertEquals("123 Wall Street", r.address)
                        assertEquals(friend._id, r._id)
                    }
                })
            })
        })
    }

}
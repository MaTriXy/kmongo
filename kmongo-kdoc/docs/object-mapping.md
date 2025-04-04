# Object Mapping

Query results are automatically mapped to objects.

Look at the [Quick Start related paragraph](https://litote.org/kmongo/quick-start/#object-mapping-engine)
in order to know how to select the object mapping engine.

## Configure ```KotlinModule```

The ```ObjectMapper``` used by KMongo uses a [jackson's KotlinModule](https://github.com/FasterXML/jackson-module-kotlin). 
The module is created with default ```KotlinFeature```s. 
To change configuration of the module, use ```KotlinModuleConfiguration``` object:

```kotlin
KotlinModuleConfiguration.kotlinModuleInitializer = { 
    // Configure `KotlinModule` using its `Builder` (which is referenced as `this`):
    enable(KotlinFeature.SingletonSupport) 
    enable(KotlinFeature.StrictNullChecks)
    ...
}
KMongoConfiguration.resetConfiguration()
```

## Set your _id

To manage Mongo ```_id```, a class must have one ```_id``` property
OR a property annotated with the ```@BsonId``` annotation.
 
> For **kotlinx serialization**, ```@BsonId``` is not supported - you have to use ```@SerialName("_id")``` as ```@BsonId``` replacement.
>          
> For example, ```Data(@Contextual val _id: Id<Data>)``` and ```Data(@Contextual @SerialName("_id") val myId: Id<Data>)``` are equivalent.
 
If there is no such field in your class, an ```ObjectId``` _id is generated on server side.

### KMongo Id type

KMongo provides a optional dedicated [Id](http://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/-id.html) type.
With this Id class, the document identifier is typed. 

So for example:  
 
```kotlin
class LightSaber(val _id: Id<LightSaber> = newId())
class Jedi(
    @BsonId val key: Id<Jedi> = newId(), 
    //set of typed ids, now I see what it is!
    val sabers:Set<Id<LightSaber>> = emptySet()
)
``` 

It is easy to transform an ObjectId in Id<> with the *toId()* extension:

```kotlin
LightSaber(ObjectId("myId").toId())
``` 

> For **kotlinx serialization** ```@Contextual``` is mandatory on the ```Id``` property.
> If you want to reference a list of ids, you need to annotate the list argument:
> 
> ```val refs: List<@Contextual Id<Data>>```

#### KMongo Id does not depend of Mongo nor KMongo lib
                     
This is useful if you need to transfer your data object from a mongo backend 
to a frontend that does not need to know your storage engine.

You just have to add the ```kmongo-id``` dependency in the frontend to compile. 

- with Maven

```xml
<dependency>
  <groupId>org.litote.kmongo</groupId>
  <artifactId>kmongo-id</artifactId>
  <version>5.2.1</version>
</dependency>
```

- or Gradle

```
compile 'org.litote.kmongo:kmongo-id:5.2.1'
```

#### Id <> Json Jackson serialization

If you use Jackson to serialize your objects to json (in order to transfer your data between frontend and backend),
add the ```kmongo-id-jackson``` dependency and register the ```IdJacksonModule``` module:

```kotlin 
mapper.registerModule(IdJacksonModule())
```

#### Id <> Json Gson serialization

If you prefer to use Gson, you need to use this code:

```kotlin 
val gsonBuilder = GsonBuilder();
gsonBuilder.registerTypeAdapter(Id::class.java,
        JsonSerializer<Id<Any>> { id, _, _ -> JsonPrimitive(id?.toString()) })
gsonBuilder.registerTypeAdapter(Id::class.java,
        JsonDeserializer<Id<Any>> { id, _, _ -> id.asString.toId() })
val gson = gsonBuilder.create()
``` 

#### Id <> Json KotlinX serialization

If you use KotlinX serialization to serialize your objects to json (in order to transfer your data between frontend and backend),
add the ```kmongo-id-serialization``` dependency and register the ```IdKotlinXSerializationModule``` module:

```kotlin     
@Serializable
data class Data(@Contextual val _id: Id<Data> = newId())

val json = Json { serializersModule = IdKotlinXSerializationModule }
val data = Data()
val json = json.encodeToString(data) 
```

### Gets an Id from an ObjectId

Just use the .toId() extension:

```kotlin 
val id: Id<A> = ObjectId().toId()
```

### Other _id types

The Id type is optional, 
you can use ```String```, ```org.bson.types.ObjectId``` of whatever you need as _id type.

#### UUID

In order to use UUID as identifier, you have to specify (as documented in mongo java driver):

```kotlin 
KMongo.createClient(
    MongoClientSettings
    .builder()
    .uuidRepresentation(UuidRepresentation.STANDARD)
    .applyConnectionString(ConnectionString(connection))
    .build()
)
```
     
For *jackson*, you also need to use the
[setUUIDRepresentation](http://litote.org/kmongo/dokka/kmongo/org.litote.kmongo.util/-k-mongo-jackson-feature/index.html)
function.


## Date support

KMongo provides built-in support for these "Date" classes:

- java.util.Date
- java.util.Calendar
- java.time.LocalDateTime
- java.time.LocalDate
- java.time.LocalTime
- java.time.ZonedDateTime
- java.time.OffsetDateTime
- java.time.OffsetTime
- java.time.Instant
- java.time.ZoneId

Dates are always stored in Mongo in UTC. For ```Calendar```, ```ZonedDateTime```, ```OffsetDateTime``` and ```OffsetTime```,
whatever is the timezone of the saved date, you will get an UTC date when loading the date from Mongo.

So, in this case, the loaded date will not usually be equals to the saved date - the Instant part of the date of course is the same. If you need to check equality, use a method like ```OffsetDateTime#withOffsetSameInstant``` on the loaded date.

## Register a custom mongo Codec

Use  [ObjectMappingConfiguration.addCustomCodec](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo.util/-object-mapping-configuration/index.html) function in order to register a custom codec.
You have also the option to use [custom Jackson modules](https://litote.org/kmongo/object-mapping/#register-a-custom-jackson-module) or custom [kotlinx.serialization modules](https://litote.org/kmongo/object-mapping/#additional-modules-and-serializers).

## How to choose the mapping engine

### The Jackson choice

With the [Jackson library](https://github.com/FasterXML/jackson), you get full support of one of the most powerful data-binding java engine, in order to map your data objects.

If you need custom serialization or deserialization, you will not be blocked, you can implement it.

And you can use all [Jackson annotations](https://github.com/FasterXML/jackson-annotations/wiki/Jackson-Annotations).

Also, if you already use Jackson for json serialization (for rest services for example), the library is already in your list of dependencies.

However, if you don't use already Jackson, you add a new quite complex library to your dependencies.

#### Register a custom Jackson module

Use [registerBsonModule](http://litote.org/kmongo/dokka/kmongo/org.litote.kmongo.util/-k-mongo-configuration/register-bson-module.html) function.

### The POJO Codec "native" choice

Started in 3.5.0 (July 2017), the java driver introduces a new [POJO mapping framework](https://mongodb.github.io/mongo-java-driver/3.5/bson/pojos/).

KMongo uses it to provide object mapping for Kotlin. No other dependency than the core java mongo driver is required.

All the common cases are covered. However, there are some limitations. For example, top level POJO cannot contain any type parameters.

### The kotlinx serialization choice

Starting with 3.11.2 version, KMongo also supports [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) mapping.

The main advantage of this kind of mapping is that **almost no (slow) reflection** is involved.

#### Additional Modules and Serializers

Use ```registerModule``` or ```registerSerializer``` functions in order to register new 
(for example polymorphic) kotlinx Module or Serializer.

#### Avoid completely reflection

By default, there is still a little bit of reflection involved when persisting a document, in order to retrieve dynamically 
the id of the document instance. You can declare your own ```IdController```,
using the ```changeIdController``` function to avoid completely reflection. Then you can exclude the kotlin-reflect
dependency!

### Conclusion

- We expect that, for most of the projects, the "native" bindings will be perfectly OK. 

- For complex projects, or if you have already Jackson skills, the Jackson object mapping is a really nice choice.

- Kotlin experts will prefer kotlinx serialization, as it should provide the best performance.

Choose your poison! :)

See also [Performance section](../performance).

## Explicitly defining the ClassMappingTypeService

If you get an error stating "Service ClassMappingTypeService not found", you are probably using KMongo in an environment where your application is loaded at runtime (e.g. Minecraft plugins).

To fix this error, you can set the `org.litote.mongo.mapping.service` property to the qualified class name of the service for the object mapping engine you are using. For kotlinx.serialization this would be the following:

```kotlin
System.setProperty("org.litote.mongo.mapping.service", SerializationClassMappingTypeService::class.qualifiedName!!)
```

For the class for other mappings than in the example above, see which class inherits from `ClassMappingTypeService` in your module.

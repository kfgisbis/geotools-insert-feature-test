# Problems changing Feature

### RUN

```
run InsertTest
```

### INSERT FEATURE

I have the table with UUID key and want use for them UUID 7 version that have two good thing - sequentiality and storing creating record datetime

There are three way for it - use key from database, set default UUID generation in geoserver and use parameter idgen for wfs-t insert transaction
First variant do not implements in geoserver, second also.
Last variant is good choice, but not working when I try to call it from my app use geotools library

It is not clear how to set the user identifier. Setting the parameter idgen="UseExisting"
is possible only in objects of the InsertElementTypeImpl type.

feature.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE); - does not work

This was achieved only by overriding the Strategy createInsert method and adding the line:

```
 insert.setIdgen(IdentifierGenerationOptionType.USE_EXISTING_LITERAL);
```

```java
protected InsertElementType createInsert(WfsFactory factory, TransactionRequest.Insert elem) throws Exception {
    InsertElementType insert = factory.createInsertElementType();

    insert.setIdgen(IdentifierGenerationOptionType.USE_EXISTING_LITERAL);

    String srsName = getFeatureTypeInfo(elem.getTypeName()).getDefaultSRS();
    insert.setSrsName(new URI(srsName));

    List<SimpleFeature> features = elem.getFeatures();

    insert.getFeature().addAll(features);

    return insert;
}
```



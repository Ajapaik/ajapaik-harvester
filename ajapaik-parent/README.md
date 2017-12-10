# Ehitamine #
Projekti ehitamiseks, sõltuvuste halduseks ja reliisprotsessis on kasutusel Apache Maven2.

Projekti ehitamiseks tuleb kloonida repository, navigeerida "ajapaik-parent" kausta ja käivitada käsk:


```

mvn clean install

```

Käsk ehitab kõik alammoodulid kokku, tekitades deploytavatest projektidest "war" failid.

Projekt on vana ja kõik dependency'd pole enam kätte saadavad. Need on Gitis kaustas ajapaik-service/lib. Selleks, et need tööle hakkaksid, tuleb need lokaalselt installida nii:
```
mvn install:install-file   -Dfile=ajapaik-service/lib/jsonrpc4j-0.24.jar   -DgroupId=com.googlecode   -DartifactId=jsonrpc4j   -Dversion=0.24   -Dpackaging=jar   -DgeneratePom=true
```

JsonRPCst on ka uuem versioon olemas ja pom.xml kaudu alla laetav, aga sellega ei tööta valimimooduli otsing korrektselt.

# Arendamine #
Projekti arendamiseks tuleb genereerida projekti failid. Eclipse keskkonnas arendamiseks navigeerida "ajapaik-parent" kausta ja käivitada käsk:


```

mvn eclipse:eclipse

```

Käsk genereerib projekti failid moodulitesse. Peale seda on võimalik projektid importida (Import existing projects) eclipse keskkonda.

# Jooksutamine#
Jooksutamiseks Jetty serveris, tuleks navigeerida "ajapaik-parent" kausta ja käivitada käsk 


```

mvn jetty:run
```

Peale käivitust on rakendus nähtav aadressil http://localhost:8080/ajapaik-ui

Rakendust hoiab elus supervisor.


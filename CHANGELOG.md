
# Release Notes


## [1.3.0](https://github.com/terraframe/geoprism-registry/releases/tag/1.3.0) (2023-07-17)

### Features

 - **explorer** include parents from inherited hierarchies in parents tab  ([#911](https://github.com/terraframe/geoprism-registry/issues/911)) ([4b8fe](https://github.com/terraframe/geoprism-registry/commit/4b8fe158c604d6b085cc90679a84fb239fac48a5))
 - **importer** multithreaded importing   ([982e9](https://github.com/terraframe/geoprism-registry/commit/982e9bfeeef10d39cfb55dd839627a39502c6b8b))
 - **lists** allow public access to the list version page   ([37d10](https://github.com/terraframe/geoprism-registry/commit/37d1068bfaea34f4531fc9ac1f74772cd5bacd42))

### Bug Fixes

   - fixed application labels not being localized. #900  ([#900](https://github.com/terraframe/geoprism-registry/issues/900)) ([08718](https://github.com/terraframe/geoprism-registry/commit/0871862570219c8b7a4e7a4354d6454400d15fd5))
   - changed the initial explorer bounds of an object to use the most recent geometry  ([#902](https://github.com/terraframe/geoprism-registry/issues/902)) ([c7545](https://github.com/terraframe/geoprism-registry/commit/c75457047dbdaa5d1a54585e5820d9e20fee3c75))
   - **dhis2-sync** error message details empty for external system synchronization  ([#909](https://github.com/terraframe/geoprism-registry/issues/909)) ([11d82](https://github.com/terraframe/geoprism-registry/commit/11d826852de34ffa0a4c3b1d270532604c9164df))
   - **explorer** parent auto-complete dropdown missing labels  ([#913](https://github.com/terraframe/geoprism-registry/issues/913)) ([75742](https://github.com/terraframe/geoprism-registry/commit/75742fea70fad2c3bae8490a50bfb09a95b8627a))
   - georegistry-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGSPRINGFRAMEWORK-3369749- https://snyk.io/vuln/SNYK-JAVA-ORGSPRINGFRAMEWORK-3369852  ([bd27f](https://github.com/terraframe/geoprism-registry/commit/bd27f788da406e6ba77a64d845691aa850afd73d))



## [1.2.2](https://github.com/terraframe/geoprism-registry/releases/tag/1.2.2) (2023-06-23)

### Features

 - ability to edit external ids in explorer and change requests  ([#872](https://github.com/terraframe/geoprism-registry/issues/872)) ([67bac](https://github.com/terraframe/geoprism-registry/commit/67bacc93754a69946596a775125ecf28bdd0bf11))
 - **dhis2** support for dhis2 v2.40.0  ([#897](https://github.com/terraframe/geoprism-registry/issues/897)) ([face8](https://github.com/terraframe/geoprism-registry/commit/face88989995c23731b026307fd4fa38073a825a))
 - improve system logging   ([10c71](https://github.com/terraframe/geoprism-registry/commit/10c71d978ce708da52d933513a3456b1324c316d))

### Bug Fixes

   - **explorer** removed autocomplete behaviour when searching  ([#895](https://github.com/terraframe/geoprism-registry/issues/895)) ([a0004](https://github.com/terraframe/geoprism-registry/commit/a0004ed3dc94f4d23dde1e8289c77d01ce078e6f))
   - **synchronization** inherited types displaying duplicate or extra values  ([#896](https://github.com/terraframe/geoprism-registry/issues/896)) ([c45c7](https://github.com/terraframe/geoprism-registry/commit/c45c72aaa0ffca1bbba2577779fc61ba3eff40bf))
   - **synchronization** null pointer when syncing translations with dhis2  ([#898](https://github.com/terraframe/geoprism-registry/issues/898)) ([73492](https://github.com/terraframe/geoprism-registry/commit/734927b15605ab9e7c3926f48a0a0879ae7c7972))
   - **synchronization** too many codes in a sync error makes page unreadable  ([#898](https://github.com/terraframe/geoprism-registry/issues/898)) ([4620e](https://github.com/terraframe/geoprism-registry/commit/4620ec4848b6a12fcd83645d626503d59ffd0563))
   - **explorer** validity 'Yes' and 'No' labels for Geo-Objects swapped  ([#908](https://github.com/terraframe/geoprism-registry/issues/908)) ([94404](https://github.com/terraframe/geoprism-registry/commit/94404591278dfabf56a47e6593e5c3c8749618f9))
   - **dhis2-sync** better error handling and form validation  ([#910](https://github.com/terraframe/geoprism-registry/issues/910)) ([8e69b](https://github.com/terraframe/geoprism-registry/commit/8e69bec2f7d57f2b3e37fc8c3909bd82c7c1c492))
   - georegistry-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGSPRINGFRAMEWORK-5422217  ([7474c](https://github.com/terraframe/geoprism-registry/commit/7474c4b4fbc6c9a8e2dfee3f952da7e9687bb355))



## [1.2.1](https://github.com/terraframe/geoprism-registry/releases/tag/1.2.1) (2023-06-15)

### Features

 - ability to edit external ids in explorer and change requests  ([#872](https://github.com/terraframe/geoprism-registry/issues/872)) ([67bac](https://github.com/terraframe/geoprism-registry/commit/67bacc93754a69946596a775125ecf28bdd0bf11))
 - **dhis2** support for dhis2 v2.40.0  ([#897](https://github.com/terraframe/geoprism-registry/issues/897)) ([face8](https://github.com/terraframe/geoprism-registry/commit/face88989995c23731b026307fd4fa38073a825a))
 - improve system logging   ([10c71](https://github.com/terraframe/geoprism-registry/commit/10c71d978ce708da52d933513a3456b1324c316d))

### Bug Fixes

   - **explorer** removed autocomplete behaviour when searching  ([#895](https://github.com/terraframe/geoprism-registry/issues/895)) ([a0004](https://github.com/terraframe/geoprism-registry/commit/a0004ed3dc94f4d23dde1e8289c77d01ce078e6f))
   - **synchronization** inherited types displaying duplicate or extra values  ([#896](https://github.com/terraframe/geoprism-registry/issues/896)) ([c45c7](https://github.com/terraframe/geoprism-registry/commit/c45c72aaa0ffca1bbba2577779fc61ba3eff40bf))
   - **synchronization** null pointer when syncing translations with dhis2  ([#898](https://github.com/terraframe/geoprism-registry/issues/898)) ([73492](https://github.com/terraframe/geoprism-registry/commit/734927b15605ab9e7c3926f48a0a0879ae7c7972))
   - **synchronization** too many codes in a sync error makes page unreadable  ([#898](https://github.com/terraframe/geoprism-registry/issues/898)) ([4620e](https://github.com/terraframe/geoprism-registry/commit/4620ec4848b6a12fcd83645d626503d59ffd0563))
   - **explorer** validity 'Yes' and 'No' labels for Geo-Objects swapped  ([#908](https://github.com/terraframe/geoprism-registry/issues/908)) ([94404](https://github.com/terraframe/geoprism-registry/commit/94404591278dfabf56a47e6593e5c3c8749618f9))
   - **importer** Large geometries causing issues
   - georegistry-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGSPRINGFRAMEWORK-5422217  ([7474c](https://github.com/terraframe/geoprism-registry/commit/7474c4b4fbc6c9a8e2dfee3f952da7e9687bb355))



## [1.2.0](https://github.com/terraframe/geoprism-registry/releases/tag/1.2.0) (2023-04-12)

### Features

 - **dhis2-sync** ability to select multiple org unit groups ([#873](https://github.com/terraframe/geoprism-registry/issues/873)) ([48d2e](https://github.com/terraframe/geoprism-registry/commit/48d2e83d41ac72c7cc847da37b2dd82341c54021))

### Bug Fixes

   - **change-request** privacy setting always disabled on edit   ([bb741](https://github.com/terraframe/geoprism-registry/commit/bb741acbea85d9ffa71a1f6ccf80e36590d54b4b))
   - **settings** removing system logo throwing error   ([58466](https://github.com/terraframe/geoprism-registry/commit/584664187ae29c4212ab2b9efde5afb8986304f4))
   - **jobs** canceling a job does nothing if the import type was deleted   ([7b699](https://github.com/terraframe/geoprism-registry/commit/7b6991d18f15795f8322870e2135adb245f56af1))
   - georegistry-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGSPRINGFRAMEWORK-3369749- https://snyk.io/vuln/SNYK-JAVA-ORGSPRINGFRAMEWORK-3369852  ([bd27f](https://github.com/terraframe/geoprism-registry/commit/bd27f788da406e6ba77a64d845691aa850afd73d))



## [1.1.1](https://github.com/terraframe/geoprism-registry/releases/tag/1.1.1) (2023-03-13)


### Bug Fixes

   - **explorer** searching for hyphenated GeoObjects  ([#879](https://github.com/terraframe/geoprism-registry/issues/879)) ([b058a](https://github.com/terraframe/geoprism-registry/commit/b058afd4df244737f8a40d02cdd63c793e8f8312))
   - **dhis2** point geometries not syncing properly to dhis2  ([#887](https://github.com/terraframe/geoprism-registry/issues/887)) ([784be](https://github.com/terraframe/geoprism-registry/commit/784be18fdc834b32ffac7cd51707ffa50cf44298))
   - **account** submit button on user profile editor does nothing   ([1993d](https://github.com/terraframe/geoprism-registry/commit/1993d2b5680dc19259c0fc7d64b5bc9a1474d232))
   - **oauth** clicking on oauth button does nothing   ([d435e](https://github.com/terraframe/geoprism-registry/commit/d435e5cc30b9a270251e2631404bc73ad4cf9980))
   - **hierarchy-manager** unspecified error thrown when importing types from xml   ([939e0](https://github.com/terraframe/geoprism-registry/commit/939e0404f7ec3f2d9d8dd15050603d52f6af4ad1))





## [1.1.0](https://github.com/terraframe/geoprism-registry/releases/tag/1.1.0) (2023-02-27)

This release includes two major breaking changes: upgrading of OrientDB from v3.0 to v3.2 and upgrading Postgres/PostGIS from v9.5-3.0 to v14-3.2. Please refer to the [Database Migration](https://docs.geoprismregistry.com/readme/current/deployment-and-setup/3.10-migration) section of our documentation for detailed migration instructions.

<b>Note:</b> The v1.0 version of our docker-compose.yml file was already set to use PostgreSQL version 14-3.2. If this applies to you, then you might not need to perform a PostgreSQL migration. The PostgreSQL breaking change was included here because our cloud customers were migrated to PostgreSQL v14 as part of this release.

### Features

 - ![BREAKING CHANGE](https://raw.githubusercontent.com/terraframe/geoprism-registry/master/src/build/changelog/breaking-change.png) upgrade Orientdb to v3.2   ([2ce9b](https://github.com/terraframe/geoprism-registry/commit/2ce9bec8d0ce36b68b0a7aa879984d99855885d8))
 - ![BREAKING CHANGE](https://raw.githubusercontent.com/terraframe/geoprism-registry/master/src/build/changelog/breaking-change.png) upgrade Postgres to v14   ([64a01](https://github.com/terraframe/geoprism-registry/commit/64a01cb2e2cef93403d82252097e429a564196b2))
 - **explorer** show lat/long when not editing  ([#317](https://github.com/terraframe/geoprism-registry/issues/317)) ([be8d0](https://github.com/terraframe/geoprism-registry/commit/be8d05c669174dc94ef64def0c262bfc23be8cf4))
 - **change-request** ability to submit a create CR with no code  ([#401](https://github.com/terraframe/geoprism-registry/issues/401)) ([e8cd8](https://github.com/terraframe/geoprism-registry/commit/e8cd8957668ae4bf8965bf3af1213fb18c1905d0))
 - **api** optional hierarchy parameter on getChildGeoObjects/getParentGeoObjects   ([72dc7](https://github.com/terraframe/geoprism-registry/commit/72dc75c5933cc917034b43faae254398d178cdbd))
 - made attribute ordering consistent between a list and its exported spreadsheet, shapefile, and data dictionary  ([00c50](https://github.com/terraframe/geoprism-registry/commit/00c506e626fb2b06a38c4ef3c7e692e392e0839b))
 - **explorer** geo-Object search now displays a message if there a no results and gives look ahead options when typing  ([a666a](https://github.com/terraframe/geoprism-registry/commit/a666adb0f4be9277968bca0cca6a1ee85dd28fa7))
 - **lists** editing or creating a geo object will automatically update all working list versions of that geo object type to include the edits  ([cf6a7](https://github.com/terraframe/geoprism-registry/commit/cf6a700e8bb7a6edce3ee32571bb76e3bc28d54c))

### Bug Fixes

   - **lists** enforce read-only list-type properties  ([#569](https://github.com/terraframe/geoprism-registry/issues/569)) ([e26d8](https://github.com/terraframe/geoprism-registry/commit/e26d8f5a07cfe205ddc95fcc5b53194a81afe011))
   - **explorer** date picker cut off  ([#763](https://github.com/terraframe/geoprism-registry/issues/763)) ([e58f4](https://github.com/terraframe/geoprism-registry/commit/e58f4dc052d465d9c7a305eb6b4a357ca0d4f3ea))
   - **lists** localize labels  ([#794](https://github.com/terraframe/geoprism-registry/issues/794)) ([4dbe8](https://github.com/terraframe/geoprism-registry/commit/4dbe86bd5660ce25a3828f0830a4ce7cae3c1729))
   - **lists** fixed issue with updating list periods for a frequency-based series, the new lists not in chronological order #835 ([#835](https://github.com/terraframe/geoprism-registry/issues/835)) ([22680](https://github.com/terraframe/geoprism-registry/commit/22680f14fc36d691baf583c2f9854c7623e03191))
   - **explorer** show list when viewing record in working version  ([#862](https://github.com/terraframe/geoprism-registry/issues/862)) ([0b5d7](https://github.com/terraframe/geoprism-registry/commit/0b5d79fdb508c2b40204ab9e47805cf174cac3ba))
   - **hierarchies** lao translation showing in english version  ([#877](https://github.com/terraframe/geoprism-registry/issues/877)) ([28e98](https://github.com/terraframe/geoprism-registry/commit/28e982abfa75ff0750819cc76fa324571c1db966))
   - **localization** fixed issue where the locale of the user creating a type shows for the default locale if the default locale is not an exact match to theusers locale.  #877 ([#877](https://github.com/terraframe/geoprism-registry/issues/877)) ([277e1](https://github.com/terraframe/geoprism-registry/commit/277e1332e7d9366fd22f7f73224647489e0aef96))
   - **synchronization** add inherited types to dhis2 sync config  ([#878](https://github.com/terraframe/geoprism-registry/issues/878)) ([15539](https://github.com/terraframe/geoprism-registry/commit/1553961b75d6460c0b94186eada41c1007814111))
   - **geo-object-editor** unspecified error creating new time range  ([#883](https://github.com/terraframe/geoprism-registry/issues/883)) ([5a8c3](https://github.com/terraframe/geoprism-registry/commit/5a8c34648a74a41533550db27a20ee76dc4ba89d))
   - **geo-object-editor** unspecified error creating new time range  ([#884](https://github.com/terraframe/geoprism-registry/issues/884)) ([5a8c3](https://github.com/terraframe/geoprism-registry/commit/5a8c34648a74a41533550db27a20ee76dc4ba89d))
   - **import** adjusted UI of problem resolution for synonyms   ([35a36](https://github.com/terraframe/geoprism-registry/commit/35a36ab9f545b266f760d03138cf6429f48ef7b7))
   - **lists** fixed bug where published lists do not show on the list entries page if only a single version has been published and the user belongs toa different organization  ([c3c5c](https://github.com/terraframe/geoprism-registry/commit/c3c5c206fcb42f7407f809341d770db322d45fe8))
   - **localization** deadlock which could occur with active users on a system installing a new locale   ([bcf94](https://github.com/terraframe/geoprism-registry/commit/bcf9478c85a826c5368db88c88db4be521f6fe6b))


## [1.0.0](https://github.com/terraframe/geoprism-registry/releases/tag/1.0.0) (2022-12-02)

### Features

 - **explorer** rendering list records as selected layer  ([#862](https://github.com/terraframe/geoprism-registry/issues/862)) ([a8c6c](https://github.com/terraframe/geoprism-registry/commit/a8c6c0441d9f47107b6e0367c9ab01d8c2281d8f))
 - **synchronization** group related errors and classify no parent exception as warning   ([95aff](https://github.com/terraframe/geoprism-registry/commit/95aff3d97c84353a8fb7b4c1806f93f1dbb3a807))
 - **dhis2** support for version 2.39   ([08fcc](https://github.com/terraframe/geoprism-registry/commit/08fcc6f30ff655230ccda4212eb659cbc98ebe6d))
 - **external-system** alert users if the dhis2 server is unsupported   ([150a0](https://github.com/terraframe/geoprism-registry/commit/150a0ebdb83d27ed273cc6fc70692953ca4a5b6e))
 - metadata to support attribute groupings in a list   ([4ddee](https://github.com/terraframe/geoprism-registry/commit/4ddee46d18b9c8f0b8878c29b9ffec126acadda2))
 - **synchronization** synchronize with dhis2 in bulk submissions   ([7de21](https://github.com/terraframe/geoprism-registry/commit/7de21e8ae763a52ff23b7eca739c5174e6df2db4))
 - support for viewing lists in a panel on explorer   ([514da](https://github.com/terraframe/geoprism-registry/commit/514da7614619bf6ecebc91a2224e024237f4fd1a))
 - ability to export and refresh a list from the list modal if the user has permissions  ([51b24](https://github.com/terraframe/geoprism-registry/commit/51b249a6c320ebfb5ddbdc25980f39ee9d7d37c1))
 - changed table state to be cleared on log out. preserve state of multiple different tables  ([4334e](https://github.com/terraframe/geoprism-registry/commit/4334e74f89da1ba96754899f5c845808c412a2fe))
 - store table state in local storage so that it can be used on multiple pages  ([64550](https://github.com/terraframe/geoprism-registry/commit/64550319a0477ce63ba85e406f9b992d8a5a8dc3))
 - support for custom localized attributes   ([830aa](https://github.com/terraframe/geoprism-registry/commit/830aa0a676c41a1fe9862543c225d2903ad0e7f1))
 - on linking from the list to the explorer open the geo object if the user has write permissions  ([51271](https://github.com/terraframe/geoprism-registry/commit/51271503c24de6936f1b52e934fcf399e00ff160))
 - ability to restore state for the generic table widget   ([8c802](https://github.com/terraframe/geoprism-registry/commit/8c8022a0303e1cd22807ff34addd340eac14a0c1))
 - **synchronization** support for DHIS2 geometry syncing on DHIS2 servers > 31   ([6a926](https://github.com/terraframe/geoprism-registry/commit/6a9267327e88d45b83161341ca09e7bfd47fc2df))
 - added 'sync non-existent Geo-Objects' option to dhis2 sync config   ([8f2d5](https://github.com/terraframe/geoprism-registry/commit/8f2d56f99b2c9ed12d617970b30218c7ce059b29))
 - improve performance of list-type/entries API endpoint   ([85f51](https://github.com/terraframe/geoprism-registry/commit/85f5195b4d774f093f2e74be8ae1816ef6bb1cce))
 - allow users to sync exist's period dates to dhis2   ([ac448](https://github.com/terraframe/geoprism-registry/commit/ac4488696eff6f67bab4ea341d1821e2e5841f40))
 - **shapefile** Made projection checking optional   ([e64c4](https://github.com/terraframe/geoprism-registry/commit/e64c42a789bda0fe9fe21e8d6de7788cf89330a0))

### Bug Fixes

   - support for localized text attribute on business types   ([fdd53](https://github.com/terraframe/geoprism-registry/commit/fdd533d6f600fd2cce244abec7fa83a4bff4f9eb))
   - ![BREAKING CHANGE](https://raw.githubusercontent.com/terraframe/geoprism-registry/master/src/build/changelog/breaking-change.png) **external-system** duplicate data constraints on external ids   ([11b02](https://github.com/terraframe/geoprism-registry/commit/11b0220b5490aa73d5a8c2322df1c8b5a50eab0f))
   - **dhis2** target attribute sometimes would not populate correctly   ([af73b](https://github.com/terraframe/geoprism-registry/commit/af73b71fddfb495574a3282558bf6b30f852ab6b))
   - **synchronization** added preferred locale as well as other bugfixes   ([6cab4](https://github.com/terraframe/geoprism-registry/commit/6cab496c9ec9426e43f1463708da0b81390ff5ab))
   - fix bug preventing deleting point geometries   ([ee325](https://github.com/terraframe/geoprism-registry/commit/ee3258f2ed12d5a9bc85e9bbaf5d9d690424dd96))
   - searching for geo-objects in explorer with apostrophe silently failing   ([35b1b](https://github.com/terraframe/geoprism-registry/commit/35b1b505c336d24594ea32bcf49f25c81f7e5d9e))
   - no longer allow the user to edit the hierarchy definitions of existing lists  ([bac5d](https://github.com/terraframe/geoprism-registry/commit/bac5d6553fc7b53604994c58440139783d9bcc36))
   - **navigator** attribute panel styling bug on firefox   ([43392](https://github.com/terraframe/geoprism-registry/commit/43392acd2a3f445632e31413bfd3c8ca833f08f8))
   - navigator stability period not correct when viewing from list   ([d131d](https://github.com/terraframe/geoprism-registry/commit/d131d5d4f3ae90d447fec8be9fe4533b30b56ce2))
   - **synchronization** dhis2 translations not syncing correctly   ([92a83](https://github.com/terraframe/geoprism-registry/commit/92a83878c7ae9d2dbffc8901819db4d51613aa9e))
   - **synchronization** best fit locales in translations where the countries do not match   ([93533](https://github.com/terraframe/geoprism-registry/commit/935335ea314d5efdc573e5790d7848816efe13b7))
   - don't export infinity date to DHIS2   ([54505](https://github.com/terraframe/geoprism-registry/commit/5450568545cb9994f0ecba0fa228fff875c907af))
   - layer pin not displaying correctly   ([fcf93](https://github.com/terraframe/geoprism-registry/commit/fcf936066c0555cb1d5899d9ec9e9f2efb6cda88))
   - navigator back button not working   ([358e7](https://github.com/terraframe/geoprism-registry/commit/358e726cf847122f04bf9940e9832ca5ec84b688))
   - don't export invalid Geo-Objects to DHIS2   ([34232](https://github.com/terraframe/geoprism-registry/commit/342326cc070bce9dba6f46198c84c4da454933fb))
   - expand list of DHIS2 versions that our server can connect to   ([6f343](https://github.com/terraframe/geoprism-registry/commit/6f3436dcaac48c00c828ecf2c75c707f8c67ae57))
   - Fixed mismatch of versions between the test project and the others   ([fb035](https://github.com/terraframe/geoprism-registry/commit/fb035ada518d27ce64e8e22ae4c265b29015e792))


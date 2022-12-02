
# Release Notes

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


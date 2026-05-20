# Changelog

## [0.5.0](https://github.com/sergiodelgadotech/kotlin-saas-starter/compare/v0.4.0...v0.5.0) (2026-05-20)


### Features

* add cache group to SaasStarterProperties ([13b8039](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/13b8039c8a73a921fbcc523fe24e7e49b0920318))
* add RedisAutoConfiguration with property-driven cache layout ([0bafa09](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/0bafa09b578a3da43b5da574736a1cecf380767a))


### Bug Fixes

* remove @ConditionalOnBean from EnableRedisSessionConfig ([c81b0f2](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/c81b0f210f065e0719aa7ba7c2e0e1cadb8b6869)), closes [#28](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/28)
* remove BeansConfig ConditionalOnBean and rename template bean to jsonRedisTemplate ([36ece66](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/36ece66dd7d3262f6306e225cb2dfc4acd15969e))


### Documentation

* add design spec for issue [#28](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/28) SessionAutoConfiguration fix ([c5ecc6e](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/c5ecc6e398a92559d58005e498cfe8d6a396db41))
* add project analysis and suggestions ([6be4642](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/6be4642c3b4ac029f45f45cdbb81ae7926c23a2c))
* record autoconfig inner-class pattern gotcha in memory ([7761d9f](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/7761d9f71466a4ca896b1a94d092efde7c6326c7))

## [0.4.0](https://github.com/sergiodelgadotech/kotlin-saas-starter/compare/v0.3.0...v0.4.0) (2026-05-17)


### Features

* add JobRunrAutoConfiguration with tenant filter and scheduler beans ([7b8b428](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/7b8b428d24dbfc901f4e4dd34a9e6e533acdeed1))
* add jobs group to SaasStarterProperties ([f9d93a1](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/f9d93a13ad2dd637ee18bf57163164557b8c868b))


### Bug Fixes

* **jobs:** wire TenantJobFilter into JobScheduler and BackgroundJobServer ([914ab05](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/914ab054576c78950e457c2766a1a3db0bab8aa5))


### Documentation

* **memory:** update feedback — composite build replaces publishToMavenLocal ([085fedf](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/085fedf3d09ba98a2bd8be41dd3ede4379469eda))

## [0.3.0](https://github.com/SergioDelgado-tech/kotlin-saas-starter/compare/v0.2.1...v0.3.0) (2026-05-14)


### Features

* add session group to SaasStarterProperties ([6ca2ccf](https://github.com/SergioDelgado-tech/kotlin-saas-starter/commit/6ca2ccf3d6d725955c3698cf420f0efb991b7704))
* add SessionAutoConfiguration for Redis HTTP sessions ([5172fd1](https://github.com/SergioDelgado-tech/kotlin-saas-starter/commit/5172fd1fe98c77a35f0c8501aa58f0166afe415d))
* register SessionAutoConfiguration via AutoConfiguration.imports ([e3460e9](https://github.com/SergioDelgado-tech/kotlin-saas-starter/commit/e3460e9575e2d25a49b44b74755ed740ed830c79))


### Bug Fixes

* add @AutoConfigureAfter(RedisAutoConfiguration) to SessionAutoConfiguration ([41cf5ee](https://github.com/SergioDelgado-tech/kotlin-saas-starter/commit/41cf5ee9bba2d1e5eb2708c042d7f02eaa23ea62))
* correct GitHub Packages URL to use org owner SergioDelgado-tech ([c63ac80](https://github.com/SergioDelgado-tech/kotlin-saas-starter/commit/c63ac80bff0fc3200b5be6e9f0deda7efdcd900f))


### Documentation

* add workflow conventions and repo locations to CLAUDE.md ([bae110f](https://github.com/SergioDelgado-tech/kotlin-saas-starter/commit/bae110f8ce4104d970ca88b60ab8680d38184a9f))

## [0.2.1](https://github.com/serandel/kotlin-saas-starter/compare/v0.2.0...v0.2.1) (2026-05-10)


### Bug Fixes

* point GitHub Packages publish URL at the actual repo owner ([7d6ac54](https://github.com/serandel/kotlin-saas-starter/commit/7d6ac54aef035990af33f2afc745d49fa8440b1e))

## [0.2.0](https://github.com/serandel/kotlin-saas-starter/compare/v0.1.0...v0.2.0) (2026-05-09)


### Features

* add SaasStarterAutoConfiguration scaffolding ([c812005](https://github.com/serandel/kotlin-saas-starter/commit/c812005e911d6cbc1766b0c3d45546b3eaf93c1d))
* initial release ([0d6447f](https://github.com/serandel/kotlin-saas-starter/commit/0d6447f2079c4f60911279ecbc6ebafc3837105b))
* register SaasStarterAutoConfiguration via AutoConfiguration.imports ([8c43090](https://github.com/serandel/kotlin-saas-starter/commit/8c430907083c6bf204b1a6f769e7adbf8830879a))


### Bug Fixes

* add Gradle wrapper and adapt build to Gradle 9.5 ([4c196e6](https://github.com/serandel/kotlin-saas-starter/commit/4c196e6a5ab62a0b00caca7afa1f5394b2af8abf))
* align job-tenant propagation with JobRunr 7.x SPI ([4fd3630](https://github.com/serandel/kotlin-saas-starter/commit/4fd363006f552f11e94ee67638200185fc79d60d))
* declare junit-platform-launcher for Gradle 9 useJUnitPlatform ([bce5fae](https://github.com/serandel/kotlin-saas-starter/commit/bce5fae789b1264354f1bd44bdb410bcb8905bab))
* declare Konform dependency and fix validation against current API ([4fdbcac](https://github.com/serandel/kotlin-saas-starter/commit/4fdbcac024c273c4a0832569ea1713d542f2a5ee))
* track gradle-wrapper.jar by reordering .gitignore rules ([3dd690b](https://github.com/serandel/kotlin-saas-starter/commit/3dd690bd85ad2c19c12dacef708c2130a3130184))


### Documentation

* correct companion repo name to kotlin-saas-template ([3d52f32](https://github.com/serandel/kotlin-saas-starter/commit/3d52f32f5aa4d7e22bc13fc10c9739fe1f594b3a))
* document plan-as-issues workflow and link project board ([b159aae](https://github.com/serandel/kotlin-saas-starter/commit/b159aae24791488035661fd2fbd118e468751046))

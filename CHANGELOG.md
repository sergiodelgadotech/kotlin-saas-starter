# Changelog

## [0.7.0] (2026-06-12)

### Features

* **auth:** add `IdpUserDirectory.updateProfile` for self-service display-name editing

### Breaking Changes

* `IdpUserDirectory` is no longer a `fun interface` — SAM-lambda stubs must be converted to anonymous objects implementing both `findOrInvite` and `updateProfile`.

## [0.6.0](https://github.com/sergiodelgadotech/kotlin-saas-starter/compare/v0.5.0...v0.6.0) (2026-05-30)


### Features

* add BillingAutoConfiguration with BillingService and StripeWebhookHandler ([1fcebb8](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/1fcebb8ca9b97ab41281ffa8169fff05ab1a6050))
* add BillingPlan interface and SubscriptionStatus enum ([247c3dd](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/247c3dd706c5cc78b133a33d48f227218ba52bd7))
* add MemberRole interface with DefaultMemberRole enum ([4fa76a3](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/4fa76a37f304893a35703eaef917660790e3870b))
* add Organization and Member entities with repositories ([7d4a39c](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/7d4a39c4c2320985fb9fd2ee9dbbc249e1cb9a55))
* add OrganizationService, OrganizationAutoConfiguration, V100 migration ([5c0eddb](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/5c0eddbf81b5d0cf53164a0e1d3f71fd7a276fa6))
* add OrganizationValidations with Konform commands ([bc49e7e](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/bc49e7e1a3b4c5c6ebcd3e9663d0cba8a0599db6))
* add Subscription entity, repository, and billing properties ([d5134a0](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/d5134a06d8886403ae2a83e1985833a76a0e5323))
* add Tenant and RateLimit groups to SaasStarterProperties ([312dd4e](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/312dd4efe8ef464e7e3ed6951d89e451cb356dff))
* add WebMvcAutoConfiguration with property-driven interceptor paths ([d0d8535](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/d0d8535be8ac94ecc09b07bb540aa9c4905cdb58))
* **autoconfigure:** add SecurityAutoConfiguration and WebAutoConfiguration (Plan 8) ([8a9f3b0](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/8a9f3b0d81191a9ff3661c2598709d1a8b326ef5))
* **ci:** delete SNAPSHOT from GitHub Packages when a release is published ([cad728d](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/cad728dce119f33c9ff16f15adddde72dc9aa78a))
* **ci:** publish main-SNAPSHOT to GitHub Packages on every push to main ([c70f757](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/c70f757caacdd1833496ee130d146d1f31d7ffcc))
* declare RedisLockService bean in RedisAutoConfiguration ([d21d964](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/d21d964539ef08f38a7e4733d557dffbb8e53695))
* **devcontainer:** add devcontainer support for VS Code, Cursor, and JetBrains Gateway ([fe77b55](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/fe77b55a1c97d08a86d8ef95ed96f9dfc6db2791))
* **ratelimit:** make rate limits configurable per route ([3ddcafc](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/3ddcafc58d3fd5705bc830a1dbbf99aec8c07f6b)), closes [#24](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/24)
* ship subscriptions schema and register BillingAutoConfiguration ([51aab5e](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/51aab5e551efa966782b5d3dd0178bd1d3c75bf2))


### Bug Fixes

* **autoconfigure:** use name-based ConditionalOnMissingBean for jsonRedisTemplate ([f82fdbe](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/f82fdbe2a02a9a9fb98e4c5bbaf06419285963d9))
* **billing:** implement Persistable&lt;UUID&gt; on Subscription and wire AfterConvertCallback ([0661758](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/0661758dc6792f655069d8ba4fa812f9447a05e1))
* **ci:** derive SNAPSHOT version from release-please branch, skip on releases ([10954dc](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/10954dc383b6e5b1d6df1f64a3bad290a49f02e5))
* **deps:** update all non-major dependencies ([05b3563](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/05b3563a224686d639eb2ac55f9e2bc5aa310eda))
* **deps:** update all non-major dependencies ([5097f9c](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/5097f9c3a378f76b4a7b9e7e7f2a72e9cc3c197b))
* **devcontainer:** add workspaceMount, fix CI java version, add gradle cache volume ([97f081d](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/97f081d8c553cc718a24d60ea0bef0752b805e62))
* **lock:** release Redis lock atomically via DELEX IFEQ (Redis 8.4+) ([3c3a4d2](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/3c3a4d2bf71d1cd721ba72f7c6bd8858037ad532)), closes [#21](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/21)
* **ratelimit:** use SessionCallback to correctly pipeline sorted-set ops ([0ae9b65](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/0ae9b6580bc27d59ef0047196f4dc8355d1b84ea))
* remove redundant SQL index, fix KDoc reference, add test isolation ([2998d1d](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/2998d1d365973240b10daee13e9a24c14049b8c5))
* **test:** move TestBootApp out of organization package to avoid duplicate JDBC repo scan ([622476e](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/622476e81f60987c9323d312a37ef533e8ead82e))
* **validation:** rename validate to validateOrThrow ([489ffd2](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/489ffd2e496e4a225ff90582f62a106a072170e8))


### Documentation

* add memory for frontend architecture decision ([2105f8e](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/2105f8e8573a2c1ec1a45a24f80ce8b52a4095bc))
* document NOTICE regeneration process ([3dd1d18](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/3dd1d18dca739d53268774a66d275f3148f31bb0))
* **readme:** add CI, stable, and snapshot badges ([5f96cb0](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/5f96cb027731bc473925298a4facbbdc99a9df51))

## [Unreleased]

### Features

* add `SecurityAutoConfiguration`: auto-wires a cached, rate-limited `JwkProvider` and `JwtAuthFilter` (closes #23, #20)
* add `WebAutoConfiguration`: auto-wires `GlobalExceptionHandler` (closes #23)

### Bug Fixes

* JWKS provider is now cached + rate-limited via `JwkProviderBuilder` instead of being recreated per request (closes #20)

### BREAKING CHANGES

* Property keys `auth.jwks-url` and `auth.issuer` have moved to `saasstarter.security.jwks-url` and `saasstarter.security.issuer`. Update your `application.yml` accordingly.

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

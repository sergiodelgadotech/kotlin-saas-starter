# Changelog

## [0.7.0](https://github.com/sergiodelgadotech/kotlin-saas-starter/compare/v0.6.0...v0.7.0) (2026-06-12)


### Features

* **auth:** add IdpUserDirectory interface + email/role validation to InviteMemberCommand ([5c8cd98](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/5c8cd980a0b0013830931e763c0e91c2ae7424f3))
* **auth:** add updateProfile to IdpUserDirectory for self-service name editing ([e6e8ecc](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/e6e8eccfcc7ad1299c8527c5b02e4709f592bf26))
* **auth:** add ZitadelSessionBridgeFilter for OIDC session → auth_user_id bridge ([7b862cb](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/7b862cb2d24d4e5dedfc08561b6eddc639d78fb8))
* **auth:** autoconfigure ZitadelSessionBridgeFilter and OidcLogoutSuccessHandler ([1489136](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/14891369210e98ee0e9d640a01317ab4866afc3b))
* **billing:** add createCustomer and ensureSubscription to BillingService ([2b8dfcd](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/2b8dfcd77793342c2c0f35bcbc92c0324985dd1a)), closes [#53](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/53)
* **email:** add EmailService interface and ResendEmailService default implementation ([6e7024e](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/6e7024e4c5a19d9360adfad5603bf1e24fa92300))
* **email:** add fromName support to EmailService ([2190097](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/2190097dbb0487538149e0d3b27dcc52ee3fea6b))
* **observability:** add CorrelationIdFilter, TenantMdcInterceptor, and TenantObservationFilter ([c1d4bf6](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/c1d4bf63fd94289847b25869b295d178bef2a474))
* **observability:** add Micrometer Observation instrumentation to operational classes ([296a028](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/296a028493752af09477b355f60a984f55fc8652)), closes [#50](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/50)
* **org:** add MemberInvitedEvent domain event ([b702773](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/b702773fffdda023331b1f0c64e582c0c6f62841))
* **organization:** add profile fields to Member + updateProfile on login ([53e4f7a](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/53e4f7a9399704062f5678b2e6b96b978b8ecf6b))
* **org:** publish MemberInvitedEvent from inviteMember ([c9fa918](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/c9fa918cfbc98e5d5a461817f454b159d990b051))
* upgrade to Spring Boot 4, JobRunr 8, and Kotlin annotation-default-target ([876d67a](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/876d67af300fcc53a7201571ef15df82dff03e5f))


### Bug Fixes

* **billing:** resolve Stripe plan via configured plan-prices map ([62c091d](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/62c091d2c086fe886f3ee245c78abec6970c9c5c))
* **billing:** return nullable Subscription from currentSubscription() ([4a4f680](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/4a4f68054eb3605b9a6edaf3c1383cecc470d08e))
* **build:** correct POM license to LGPL v2.1 ([bb6099c](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/bb6099c2eae9d5279a212a799e2f74e23f1c5539)), closes [#52](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/52)
* **build:** mark license-report tasks as not configuration-cache compatible ([2f89086](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/2f89086db9fe4f43c4eba1a49912367262830250))
* **cache:** return String? from findOrganizationIdByUserId to avoid UUID cast on cache hit ([84cb636](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/84cb6368c19183dfa827ecf6c8cad1253c79849e))
* **deps:** update all non-major dependencies ([0f7c4cd](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/0f7c4cdec973cf10b2d4fc8dde201c7ebdab4a0e))
* **deps:** update all non-major dependencies ([099b9fa](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/099b9fa31bbe6dcc44fe1815a48bb04e3b03a83c))
* **deps:** update dependency io.mockk:mockk to v1.14.11 ([28622f8](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/28622f80149d40e9e99bc2f57faa6962f233fb00))
* **deps:** update dependency io.mockk:mockk to v1.14.11 ([3878393](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/3878393eeea5aaf98d5e39a6d0a6459883ef69ed))
* **deps:** update flyway to v12 ([f5eba05](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/f5eba0523bdfbb8dbb09336271db321f6704aa9a))
* **deps:** update flyway to v12 ([bf0bcb4](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/bf0bcb4563004e4a9354d9ce4e55185f305f1421))
* **migration:** drop DEFAULT after adding email column so bare INSERTs fail fast ([ecc71a8](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/ecc71a8b5d3c0177239a7f6a9a524d7aaef8fa5f))
* **observability:** add missing outcome tags and release observation ([9f913ac](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/9f913ac74278e2e8ca04bf20e4c736cb038780d4))
* **observability:** correct cardinality, error signals, and double-verification ([553da4e](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/553da4eef8769b5b8ad2e086369cb2040aca5243))
* **observability:** propagate tenant.id to HTTP server observations via request attribute ([3346b08](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/3346b0891be74f1f755b6451502b6c5065feff23))
* **observability:** split inner BeansConfig to avoid ConditionalOnBean timing issue ([d26ffa9](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/d26ffa91fcc818ea16d1d454a82cbbf6a3d4ae71))
* **organization:** make Member.email non-nullable (NOT NULL DEFAULT '') ([3273827](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/327382792bd693de08e7beeafa85376d48b0c4d3))
* **redis:** use EVERYTHING typing in Jackson serializer to handle final-class cache values ([6479a94](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/6479a941f32f5a1d2b1170e6040b872aa4a11fa8))
* **security:** read tenant from SecurityContext when JWT header is absent ([c18e428](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/c18e428dc78ac39f6e019e9ecce3bae2ce63d1fe))
* **test:** correct UUID→String comparison in OrganizationServiceIntegrationTest ([d3e6366](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/d3e63661b783a9d19c5a60080d23afa262a2c1f3))
* **web:** handle AsyncRequestTimeoutException and guard committed responses in GlobalExceptionHandler ([95af64a](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/95af64a7573782699b7ef6074cf7f3f2036f66bc))
* **web:** handle NoResourceFoundException explicitly in GlobalExceptionHandler ([2ffed12](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/2ffed12d2fb60e9385748dbd2a20ba6eeeffc869)), closes [#65](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/65)


### Performance

* enable Gradle configuration cache ([4b77525](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/4b775252e9b4e5350ac95d5fb19ac0834d2fdcc5))


### Refactoring

* **auth:** promote USER_ID_ATTR to internal and use safe-call chain in bridge filter ([4d3fab0](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/4d3fab0bf398487af091f39a55d9bbdd95b32518))
* **billing:** migrate checkout/portal session creation to StripeClient ([a1fa61e](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/a1fa61ec0cf788d5af11eccd036872fd769e32e1)), closes [#57](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/57)
* **billing:** migrate StripeClient calls to v1() namespace ([493e60d](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/493e60d40aedb2c1bc775b14b58e307b0b2e00e4))
* **migration:** fold member profile columns into V100 baseline ([e429712](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/e429712211244771d4db26e2222978ce38bb1d39))
* **migration:** fold V101 subscriptions into V100 single baseline ([defc5a6](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/defc5a668f6f1091e20bb0d005cb5858d3062a2e))
* **org:** document allowedRoles intent in OrganizationValidations ([049c46c](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/049c46ca7442d0bf7f096e10ae4433673a3ca442))
* **org:** remove plan field from Organization — subscription is the source of truth ([836fd78](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/836fd78d8fd648b91ecc641834e94324e87539ba))
* **ratelimit:** open RateLimiter and isAllowed for test-double subclassing ([f64b59b](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/f64b59bfd0e9e84231f63a6796676cfca8cc0ec4))
* **test:** replace @TestConfiguration MockBeans with @MockkBean ([b194e68](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/b194e68d5db452e3cdf6cc52666da0e9dfcb5b8e))


### Documentation

* **auth:** add KDoc to ZitadelOidcAutoConfiguration ([7a7f237](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/7a7f237243ad0d48ba6555dae524afdc2a1e8027))
* **auth:** declare Zitadel as the opinionated auth provider ([5a7c2d9](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/5a7c2d981a13cec89cee2847dcc01d14d130d88e))
* **auth:** remove stale Zitadel-specific KDoc references ([0b416fb](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/0b416fb1abd6862f00832fed44e157a8f810eb9b))
* document Spring Boot 4.0.x minimum requirement in CLAUDE.md ([a9860c8](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/a9860c85bcdde08dd3643c420281f130657a4721))
* **observability:** document built-in Redis and Jobrunr health indicators ([952a31d](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/952a31df52b800e090d1637f6665af97c94657e4)), closes [#51](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/51)
* refresh README — stale group, version, and quick-start ([d0d026b](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/d0d026bc02c305fcf8236b62def92b39ec93a932))
* refresh README quick-start with current coordinates ([faf3b06](https://github.com/sergiodelgadotech/kotlin-saas-starter/commit/faf3b06b52eb5816565f34520d815b380ec150fd)), closes [#55](https://github.com/sergiodelgadotech/kotlin-saas-starter/issues/55)

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

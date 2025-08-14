Stack:
MVP (Moxy)
RxJava2
Cicerone (Navigation)
Retrofit
Toothpick (DI)
ViewBinding
Room
Glide
Modularization: data, domain, app

API: https://stage.naukotheka.ru/api/a/swagger-ui/index.html?configUrl=/api/a/api-docs/swagger-config#/custom-controller/isEmailFree

How to run tests:
1) Open app/src/test/java
2) Open necessary file
3) click on the left side of class name and Run test file or Ctrl+Shift+F10

## Code structure
```
|---app - main application components
|   └---di - dependency injection components
|   |   └---modules - modules that provide or create dependencies to reuse
|   |   └---providers - provide implementation of necessary class instance
|   |   └---utils - helpers functions (interceptors, factories, logger and etc.)
|   |
|   └---global - base classes\interfaces
|   |   └---base - base classes
|   |   └---views - base interfaces for view
|   |
|   └---navigation - routes in app
|   |   └---AnimatableAppNavigator - class that overrides animation between screens
|   |   └---Screens - all screens of our application
|   |   └---TabRouterHolder - class that holds every tab in himself
|
|---data - Sends data required by the application to the domain layer, implementing a domain-provided interface.
|   └---cache - logging wrapper
|   |   └---base - base sharedpreferences cache abstract implementation
|   |   └---AnimatableAppNavigator - class that overrides animation between screens
|   └---repositories - logging wrapper
|   |   └---auth - work with login\registration\recovery
|   |   └---user_profile - work with user profile
|   |
|   └---services - classes for work with api services
|   |   └---AuthApiService - working with a/login-actions endpoint
|   |   └---UserProfileApiService - working with core/user_profile endpoint
|
|---domain - Runs business logic independent of other layers. Ideally, this is a pure Kotlin package with no android dependencies.
|   └---entities - contain common domain entities (ApiExceptions, StatusCodes and etc.)
|   └---interactors - contain business logic and transfer events to app module
|   └---repositories - contains interfaces for data module
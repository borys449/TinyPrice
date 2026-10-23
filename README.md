# TinyPrice — archivos del proyecto

Estos archivos van DENTRO del proyecto que crea Android Studio
(New Project → Empty Activity → nombre `IATinyFish`, paquete `com.apps.iatinyfish`, minSdk 23).
Android Studio ya genera: settings.gradle.kts, build.gradle.kts (raíz), gradlew, gradle/wrapper, themes.xml, iconos.

## 1. Copiar archivos (sobrescribe los generados)
- gradle/libs.versions.toml
- app/build.gradle.kts
- app/src/main/AndroidManifest.xml
- app/src/main/java/com/apps/iatinyfish/**  (borra los archivos de ejemplo que creó Android Studio en ui/theme si hay duplicados)
- app/src/test/.../DealsViewModelTest.kt
- supabase/ (después de correr `npx supabase init`, copia functions/search-products/index.ts y añade el bloque de config.toml)

En el build.gradle.kts de la RAÍZ asegúrate de tener:
    plugins {
        alias(libs.plugins.android.application) apply false
        alias(libs.plugins.kotlin.compose) apply false
    }
Y en gradle/wrapper/gradle-wrapper.properties:
    distributionUrl=https\://services.gradle.org/distributions/gradle-9.6.0-bin.zip

## 2. Probar la demo (sin red)
    ./gradlew :app:assembleDebug     (y Run en Android Studio)

## 3. Supabase + TinyFish
    npm init -y
    npm install --save-dev supabase
    npx supabase login
    npx supabase init
    npx supabase link --project-ref YOUR_PROJECT_REF
    # guarda TINYFISH_API_KEY en Dashboard → Edge Functions → Secrets
    # (o: copia supabase/.env.production.local.example → .env.production.local y)
    npx supabase secrets set --env-file supabase/.env.production.local
    npx supabase functions deploy search-products

Prueba:
    curl -X POST 'https://YOUR_PROJECT_REF.supabase.co/functions/v1/search-products' \
      -H 'Content-Type: application/json' -d '{"query":"Nintendo Switch OLED"}'

## 4. Activar búsqueda real en la app
    ./gradlew :app:assembleDebug -PuseRemoteDeals=true \
      -PedgeRuntimeBaseUrl=https://YOUR_PROJECT_REF.supabase.co/functions/v1
(o usa gradle.properties.example)

## 5. Tests
    ./gradlew :app:testDebugUnitTest

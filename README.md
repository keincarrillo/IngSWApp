# PinguBalance – Aplicación móvil

PinguBalance es una aplicación móvil desarrollada en **Kotlin** para **Android**.  
Este repositorio contiene el código fuente de la app y su configuración para ejecutarse en Android Studio.

---

## 📋 Requisitos previos

Antes de comenzar, asegúrate de contar con:

1. **Android Studio** (recomendado: versión 2023.1.1 o superior)  
   - Descarga: https://developer.android.com/studio  
   - Requisitos mínimos sugeridos:
     - 8 GB de RAM
     - 8 GB de espacio libre en disco

2. **Java Development Kit (JDK) 11 o superior**  
   - Descarga: https://www.oracle.com/java/technologies/downloads/

3. **Dispositivo Android o emulador**
   - Mínimo: **Android 12 (API 31)**
   - Recomendado: **Android 13 o superior**

---

## 🚀 Instalación

### Si es tu primera vez usando Android Studio

1. **Instalar Android Studio**
   - Descarga el instalador desde el enlace anterior.
   - Ejecuta el instalador y sigue las instrucciones.
   - Cuando se te pregunte por el tipo de instalación, elige **“Standard”**.

2. **Abrir el proyecto**
   - Abre Android Studio.
   - Selecciona **“Open an existing project”**.
   - Navega hasta la carpeta donde se encuentra el proyecto de **PinguBalance** y selecciónala.
   - Espera a que Android Studio sincronice y descargue las dependencias (puede tardar algunos minutos).

3. **Configurar un emulador (opcional)**
   - En Android Studio, ve a **Tools > Device Manager**.
   - Haz clic en **“Create Device”**.
   - Elige un dispositivo (por ejemplo, **Pixel 6**).
   - Selecciona una imagen del sistema **API 31 o superior**.
   - Finaliza la configuración.

### Si ya tienes experiencia

```bash
# Clonar el repositorio
git clone https://github.com/keincarrillo/PinguBalance.git
cd PinguBalance

# Compilar con Gradle (opcional, también puedes usar solo Android Studio)
./gradlew build


## 🔧 Compilación y Ejecución

1. **Compilar el proyecto**

   - En Android Studio, haz clic en el botón "Make Project" (martillo)
   - O usa el comando: `./gradlew assembleDebug`

2. **Ejecutar la aplicación**
   - Conecta tu dispositivo Android o inicia el emulador
   - Haz clic en el botón "Run" (triángulo verde)
   - Selecciona tu dispositivo o emulador
   - La aplicación se instalará y ejecutará automáticamente

## 📱 Características de la Aplicación

- Personalización de perfil de usuario
- Seguimiento de hábitos
- Interfaz intuitiva con Material Design 3
- Navegación entre pantallas
- Almacenamiento local de datos

## 🛠️ Estructura del Proyecto

app/
├── src/
│   ├── main/
│   │   ├── java/com/example/koalm/
│   │   │   ├── PersonalizarPerfil.kt
│   │   │   └── ... (otros archivos)
│   │   └── res/
│   │       ├── drawable/
│   │       ├── layout/
│   │       └── values/
├── build.gradle.kts
└── ...
```

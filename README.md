# Pokédex — Java MVP

Aplicación de escritorio desarrollada en Java + Swing que consume la API pública de PokéAPI en tiempo real para explorar información detallada de todos los Pokémon. Implementa el patrón arquitectónico MVP (Model–View–Presenter) con separación estricta de responsabilidades y ejecución asíncrona para no bloquear el EDT.

---

## 📌 Descripción

La aplicación permite buscar Pokémon por nombre o ID, navegar por una lista paginada, visualizar estadísticas base, consultar la cadena evolutiva completa, mostrar sprites animados y leer descripciones en español. La interfaz utiliza un tema oscuro reactivo: el color de fondo, las barras de estadísticas y el halo detrás de la imagen cambian dinámicamente según el tipo del Pokémon seleccionado.

---

## 🚀 Características

- 🔍 Búsqueda inteligente
- 📋 Lista lateral paginada (25 por página)
- 🎮 Sprites animados Gen V (fallback a artwork oficial)
- 🎨 Tema dinámico por tipo (con degradado si tiene doble tipo)
- 🔗 Cadena evolutiva interactiva
- 📖 Descripción en español (fallback a inglés)
- ⬅️➡️ Navegación rápida entre Pokémon

---

## 🏗 Arquitectura MVP

src/main/java/com/projectApirest/api/
├── Main.java
├── model/
│   └── Pokemon.java
├── service/
│   └── ApiService.java
├── view/
│   ├── PokemonView.java
│   └── PokemonMainFrame.java
└── presenter/
└── PokemonPresenter.java

| Capa | Función |
|------|---------|
| Model | Datos y consumo HTTP |
| View | Interfaz Swing |
| Presenter | Lógica y orquestación asíncrona |

---

## 📦 Requisitos

- Java 17 o superior
- Maven 3.6+
- Conexión a internet

---

## ▶️ Ejecutar el proyecto

git clone <URL_DEL_REPO>
cd pokedex-mvp
mvn clean package
java -jar target/pokedex-mvp-1.0.0.jar

---

## 🖥 Generar ejecutable .exe en Windows

Nota: Es necesario descargar Launch4j para convertir el JAR en EXE.

PASO 1: Descargar Launch4j
1. Ve a: https://sourceforge.net/projects/launch4j/files/launch4j-3/
2. Descarga launch4j-3.50-win64.exe
3. Ejecuta o descomprime

PASO 2: Generar el JAR
Desde IntelliJ:
1. Ctrl + Alt + Shift + S → Artifacts
2. + → JAR → From modules with dependencies
3. Módulo: pokedex-mvp
4. Main Class: com.projectApirest.api.Main
5. Opción: extract to the target JAR
6. OK → Apply → Build → Build Artifacts

Desde Maven:
mvn clean package

PASO 3: Configurar Launch4j
Pestaña Basic:
- Output file: C:\projects\PokemonApi\out\Pokedex.exe
- Jar: C:\projects\PokemonApi\out\artifacts\pokedex_mvp_jar\pokedex-mvp.jar
- Don't wrap the jar: Desmarcado

Pestaña JRE:
- JRE path: %JAVA_HOME%;%PATH%
- Min JRE version: 17
- Max JRE version: (vacío)
- JDK required: Desmarcado
- 64-Bit required: Marcado

Pestaña Header:
- Header type: gui

Pestaña Messages:
- Runtime JRE error: "Esta aplicación requiere Java 17 o superior. Instálalo desde https://adoptium.net/"

PASO 4: Guardar configuración
1. Ctrl + S
2. Nombre: pokedex-config.xml
3. Guardar en: C:\projects\PokemonApi\
4. Save

PASO 5: Generar el .exe
1. Click en ⚙️ (Build wrapper)
2. Se generará: C:\projects\PokemonApi\out\Pokedex.exe

PASO 6: Probar
1. Ve a C:\projects\PokemonApi\out\
2. Doble clic en Pokedex.exe

---

## 📊 Tamaño del ejecutable

| Configuración | Tamaño | Requisitos |
|---------------|--------|------------|
| Sin JRE | Pocos KB | Requiere Java 17+ |
| Con JRE embebido | 200-300 MB | Sin requisitos |

---

## 🔄 Alternativa: jpackage (Java 14+)

jpackage --input target/ --name Pokedex --main-jar pokedex-mvp-1.0.0.jar --main-class com.projectApirest.api.Main --type exe

---

## 📚 Dependencias

| Librería | Versión | Uso |
|----------|---------|-----|
| org.json:json | 20231013 | Parsing JSON |

---

## 🌐 Endpoints utilizados

| Endpoint | Uso |
|----------|-----|
| GET /pokemon/{id} | Datos principales |
| GET /pokemon-species/{id} | Descripción |
| GET /evolution-chain/{id} | Cadena evolutiva |
| GET /pokemon?limit=2000 | Lista completa |

---

## 🙌 Créditos

Datos: PokéAPI (https://pokeapi.co/)
Sprites: PokeAPI/sprites (https://github.com/PokeAPI/sprites)

---

## 📄 Licencia

MIT License
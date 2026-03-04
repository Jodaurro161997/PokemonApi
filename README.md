# Pokédex — Java MVP

Aplicación de escritorio desarrollada en **Java + Swing** que consume la [PokéAPI](https://pokeapi.co/) en tiempo real para explorar información detallada de todos los Pokémon. Implementa el patrón de arquitectura **MVP (Model-View-Presenter)** con separación estricta de responsabilidades.

---

## Descripción

La app permite buscar Pokémon por nombre o ID, navegar por listas paginadas, ver estadísticas, cadenas evolutivas, sprites animados y descripciones en español. El diseño es oscuro y reactivo: el color de fondo, las barras de stats y el halo detrás de la imagen cambian dinámicamente según el tipo del Pokémon seleccionado.

---

## Características

- 🔍 **Búsqueda inteligente** — búsqueda exacta por nombre o ID; si no hay coincidencia exacta, filtra todos los Pokémon que empiecen con el texto ingresado
- 📋 **Lista lateral paginada** — listas de 25 Pokémon con botones "Lista anterior / Lista siguiente"
- 🎮 **Sprites animados** — GIFs animados de la Gen V (Black/White); fallback al artwork oficial si no hay GIF
- 🎨 **Tema dinámico por tipo** — fondo, barras de stats y halo de imagen se colorean con el tipo del Pokémon; si tiene dos tipos, las barras muestran un degradado entre ambos colores
- 🔗 **Cadena evolutiva** — tira interactiva en la parte inferior con sprites y nombres clicables
- 📖 **Descripción en español** — flavor text extraído de `pokemon-species`, con fallback a inglés
- 🌍 **Tipos en español** — Fuego, Agua, Planta, Eléctrico, Psíquico, etc.
- ⬅️➡️ **Navegación rápida** — flechas para pasar al Pokémon anterior/siguiente sin usar la lista

---

## Arquitectura MVP

```
src/main/java/com/projectApirest/api/
├── Main.java                        ← Entry point, wiring MVP
├── model/
│   └── Pokemon.java                 ← Record con todos los datos del Pokémon
├── service/
│   └── ApiService.java              ← Capa de datos: HTTP + parsing JSON
├── view/
│   ├── PokemonView.java             ← Interfaz contrato de la Vista
│   └── PokemonMainFrame.java        ← Implementación Swing (UI completa)
└── presenter/
    └── PokemonPresenter.java        ← Lógica de negocio, acciones del usuario
```

| Capa | Responsabilidad |
|---|---|
| **Model** | Datos puros. `Pokemon` es un record inmutable. `ApiService` consume la PokéAPI con `HttpClient` (Java 11+) |
| **View** | Solo UI. No conoce la API ni la lógica. Delega toda acción al Presenter |
| **Presenter** | Conecta Model y View. Maneja async con `ExecutorService`, nunca bloquea el EDT |

---

## Requisitos

- Java 17 o superior
- Maven 3.6+
- Conexión a internet (consume PokéAPI en tiempo real)

---

## Instalación y ejecución

```bash
# Clonar o copiar los archivos del proyecto
cd pokedex-mvp

# Compilar y empaquetar (fat JAR con dependencias)
mvn package

# Ejecutar
java -jar target/pokedex-mvp-1.0.0.jar
```

---

## Dependencias

| Librería | Versión | Uso |
|---|---|---|
| `org.json:json` | 20231013 | Parsing de respuestas JSON de la PokéAPI |

El JAR se genera como **fat JAR** (shaded) con el plugin `maven-shade-plugin`, sin necesidad de instalar dependencias por separado.

---

## Endpoints utilizados

| Endpoint | Uso |
|---|---|
| `GET /pokemon/{id}` | Datos principales del Pokémon |
| `GET /pokemon-species/{id}` | Descripción (flavor text) en español/inglés |
| `GET /evolution-chain/{id}` | Cadena evolutiva completa |
| `GET /pokemon?limit=2000` | Lista completa de nombres (caché en memoria para búsqueda por prefijo) |
| GitHub raw sprites | GIFs animados Gen V |

---

## Capturas de pantalla

> _La interfaz muestra el Pokémon con su sprite animado, halo de tipo, estadísticas con barras degradadas, descripción en español y cadena evolutiva interactiva en la parte inferior._

---

## Créditos

- Datos: [PokéAPI](https://pokeapi.co/) — API abierta y gratuita
- Sprites animados: [PokeAPI/sprites](https://github.com/PokeAPI/sprites) en GitHub

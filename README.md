# O'Prompter (Android)

**O'Prompter** es una aplicación de teleprompter profesional para Android, diseñada y optimizada para ofrecer un rendimiento fluido y compatible desde **Android 4.2 (API 17)** en adelante.

---

## 🚀 Características Principales

1. **Modo Prompter y Edición**:
   - Visualización de texto a gran escala con espaciado de línea optimizado para lectura a distancia.
   - Modo edición integrado con soporte para formato Markdown simplificado:
     - `*negrita*`
     - `_cursiva_`
     - `~tachado~`
     - `+subrayado+`

2. **Gestión de Archivos**:
   - Carga rápida de archivos de texto (`.txt`) desde la carpeta dedicada `/sdcard/OPrompter`.
   - Carga de archivos externos desde cualquier ubicación del dispositivo mediante el explorador de archivos del sistema.
   - Guardado de textos directamente en la carpeta `/sdcard/OPrompter`.

3. **Navegación Rápida de Archivos**:
   - Barra de navegación unificada integrada con botones **`< Anterior`** y **`Siguiente >`** y el nombre del archivo actual centrado.
   - Búsqueda y ordenamiento alfabético automático de los archivos `.txt` en la carpeta `OPrompter`.
   - Alertas modales informativas al alcanzar el inicio o el fin de la lista de archivos.

4. **Controles de Avance (Auto-Scroll)**:
   - **Toque corto en pantalla**: Avanza el texto exactamente una altura de pantalla en modo lectura.
   - **Botón flotante ajustable**: Permite avance por toques y modo arrastre mantenido (long click + drag) para posicionarlo libremente en la pantalla.

5. **Ciclo de Vida y Cierre**:
   - Pantalla de presentación (*Splash Screen*) con enlace al creador (*Dédalo*).
   - Opción en el menú para salir limpiamente de la aplicación (`Salir de la aplicación`).

---

## 🛠️ Especificaciones Técnicas y Compatibilidad

- **`minSdk`**: `17` (Android 4.2 Jelly Bean MR1).
- **`targetSdk` / `compileSdk`**: `34`.
- **Arquitectura de Compatibilidad**:
  - Implementación de `MultiDexApplication` para soporte completo en Dalvik (API 17).
  - *Core Library Desugaring* para soporte de características de Java en versiones antiguas.
  - Librería `appcompat:1.2.0` optimizada para API 14+.

---

## 📂 Estructura del Proyecto

- `com.dedalo.oprompter.App`: Clase de aplicación que inicializa MultiDex y soporte vectorial.
- `com.dedalo.oprompter.SplashActivity`: Pantalla de bienvenida e inicio.
- `com.dedalo.oprompter.MainActivity`: Actividad principal, control de prompter, edición, menú y navegación de archivos.
- `com.dedalo.oprompter.MarkdownParser`: Analizador sintáctico de formatos Markdown para Spannables.

## 📄 Licencia

Este proyecto se distribuye bajo los términos de la **Licencia MIT**. 

```text
MIT License

Copyright (c) 2026 Dédalo Integraciones

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES / FOREIGN
LIABILITY WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```


# The Night Skyline 🌃 | 3D Interactive Environment

An immersive 3D interactive visualization project developed using **JavaFX**. This project creates a dynamic night city atmosphere, combining advanced mathematical algorithms with real-time rendering.

## 📺 Project Demo
[![Watch the Demo](https://img.shields.io/badge/🎥-Watch_Video_Demo-red?style=for-the-badge)](https://youtu.be/xecmKeJvS_g)

*Click the button above to see the 3D City Skyline in action, including the rainfall system and cinematic camera paths.*

---

## 🎮 Key Features

### 🏛️ Advanced 3D Rendering & Custom Meshes
- **Architectural Diversity:** Beyond standard primitives, this project features custom-built structures like a **Louvre-inspired Dome** and Pyramids.
- **TriangleMesh Mastery:** Implemented precise vertex-and-face mapping to construct complex geometric shapes.

### 🧪 Mathematical Texturing (Procedural)
- **Perlin Noise:** Implemented noise algorithms (using Fade and Hash functions) to generate procedural marble and stone textures directly through code, ensuring high-fidelity visuals without relying solely on external images.

### 🌧️ Dynamic Weather System
- **Real-time Particles:** A dedicated **Particle System** driven by an `AnimationTimer` to simulate realistic rainfall. Optimized to handle multiple entities with fluid motion.

### 🎥 Cinematic & Manual Navigation
- **Cubic Bezier Curves:** Automated drone-view camera paths are calculated using Bezier interpolation for smooth, non-linear cinematic movements.
- **Full Interactive Control:** Manual navigation for exploring the 3D space.

---

## 🕹 Interactive Controls

| Key | Action | Key | Action |
| :--- | :--- | :--- | :--- |
| **W / S** | Move Forward / Backward | **Q / E** | Move Up / Down (Height) |
| **A / D** | Move Left / Right | **R** | **Toggle Rainfall** |
| **Arrows** | Rotate Camera View | **C** | **Toggle Cinematic Mode** |

---

## 🛠 Technical Stack
- **Language:** Java
- **Framework:** JavaFX (3D Graphics Engine)
- **Concepts:** Perlin Noise, Bezier Pathing, Lighting Models (Point/Ambient), Mesh Triangulation.

---

## 📂 Project Organization
- `src/`: Core logic and 3D scene construction.
- `resources/img/`: Texture assets for architectural rendering.
- `manifest.mf`: Project metadata and configuration.

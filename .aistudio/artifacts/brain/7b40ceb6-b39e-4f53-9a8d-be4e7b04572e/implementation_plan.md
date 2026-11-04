# Neon Hydro Serpent: Deep Abyss (أفاعي النيون المائية)

A high-octane bioluminescent aquatic snake game built for Android with Jetpack Compose. Players pilot a glowing sea serpent in the deep dark ocean abyss using an on-screen virtual joystick, devouring ambient plankton orbs and rival aquatic serpents to dominate the depths.

### User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Choices from Clarification Stage:**
> - **Visual Atmosphere:** Deep ocean abyss with vibrant bioluminescent neon glows, caustic underwater lighting, and floating light spores.
> - **Controls & Abilities:** Smooth virtual analog joystick on the bottom-left accompanied by a high-speed Turbo Dash button on the bottom-right that expels mass as glowing neon trail bubbles.
> - **Game Mode:** Endless survival mode with live ranking, dynamic bot respawns, and local record tracking (longest length, kills, highest score).

---

### 1. Overview & Core Concept

- **What It Does**: The player controls a bioluminescent aquatic serpent in a vast underwater arena. Swarms of intelligent AI serpents of varying sizes and temperaments roam the deep sea. By consuming floating light orbs and cutting off rival serpents so their heads collide with your body, you destroy them into clusters of radiant energy to grow colossal.
- **Target Audience**: Fans of arcade io-style action games (Slither.io, Snake) who appreciate fluid physics, striking neon aesthetics, and responsive tactile mobile controls.
- **Key Value**: 60fps hardware-accelerated Compose Canvas rendering with organic multi-segment inverse kinematics, dynamic camera tracking, responsive dual-handed controls, and engaging predatory AI.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Launch & Menu Screen**:
   - Atmospheric deep-sea title screen with ambient floating particles and glowing serpent animation.
   - Quick "Play Now" action, Snake Skin customization selector (Electric Cyan, Bio-Emerald, Plasma Violet, Solar Amber, Coral Neon), and High Scores / Hall of Fame.
2. **In-Game Arena**:
   - Camera follows the player's snake head with smooth damping and dynamic zoom-out as the snake grows larger.
   - **HUD Overlay**:
     - Top-Right: Live Arena Leaderboard (top 5 serpents in real-time).
     - Top-Left: Current Length, Mass, and Kill Counter.
     - Bottom-Left: Ergonomic Virtual Analog Joystick (touch-down anywhere in the left quadrant or fixed position).
     - Bottom-Right: Glowing Turbo Dash Button with cooldown/mass gauge.
     - Top-Center/Corner: Radar Mini-Map depicting world boundaries, food clusters, and nearby threats.
3. **Game Over & Respawn**:
   - Dramatic neon disintegration effect when colliding head-first into a rival serpent.
   - Immediate stats summary card (Rank Achieved, Final Length, Kills, Survival Time, New High Score badge).
   - Single-tap "Dive Again" instant restart with zero loading delay.

#### Visual Identity & Theme
- **Aesthetic Direction**: Deep-Sea Cyberpunk / Bioluminescent Marine Life. Inspired by abyssal siphonophores, neon jellyfish, and glowing sea creatures.
- **Color Palette**:
  - `Abyss Background`: `#030712` (midnight obsidian) to `#081426` (deep marine trench).
  - `Player Neon Accent`: `#00F0FF` (electric cyan) and `#00FFA3` (bioluminescent aqua).
  - `Rival Bot Palettes`: `#FF007A` (hyper magenta), `#7928CA` (plasma violet), `#FFB800` (solar gold), `#00E5FF` (arctic flare).
  - `Food / Energy Orbs`: Pulsating multi-colored glowing spheres with soft radial halos (`#FF2E93`, `#00FFD1`, `#FFE600`, `#9D00FF`).
  - `Water Effects`: Soft bubble trails, caustic light grid lines, and boundary shockwaves.
- **Typography & UI**:
  - Clean futuristic typography with high legibility on dark glassmorphism surfaces.
  - Semi-transparent HUD panels with subtle neon border glows (`1.dp` borders with alpha).

---

### 3. Key Product Decisions & Trade-Offs

- **Rendering Pipeline (Custom Compose Canvas vs Game Engine)**:
  - *Chosen Approach*: Pure Jetpack Compose `Canvas` rendering driven by a `LaunchedEffect` game loop loop synced to frame clock (`withFrameNanos`).
  - *Why*: Eliminates heavy external dependencies, keeps the APK lightweight (<15MB), ensures fast compilation, and provides seamless interoperability with Compose UI overlays (HUD, dialogs, joystick).
- **Snake Movement Physics (Segment Distance Constraints)**:
  - *Chosen Approach*: Verlet/Distance-constraint inverse kinematics. The head moves continuously along the target joystick angle; every trailing segment is constrained to maintain an exact radius from the segment ahead, with bezier curves smoothing the body outline.
  - *Why*: Delivers an authentic fluid serpentine swimming motion without unnatural sharp bends or jittering.
- **Spatial Partitioning & Collision Optimization**:
  - *Chosen Approach*: Grid-based spatial hash or radius proximity checks. Only checks collisions between serpent heads and nearby body segments within the visible camera viewport plus a safety buffer.
  - *Why*: Maintains a rock-solid 60 FPS even with 15+ bot serpents and hundreds of food pellets on the field.
- **Local Persistence Strategy**:
  - *Chosen Approach*: Room Database for persistent high scores, lifetime kill stats, and unlocked skin selections.
  - *Why*: Instant local offline durability that preserves player records across app restarts without requiring internet access or cloud sign-in.

---

### 4. Technical Architecture & Data Strategy (Technical Reference)

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                         ┌──────────▼──────────┐
                         │   GameNavigation    │
                         └────┬─────────────┬──┘
                              │             │
              ┌───────────────▼─┐         ┌─▼──────────────────┐
              │   MenuScreen    │         │     GameScreen     │
              │  (Skins/Stats)  │         │  (Arena + Overlay) │
              └─────────────────┘         └──────────┬─────────┘
                                                     │
                                          ┌──────────▼─────────┐
                                          │   GameViewModel    │
                                          └──────────┬─────────┘
                                                     │
                 ┌───────────────────────────────────┼────────────────────────────────────┐
                 │                                   │                                    │
        ┌────────▼────────┐                 ┌────────▼────────┐                  ┌────────▼────────┐
        │   GameEngine    │                 │  InputManager   │                  │ ScoreRepository │
        │ (World/Physics) │                 │(Joystick/Boost) │                  │     (Room)      │
        └────────┬────────┘                 └─────────────────┘                  └─────────────────┘
                 │
  ┌──────────────┼──────────────┐
  │              │              │
┌─▼─────────┐ ┌──▼────────┐  ┌──▼────────┐
│  Player   │ │  BotAI    │  │FoodManager│
│  Serpent  │ │ Controller│  │ (Pellets) │
└───────────┘ └───────────┘  └───────────┘
```

#### Data Model & Entities
- **Serpent**: `id`, `name`, `colorScheme`, `headPosition`, `bodySegments: List<Offset>`, `angle`, `speed`, `length`, `radius`, `isAlive`, `isBoosting`, `score`.
- **FoodPellet**: `id`, `position: Offset`, `color: Color`, `energyValue: Float`, `radius: Float`, `glowPhase: Float`.
- **GameRecord (Room)**: `id`, `timestamp`, `score`, `length`, `kills`, `survivalSeconds`, `skinUsed`.

#### Interactive Component & State Mapping
- **Virtual Joystick (`JoystickView`)**: Captures touch drag offsets on the bottom-left canvas, normalizes the vector to `(dx, dy)`, and updates the player serpent's target steering angle.
- **Turbo Button (`DashButton`)**: When held, boosts serpent speed by 1.8x, spawns glowing bubble mass behind the tail, and decrements snake length gradually until released or minimum length reached.
- **World & Camera**: Translates world coordinates `(0..WORLD_SIZE)` relative to the player's head, keeping the player centered while rendering surrounding arena elements.

---

### 5. Verification Plan

1. **Compilation Check**: Run `compile_applet` to verify clean build without syntax, import, or type errors.
2. **Game Loop & Performance**: Confirm fluid 60fps rendering of multi-segmented serpents and dynamic food orbs.
3. **Collision & Rule Check**: Verify head-to-body collisions destroy serpents into food clusters, food consumption increases body length, and boost drains mass as expected.
4. **Touch Input**: Validate virtual joystick tracking across different screen touch coordinates and boost button states.

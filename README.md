This repository contains a working Minecraft mod focused on custom entity AI behavior and is intended for code-level assistance, refactoring, and safe architectural improvements using AI tools.
# 👁️ Herobrine Minecraft Mod

> A custom Minecraft entity modification that extends Herobrine behavior using advanced AI-style logic built on Minecraft’s native Goal system.

---

## ⚡ Overview

This project is a Minecraft mod that modifies the behavior of the Herobrine entity using Java and the Minecraft modding API.

It focuses on:
- Custom AI behavior using Goal-based logic
- Entity-driven combat system
- World interaction and spawn control
- Client-server visual effects and rendering

The system is implemented directly inside Minecraft’s tick-based entity update loop.

---

## 🧠 Core Systems

### 👤 Entity AI System
- Custom `HerobrineEntity` extending Minecraft `Monster`
- Goal-based AI using built-in Minecraft AI framework
- Includes:
  - Chase behavior (`HerobrineChaseGoal`)
  - Idle roaming behavior
  - Player targeting logic

---

### ⚔️ Combat Logic
- Custom attack handling inside entity logic
- Attack cooldown system
- Dynamic combat state inside tick updates
- Health-based behavior adjustments

---

### 🌍 World Interaction
- Interaction with environment through entity logic
- Spawn control via `HerobrineSpawnManager`
- Block/world-related logic handled inside server-side entity updates

---

### 📡 Registry & Systems
- Entity registration (`ModEntities`)
- Sound system integration (`ModSounds`)
- Command handling (`HerobrineCommands`)

---

### 🌐 Networking
- Custom payload system (`HerobrineVisualEffectPayload`)
- Client-server communication for visual effects

---

### 🎮 Client Features
- Custom entity models (`HerobrineModel`)
- Visual overlays (`HerobrinePortalOverlay`)
- Entity rendering for special states and disguises

---

## 📁 Project Structure
com.engai.herobrine
├── ClassicHerobrineMod.java
├── entity/
│ ├── HerobrineEntity.java
│ ├── HerobrineChaseGoal.java
│ ├── HerobrineDisguise*.java
├── registry/
├── world/
├── command/
├── network/
├── client/

---

## ⚙️ Architecture Notes

- Built entirely on Minecraft’s entity tick system
- AI logic is embedded directly inside entity lifecycle
- Behavior is driven by Goal system + internal state tracking
- Designed for real-time gameplay interaction

---

## 🎯 Purpose of the Project

This mod is a personal experimental project focused on:

- Understanding Minecraft AI systems deeply
- Building custom entity behaviors beyond vanilla mechanics
- Working with real-time game tick logic
- Extending gameplay through custom entity intelligence

---

## 🧪 Current State

- Actively under development
- Systems are functional but still evolving
- AI behavior is being expanded incrementally
- Architecture may change as features are added

---

## 🛠️ Tech Stack

- Java
- Minecraft Modding API (Fabric/Forge-style structure)
- Entity Goal AI system
- Client/Server networking system

---

## ⚠️ Disclaimer

This is a fan-made independent Minecraft modification.
Not affiliated with Mojang or Microsoft.

---

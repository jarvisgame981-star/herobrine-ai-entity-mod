# Herobrine Minecraft Mod

A custom Minecraft mod that modifies and extends the behavior of the Herobrine entity using Java and the Fabric/Forge modding framework.

## Overview

This project implements a custom Herobrine entity with modified AI behavior, combat logic, and world interaction systems inside Minecraft.

The goal of the mod is to experiment with advanced entity behavior by extending the default Minecraft AI system and introducing custom logic layers inside a single entity implementation.

## Current Features

### Entity System
- Custom Herobrine entity extending the base Minecraft `Monster` class
- Modified health, attack, and movement attributes
- Custom AI goals using Minecraft Goal system

### AI Behavior
- Basic target detection using player proximity
- Custom chase behavior via `HerobrineChaseGoal`
- Random movement and look-around behavior when idle
- Conditional targeting of nearest players

### Combat System
- Custom attack damage logic
- Attack cooldown system
- Health-based behavioral adjustments (internal logic inside entity tick)

### World Interaction
- Interaction with blocks and environment through entity logic
- Custom spawn management via `HerobrineSpawnManager`

### Registry System
- Entity registration handled through `ModEntities`
- Sound integration through `ModSounds`

### Network System
- Custom payload handling via `HerobrineVisualEffectPayload`
- Client-side visual effect support

### Client Rendering
- Custom entity model (`HerobrineModel`)
- Portal/overlay visual effects (`HerobrinePortalOverlay`)
- Entity renderer support for disguised forms

## Project Structure
com.engai.herobrine
├── ClassicHerobrineMod.java
├── entity/
├── registry/
├── world/
├── command/
├── network/
├── client/

## Notes

- This project is still under active development.
- Some systems are experimental and tightly integrated inside the entity logic.
- AI behavior is implemented using Minecraft’s built-in tick system and custom goal classes.

## Tech Stack

- Java
- Minecraft Modding API (Fabric/Forge style structure)
- Entity AI Goal System
- Client/Server Networking

## Purpose

This mod is a personal experimental project focused on:
- Understanding Minecraft entity AI systems
- Building custom behavioral logic for mobs
- Exploring real-time game tick decision-making systems

---

## Disclaimer

This is an independent fan-made modification and is not affiliated with Mojang or Microsoft.
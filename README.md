# MonkeyUtils

A Minecraft plugin for Paper servers, developed using the Paper API.

## Overview

MonkeyUtils is a personal project designed to transfer player data and properties between Minecraft accounts.

It provides a command-based system to initiate account transfers, coordinating data migration across multiple third-party plugins through their respective APIs.

## Features & Integrations

- **Account Transfer:** command-based transfer between two player accounts using `/cambioaccount`.
- **LuckPerms:** transfer permissions and primary group information.
- **SuperiorSkyblock2:** transfer player data and island-related information.
- **Vault:** integration with the server economy system for balance transfers.
- **Quarry and ODailyQuests:** planned integrations for additional player data.
- **WorldGuard:** considered for region ownership and membership management.

## Technologies

- Java 21
- Paper API 1.21.4
- Gradle
- LuckPerms API
- SuperiorSkyblock2 API
- Vault API
- CompletableFuture and Bukkit Scheduler

## Project Status

This project is an unfinished personal prototype and is no longer actively developed.

It was developed as a learning experience focused on third-party API integration, asynchronous programming, and coordinating player data transfers across independent Minecraft plugins.

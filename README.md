# NordPing

Changes the version label shown in the multiplayer server list. Runs on Velocity; it does not change the MOTD, player count, protocol or login behavior.

## Configuration

Edit `plugins/NordPing/config.properties`, then run `nordping reload` in the proxy console or `/nordping reload` in game.

## Permissions

| Permission | Allows |
| --- | --- |
| `nordping.admin` | `/nordping reload` |

Player access comes from the Velocity permission provider. NordPing does not register a default player grant. Permissions configured only on a Paper/Folia backend do not grant proxy permissions.

## Build and installation

Use Maven 3.9+ and JDK 25. Build instructions are in [BUILDING.md](BUILDING.md). Stop the proxy before replacing the JAR and retain its installed configuration.

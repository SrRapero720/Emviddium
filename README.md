# Emviddium - Nvidium Port

[![Modrinth](https://img.shields.io/modrinth/dt/nvidium?logo=modrinth&label=Nvidium%20downloads&style=for-the-badge)](https://modrinth.com/mod/nvidium)

# WHAT IS EMVIDDIUM
Emviddium is a Forge port of Nvidium. 
Currently, this port is a PoC because I CANT TEST IT.
I had plans to do a proper release until I got the needed hardware (GTX 1660) 
to properly test it, fix bugs and contribute to the official project)

Maybe you can see me more on embeddium than Enviddium, 
that's because backport needs to move modern sodium code to the older versions
(or may some rid off purposeless Nvidium code if backported sodium can't use new code)

### BACKPORTING STATUS
|  VERSION   | STATUS |           INFORMATION           |
|:----------:|:------:|:-------------------------------:|
|   1.20.1   |   ❎    |      Needs tests but runs       |
|   1.19.2   |   ⛔    |               ---               |
|   1.18.2   |   🔃   | Delayed by Frustum and Viewport |
|   1.16.5   |   ⛔    |               ---               |
|   1.12.2   |   N/A    |               ---               |
| ~~1.7.10~~ |  N/A   |               ---               |

✅ - Done
❎ - Done Untested
🔃 - W.I.P
⛔ - STOPPED


## WHAT IS NVIDIUM
> **IMPORTANT:** Requires sodium and a nvidia gtx 1600 series or newer to run (turing+ architecture)

Nvidium is an alternate rendering backing for sodium, it uses cutting-edge nvidia features to render huge amounts of
terrain geometry at very playable frame rates.

<img src="https://github.com/user-attachments/assets/f8e04de5-28fa-4015-91b4-c7e888982666" alt="Transparent" width="100" height="100" align="right" />
<br><br>

# Anti-Autoclicker V3

This is the successor to [AntiAC](https://github.com/GodCipher/Anti-AutoClicker). Rebuilt from the ground up, AntiAC v3 is set to shine once again in the near future. Its arsenal has been expanded and will soon be deployed in the fight against autoclickers. Stay tuned!

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Dependencies](#dependencies)
- [Contributing](#contributing)
- [License](#license)

## Introduction

Anti-Autoclicker V3 (AntiAC v3) is a Minecraft plugin designed to detect and prevent the use of autoclickers. Autoclickers give players an unfair advantage by automating mouse clicks, which can disrupt the balance of gameplay. AntiAC v3 aims to provide server administrators with a robust tool to maintain fair play.

## Features

- **Advanced Detection Algorithms**: Utilizes sophisticated algorithms to detect autoclicking behavior.
- **Real-time Monitoring**: Continuously monitors player actions to detect suspicious activity.
- **Customizable Settings**: Allows server administrators to fine-tune detection parameters.
- **Integration with Other Plugins**: Compatible with various other plugins for enhanced functionality.
- **Detailed Logging**: Provides comprehensive logs for detected incidents.

## Installation

To install AntiAC v3, follow these steps:

1. Download the latest release from the [releases page](https://github.com/GodCipher/Anti-AutoClicker/releases).
2. Place the `AntiAC.jar` file in your server's `plugins` directory.
3. Restart your server to load the plugin.

**NOTE:** You will need Java 21 or higher to run AntiAC v3.

## Configuration

After installation, you can configure AntiAC v3 by editing the `config.yml` file located in the `plugins/AntiAC` directory.
The checks can be configured in the `checks` directory within `plugins/AntiAC`.

## Usage
Once installed and configured, AntiAC v3 will automatically start monitoring player actions. You can use the following commands to interact with the plugin:

- /antiac check <player>: Manually checks a player for autoclicking behavior.
- /antiac checks: Lists all available checks and an option to toggle them.

## Contributing
We welcome contributions from the community! To contribute to AntiAC v3, follow these steps:  
- Fork the repository.
- Create a new branch for your feature or bugfix.
- Commit your changes and push them to your fork.
- Submit a pull request with a detailed description of your changes.

## License
AntiAC v3 is licensed under the MIT License. See the LICENSE file for more information.

# 🌿 Easeon - DeepRefill
**This mod requires <a href="https://modrinth.com/mod/easeon-ss-core" target="_blank">EaseonSS-Core</a>**

## Overview
Deep Refill is a server-side Fabric mod that automatically refills items in your hand from your inventory, shulker boxes in your inventory, and ender chest. Perfect for builders and players who want seamless item restocking without manual inventory management.

<br/>

## Commands
All commands require OP level 2 permission.

| Command                                    | Description                        |
| ------------------------------------------ | ---------------------------------- |
| /easeon deeprefill                         | View current settings              |
| /easeon deeprefill inventory <on/off>      | Enable/disable inventory search    |
| /easeon deeprefill shulkerbox <on/off>     | Enable/disable shulker box search  |
| /easeon deeprefill enderchest <on/off>     | Enable/disable ender chest search  |
| /easeon deeprefill <on/off>                | Enable/disable all search options  |

<details>
<summary>Configuration</summary>

```json
{
  "value": 7,          // Search flags: 1=inventory, 2=shulkerbox, 4=enderchest (default: 7=all)
  "requiredOpLevel": 2 // Requires a server restart to take effect.
}
```
`config/easeon/easeon.ss.deeprefill.json`

</details>

## Commands
All commands require OP level 2 permission.



<br/>

---
### 🔗 More Easeon Mods
Looking for more lightweight and practical mods in the same style?  
Check out other Easeon series mods <a href="https://modrinth.com/user/Teron" target="_blank" rel="noopener noreferrer">here</a>.
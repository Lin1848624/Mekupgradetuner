# 通用机械：升级调校（Mekanism Upgrade Tuner）

Minecraft 1.20.1 / Forge 47.4.10 / Mekanism 10.4.x

## 功能

- 7 种升级的安装上限可配置：加速、能量、气体、消音、过滤、区块加载、造石。
- 三个独立的耗电开关：
  - 加速升级完全不加耗电（1.0.0 的行为，默认关）；
  - 加速带来的耗电上涨封顶（默认开，最多 10 倍）；
  - 能量升级的省电效果封顶（默认开，最低 0.1 倍，即最多省 90%）。
- 升级界面、物品说明、存档读写都使用配置后的上限。
- 游戏内指令实时改所有配置，并写回配置文件（1.2.0 新增）。

默认值（1.2.0）：加速 12、能量 16、气体 16、消音 4、过滤 1、区块加载 1、造石 1；
加速不耗电关、加速耗电上限开（10 倍）、能量省电下限开（0.1 倍）。
效果举例：
- 8 加速 + 8 能量 = 1 倍基础耗电；
- 12 加速 + 0 能量 = 10 倍基础耗电（封顶）；
- 0 加速 + 16 能量 = 0.1 倍基础耗电（封顶）。

## 游戏内指令

单人 / LAN 主机直接可用；专用服务器需要 OP（权限等级 2）。

```text
/mekupgradetuner get
/mekupgradetuner set <配置项> <值>
/mekupgradetuner toggle <开关项>
```

例子：

```text
/mekupgradetuner set speedMax 16
/mekupgradetuner set speedEnergyMultiplierCap 5.0
/mekupgradetuner toggle removeSpeedEnergyCost
/mekupgradetuner get
```

改完立即生效（已加载的机器下一 tick 重算），并写入 `config/mekupgradetuner-common.toml`，重启保留。

配置项：`speedMax`、`energyMax`、`gasMax`、`mufflingMax`、`filterMax`、`anchorMax`、`stoneGeneratorMax`、
`speedEnergyMultiplierCap`、`energyConsumptionFloor`。

开关项：`removeSpeedEnergyCost`、`limitSpeedEnergyIncrease`、`limitEnergyReduction`。

## 安装

1. 把 `mekupgradetuner-1.2.0.jar` 放进实例的 `mods` 文件夹。
2. 如果你装过 1.0.0：先删掉旧的 `config/mekupgradetuner-common.toml`，让新版按平衡默认值重新生成。
   从 1.1.0 升上来不用删，配置项是一样的。
3. 启动游戏，生成 `config/mekupgradetuner-common.toml`。
4. 用指令改立即生效；直接改配置文件则重启游戏生效。

单人游戏只需客户端安装。开 LAN 或服务器时，服务端和每个客户端都要安装，并使用相同的配置。

## 注意

- 调低上限或卸载模组时，已装超出的升级会被裁掉且不退还。操作前备份存档，并先把多余升级取出来。
- 加速上限超过 12 时，化学灌注机、化学洗涤机、电解分离机、同位素离心机、颜料混合机、旋转冷凝机可能明显卡服（每 tick 操作数 = 2^加速数）。
- 不要和 MekanismTweaks、Mekanism Upgrade Caps、Upgrades: Reborn 等修改同一批方法的模组同时安装。

## 构建

需要 JDK 17：

```bash
./gradlew build
```

产物在 `build/libs/`。

## 许可证

AGPL-3.0-only，见 `LICENSE`。

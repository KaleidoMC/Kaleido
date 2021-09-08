# Kaleido使用指南

Kaleido是一个支持将自定义模型以数据包+资源包的形式导入游戏的模组。目前支持Forge 1.16.5。

## 环境要求

最新版Forge和Kiwi

## 导入模型

目前提供有方块小镇-Yuushya包（作者几何Coco）以供测试。

1. 为了方便使用，建议安装一个全局数据包模组，如[Open Loader](https://www.curseforge.com/minecraft/mc-mods/open-loader)；
2. 启动游戏后会在游戏主目录生成一个`openloader`目录（视安装的模组而定），将对应的数据包和资源包放入相应的目录；
3. 在游戏中使用`/reload`命令重载数据包；
4. 在游戏中按下`F3`+`T`重载资源包；
5. 此时在创造模式装饰标签下可以看到新添加的方块。

## 自定义模型

示例可参考方块小镇-Yuushya包（作者几何Coco）。

添加自定义模型需要同时拥有数据包和资源包，两者中的条目一一对应。

### 资源包

https://minecraft.fandom.com/zh/wiki/%E8%B5%84%E6%BA%90%E5%8C%85

#### 文件结构

(资源包名称)
 - pack.mcmeta
 - pack.png
 - assets
   - (命名空间)
     - models
	   - kaleido
	     - (模型).json
     - textures
	 - lang
	   - (语言).json

### 数据包

https://minecraft.fandom.com/zh/wiki/%E6%95%B0%E6%8D%AE%E5%8C%85

#### 文件结构

(数据包名称)
 - pack.mcmeta
 - pack.png
 - data
   - (命名空间)
     - kaleido
	   - (方块定义).json - 需要与资源包中对应模型同名

(方块定义).json（所有属性均为可选）
 - template - 模板。目前支持的值："block", "horizontal", "directional", "pillar", "item"
 - renderType - 渲染类型。指定template后无效。目前支持的值："solid", "cutout", "cutoutMipped", "translucent"。默认为"solid"
 - renderTypes - 渲染类型。字符串数组。指定template后无效。提供同时指定多个渲染类型的能力
 - noCollision - 无碰撞体积。指定template后无效。目前支持的值：true, false。默认为false
 - shape - 碰撞形状。指定template后无效。目前支持的值："empty", "block", 或使用Blockbench插件VoxelShape Generators导出的结果。默认为"empty"
 - price - 魔法布匹兑换价格。整数。默认为1
 - reward - 是否为收集奖励。目前支持的值：true, false。默认为false
 - behavior - 行为。字符串或对象。可实现右键方块切换模型等功能。待补充
 - behaviors - 行为。字符串或对象数组。可实现右键方块切换模型等功能。待补充
 - offset - 随机偏移。目前支持的值："XZ", "XYZ"

模型显示名称的语言ID为"kaleido.decor.(命名空间).(方块定义)"

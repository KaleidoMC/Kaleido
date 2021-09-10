# Kaleido使用指南

https://github.com/KaleidoMC/Kaleido/wiki

Kaleido是一个支持将自定义模型以数据包+资源包的形式导入游戏的模组。目前支持Forge 1.16.5。

## 环境要求

 - Forge 1.16.5 最新版
 - Kiwi 1.16.5 最新版
 - 另外，为了方便安装数据包，推荐安装一个全局数据包模组，如[Open Loader](https://www.curseforge.com/minecraft/mc-mods/open-loader)

## 导入模型

目前提供有方块小镇-Yuushya包（作者几何Coco、Xiao2）以供测试。

1. 启动游戏后会在主文件夹中生成一个`openloader`文件夹（视安装的模组而定），可将对应的数据包和资源包放入相应的文件夹；
2. 进入世界；
3. 在游戏中使用`/reload`命令重载数据包；
4. 在游戏中按下`F3`+`T`重载资源包；
5. 此时在创造模式装饰标签下可以看到新添加的方块。或使用模组中的木工台查看。

## Cocricot

1. 从[Cocricot官网](https://cocricot.pics/)下载Java 1.12.2 - Resourcepack & Mod
2. 从压缩文件中取出模组放入**resourcepacks**文件夹。如果想使用Cocricot提供的原版资源包，建议使用1.16.1版本的。
3. 下载并安装Kaleido - Cocricot兼容包。方法同导入模型部分的步骤。
4. 大功告成！


## 自定义模型

示例可参考方块小镇-Yuushya包（作者几何Coco）。

添加自定义模型需要同时拥有数据包和资源包，两者中的条目一一对应。

### 资源包

Kaleido的资源包与标准资源包无异。

https://minecraft.fandom.com/zh/wiki/%E8%B5%84%E6%BA%90%E5%8C%85

有几点需要注意：

1. 所有的Kaleido模型都需要放入`model/kaleido`文件夹。父模型仍可放在任何位置。
2. 无需编写`(blockstate).json`。
3. 方块的本地化key为"kaleido.decor.(命名空间).(Kaleido定义)"

### 数据包

https://minecraft.fandom.com/zh/wiki/%E6%95%B0%E6%8D%AE%E5%8C%85

(Kaleido定义).json

`(Kaleido定义).json`是定义行为和属性的核心文件。每个方块或物品都需要在单独的`data/(命名空间)/kaleido/(Kaleido定义).json`文件中定义。命名空间和文件名需要和资源包中的模型文件对应。

所有选项均为可选，也就是说你可以直接在文件里仅写下一对大括号。

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

### 示例

#### default.json

```json
{}
```

#### glass.json

```json
{
   "shape": "block",
   "renderType": "cutout",
   "glass": true
}
```

#### custom-shape.json

```json
{
   "shape":"VoxelShapes.join(Block.box(7.5, 0, 0, 8, 1, 1), Block.box(0, 0, 3, 12, 1, 16), IBooleanFunction.OR)"
}
```

#### transform.json

```json
{
   "template":"directional",
   "behavior":{
      "type":"onUseBlock",
      "action":{
         "type":"transform",
         "block":"stone"
      }
   }
}
```

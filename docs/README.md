# Kaleido使用指南

https://github.com/KaleidoMC/Kaleido/wiki

Kaleido是一个支持将自定义模型以数据包+资源包的形式导入游戏的模组。

## 环境要求

 - Forge 1.16.5 最新版
 - Kiwi 1.16.5 最新版

## 导入方块

1. 启动游戏后会在主文件夹中生成一个`kaleido-loader`文件夹。将数据包放入该文件夹，数据包将会被全局启用；
2. 将资源包放入`resourcepacks`文件夹，并在选项中启用资源包；
3. 进入世界；
4. 此时在创造模式装饰标签下可以看到新添加的方块。或使用模组中的木工台查看。

**注意：Kaleido不对因版本更新而导致的问题负责，更新前请先备份。**

## 导入Cocricot Mod方块

1. 从[Cocricot官网](https://cocricot.pics/)下载Java 1.12.2 - Resourcepack & Mod
2. 从压缩文件中取出模组放入**resourcepacks**文件夹并启用它。如果想使用Cocricot提供的原版资源包，建议使用1.16.1版本的。
3. 下载并安装Kaleido - Cocricot兼容包（请在CurseForge页面内查找）。方法同**导入方块**部分的步骤。
4. 大功告成！

## 导入Yuushya Townscape方块

1. 下载[Yuushya Townscape](https://www.curseforge.com/minecraft/mc-mods/yuushya-townscape-fabric)和[Yuushya 16x](https://www.curseforge.com/minecraft/texture-packs/yuushya-16x)
2. 从压缩文件中取出模组放入**resourcepacks**文件夹并启用它。
3. 下载并安装Kaleido - Yuushya兼容包（请在CurseForge页面内查找）。方法同**导入方块**部分的步骤。
4. 大功告成！

**注意：目前Cocricot与Yuushya支持仍存在一些问题，欢迎给[转换脚本](https://github.com/KaleidoMC/BridgeBuilder)提交PR来修复问题。**

## 工具

### 多方块

多方块结构功能允许你在同一位置移动、旋转、缩放和堆叠方块。

按下Ctrl+F可将指向的方块变为多方块结构。再次按下Ctrl+F或空手右击多方块结构可打开配置界面。

手持方块右击多方块可将手中方块形状加入到多方块中。

在多方块界面中，你可以拖拽和滑动鼠标滚轮来调整多方块显示的视角。

你可以点击左侧的按钮来选择你想要调整的层次。点击叉号按钮可以删除该层次。

点击取消按钮可以取消你的所有更改。否则离开界面后，所有更改将自动应用。

### 凿子

凿子允许你将完整方块雕凿为楼梯或台阶等形状。

左击方块可以在所有可雕凿的形状间循环。鼠标中键单击方块可以选择你想要雕凿成的形状。

### 画刷

画刷是用来从世界中拾取和混合颜色的工具。鼠标中键单击方块可以拾取该方块的颜色。如果画刷已经拥有颜色，将会与新的颜色混合。

按住Ctrl拾取可以禁用混色。

按住Shift拾取可以禁用从游戏着色器中获取颜色，改为以默认方式直接获取屏幕上的颜色。

鼠标中键单击空气可以清除画刷上的颜色。

右击可被染色的方块可对方块染色。

### 建筑师锤

建筑师锤是[调试棒](https://minecraft.fandom.com/zh/wiki/%E8%B0%83%E8%AF%95%E6%A3%92)的安全版本，允许你在生存模式下轻松地调整方块的状态。

来自Minecraft Wiki：

> 调试棒能被用于调整方块状态。对方块点击可以指定你所想要更改的方块状态。比如，你可以为命令方块修改conditional与facing这两个方块状态。对方块使用将使你循环改变指定的方块状态。例如，如果要你指定了facing这个方块状态，你可以让命令方块朝向north、south、east或west。潜行点击将会使它反向循环。

建筑师锤只能用于调整栅栏、玻璃板等建筑方块和来自Kaleido的方块。

## 自定义

### 资源包

Kaleido的资源包与标准资源包无异。

[资源包介绍](https://minecraft.fandom.com/zh/wiki/%E8%B5%84%E6%BA%90%E5%8C%85)

有几点需要注意：

1. 所有的Kaleido模型都需要放入`model/kaleido`文件夹。父模型仍可放在任何位置。
2. 无需编写`(blockstate).json`。
3. 方块的本地化key为"kaleido.decor.(命名空间).(Kaleido定义)"

#### OBJ模型

你需要编写一个额外的JSON文件来引用OBJ模型。JSON文件格式请参考[这篇教程](https://boson-english.v2mcdev.com/specialmodel/obj-model.html)。

```json
{
  "loader": "forge:obj",
  "model": "kaleido_test:models/kaleido/test.obj",
  "flip-v": true,
  "textures": {
    "particle": "minecraft:block/dirt"
  },
  "detectCullableFaces": false,
  "diffuseLighting": true,
  "ambientToFullbright": false
}
```

## 以下内容暂未更新，请参考英文版wiki

### 数据包

[数据包介绍](https://minecraft.fandom.com/zh/wiki/%E6%95%B0%E6%8D%AE%E5%8C%85)

(Kaleido定义).json

`(Kaleido定义).json`是定义行为和属性的核心文件。每个方块或物品都需要在单独的`data/(命名空间)/kaleido/(Kaleido定义).json`文件中定义。命名空间和文件名需要和资源包中的模型文件对应。

所有选项均为可选，也就是说你可以直接在文件里仅写下一对大括号。

 - template - 模板。目前支持的值："block", "horizontal", "directional", "pillar", "item"
 - renderType - 渲染类型。指定template后无效。目前支持的值："solid", "cutout", "cutoutMipped", "translucent"。默认为"solid"
 - noCollision - 无碰撞体积。指定template后无效。目前支持的值：true, false。默认为false
 - shape - 碰撞形状。指定template后无效。目前支持的值："empty", "block", 或使用Blockbench插件VoxelShape Generators导出的结果。默认为"empty"
 - price - 魔法布匹兑换价格。整数。默认为1
 - reward - 是否为收集奖励。目前支持的值：true, false。默认为false
 - behavior - 行为。字符串或对象。可实现右键方块切换模型等功能。待补充
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
   "event.onUseBlock":{
      "action":{
         "type":"transform",
         "block":"stone"
      }
   }
}
```
